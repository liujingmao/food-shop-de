package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.UserBO;
import com.imooc.service.UserService;
import com.imooc.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liujingmao on 2019-11-10.
 * 2020-3-15 更新Redis功能
 */
@Api(value = "注册登录",tags = {"用于注册登录的相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController  extends BaseController{

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    //第一版本，直接打印相关的status代码
    /*@GetMapping("/usernameIsExist")
    public int usernameIsExist(@RequestParam String username){
        //1. 判断用户是否为空
        if(StringUtils.isBlank(username)){
            return 500;
        }

        //2. 判断用户是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if(isExist){
            return 500;
        }

        return 200;
    }*/

    /**
     * 判断用户是否存在
     * @param username
     * @return
     */

    //第二版本，引入一个高可用的类：IMOOCJSONResult，高可用的类
    @ApiOperation(value = "用户是否存在",notes = "用户是否存在",httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username){
        //1. 判断用户是否为空
        if(StringUtils.isBlank(username)){
            return IMOOCJSONResult.errorMap("用户不能为空");
        }
        //2. 判断用户是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if(isExist){
            return IMOOCJSONResult.errorMap("用户已经存在，请用另一个大名");
        }
        return IMOOCJSONResult.ok();
    }

    /**
     * 新用户注册
     * @param userBO
     * @return
     */
    // 用户注册
    @ApiOperation(value = "注册用户",notes = "注册并创建用户",httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,HttpServletRequest request, HttpServletResponse response){
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();

        // 0. 判断用户名和密码必须不为空
        if(StringUtils.isBlank(username)||
                StringUtils.isBlank(password)||
                StringUtils.isBlank(confirmPassword)){
            return IMOOCJSONResult.errorMsg("用户名或者密码不能为空");
        }
        // 1. 查询用户名在数据库是否存在
        boolean IsExist = userService.queryUsernameIsExist(username);
        if (IsExist){
            return IMOOCJSONResult.errorMsg("用户已经存在，请重新输入");
        }
        // 2. 密码长度不能少于8位
        if(password.length() < 6){
            return IMOOCJSONResult.errorMsg("密码的长度不能少于6");
            // TODO 密码还应该被加强，除了规定长度外，还应该增强密码强度，比如判断密码是否同时包括大小写、数字、还有特殊字符

        }
        // 3. 判断再次密码是否一致
        if(!password.equals(confirmPassword)){
            return IMOOCJSONResult.errorMsg("两次输入的密码不同，请重新输入");
        }
        // 4. 实现注册
        Users userResult = userService.createUser(userBO);
        userResult = setNullProperty(userResult);
        CookieUtils.setCookie(request,response,"user",JsonUtils.objectToJson(userResult),true);
        synchShopcartDate(userResult.getId(),request,response);

        return IMOOCJSONResult.ok();
    }

    /**
     * 用户登录
     * @param userBO
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "用户登录",notes = "用户登录",httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO, HttpServletRequest request, HttpServletResponse response) throws Exception{

        String username = userBO.getUsername();
        String password = userBO.getPassword();

        // 0. 判断用户名和密码必须不为空
        if(StringUtils.isBlank(username)||
                StringUtils.isBlank(password)){
            return IMOOCJSONResult.errorMsg("用户名或者密码不能为空");
        }
        // 1. 查询用户名在数据库是否存在,防止用户输入错误
        boolean IsExist = userService.queryUsernameIsExist(username);
        if (!IsExist){
            return IMOOCJSONResult.errorMsg("用户不存在，请注册");
        }

        Users userResult = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));

        if(userResult==null){
            return IMOOCJSONResult.errorMsg("用户名或者密码为空");
        }

        userResult =setNullProperty(userResult);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(userResult),true);

        // TODO 生成用户token,存入Redis会话
        // TODO 同步购物车数据
        synchShopcartDate(userResult.getId(),request,response);


        return IMOOCJSONResult.ok(userResult);

    }

    /**
     * 注册登录后，同步cookie和redis中的数据
     */

    private void synchShopcartDate(String userId,
                                   HttpServletRequest request,
                                   HttpServletResponse response){

        /**
         * 1.redis中无数据，如果cookies购物车中为空，不做任何处理
         *                如果购物中有商品，此时直接写入redis
         * 2.redis有数据，如果cookies中的购物车为空，那么直接把redis的购物车覆盖本地cookies
         *              如果cookies中的购物车不为空
         *                  如果cookies中的某个商品已经存在，则以cookies中的商品为准，删除redis中的
         *                  把cookies中的商品直接覆盖redis中
         * 3.同步到redis中去了以后，覆盖本地cookies购物车的数据
         *   保证本地购物车的数据同步
         */

        //1.从redis中获取数据
        String shopcartJsonRedis = redisOperator.get(FOODIE_SHOPCART+":"+userId);
        //2.从cookies中获取购物车
        String shopcartStrCookie =CookieUtils.getCookieValue(request,FOODIE_SHOPCART,true);

        if(StringUtils.isBlank(shopcartJsonRedis)){
            // redis 为空，cookies不为空，因为以cookie中的数据为准，所以直接将cookies数据写入到redis
            if(StringUtils.isNotBlank(shopcartStrCookie)){
                redisOperator.set(FOODIE_SHOPCART+":"+userId,shopcartStrCookie);
            }
        }else {
            //redis不为空，cookie不为空，合并cookie和redis中购物车的商品数据(同一商品，则以cookie中为准，覆盖redis)
            if(StringUtils.isNotBlank(shopcartStrCookie)){
                /**
                 * 1.已经存在，把cookie中对应的数据，覆盖redis参考京东
                 * 2.该项商品标记为待删除，统一放入一个待删除的List
                 * 3.从cookies中清除所有的待删除的list
                 * 4.合并redis和cookie中的数据
                 * 5.更新到redis和cookie中的数据
                 */
                List<ShopcartBO> shopcartListRedis = JsonUtils.jsonToList(shopcartJsonRedis,ShopcartBO.class);
                List<ShopcartBO> shopcartListCookie= JsonUtils.jsonToList(shopcartStrCookie,ShopcartBO.class);

                //待删除的List
                List<ShopcartBO> pendingDeleteList = new ArrayList<>();

                for(ShopcartBO redisShopCart:shopcartListRedis){
                    String redisSpecId = redisShopCart.getSpecId();
                    for(ShopcartBO cookieShopCart:shopcartListCookie){
                        String cookieSpecId = cookieShopCart.getSpecId();
                        if(redisSpecId.equals(cookieSpecId)){
                            //pendingDeleteList.add(cookieShopCart);
                            //覆盖购买数量，不累加，参考京东
                            redisShopCart.setBuyCounts(cookieShopCart.getBuyCounts());
                            //把cookieShopcart放入删除列表，用于最后的删除和合并
                            pendingDeleteList.add(cookieShopCart);
                        }
                    }
                }
                //从现有的cookie中删除对应的覆盖的商品数据
               shopcartListCookie.removeAll(pendingDeleteList);
                //合并两个List
                shopcartListRedis.addAll(shopcartListCookie);
                //更新到redis和ck
                CookieUtils.setCookie(request,response,JsonUtils.objectToJson(shopcartJsonRedis),"");
                redisOperator.set(FOODIE_SHOPCART+":"+userId,JsonUtils.objectToJson(shopcartJsonRedis));

            } else {
                //redis 不为空，cookie为空，直接用redis覆盖cookie
               CookieUtils.setCookie(request
               ,response,FOODIE_SHOPCART,shopcartJsonRedis,true);
            }
        }

    }
   //
    private Users setNullProperty(Users  userResult){
        userResult.setPassword(null);
        userResult.setMobile(null);
        userResult.setEmail(null);
        userResult.setUpdatedTime(null);
        userResult.setCreatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    /**
     * 用户退出登录
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @ApiOperation(value = "用户退出登录",notes = "用户退出登录",httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId,HttpServletRequest request,HttpServletResponse response){
        //清除Cookies
        CookieUtils.deleteCookie(request,response,"user");

        // TODO 清空用户购物车中的信息
        // TODO 分布式会话中需要清除的有关用户的信息

        return IMOOCJSONResult.ok();
    }



}
