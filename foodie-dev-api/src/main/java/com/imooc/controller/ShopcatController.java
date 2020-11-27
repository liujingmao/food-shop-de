package com.imooc.controller;

import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
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
 * Created by liujingmao on 2019-11-30.
 */

@Api(value = "购物车接口",tags = {"购物车接口相关的api"})
@RequestMapping("shopcart")
@RestController
public class ShopcatController extends BaseController{

    @Autowired
    private RedisOperator redisOperator;


    @ApiOperation(value = "添加商品到购物车",notes = "添加商品到购物车",httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(
            @RequestParam String userId,
            @RequestBody ShopcartBO shopcartBO,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        //判断userid是否为空
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg("");
        }
        System.out.println(shopcartBO);

        // TODO 前端用户在登录的情况下，添加商品到购物车，会同时在后端同步购物车到redisCache
        // TODO 需要判断当前购物车上包含已经存在的商品，如果存在则累加购买数据

        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> shopcartList = null;
        if (StringUtils.isNotBlank(shopcartJson)) {
            // redis中已经有购物车了
            shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);
            // 判断购物车中是否存在已有商品，如果有的话counts累加
            boolean isHaving = false;
            for (ShopcartBO sc: shopcartList) {
                String tmpSpecId = sc.getSpecId();
                if (tmpSpecId.equals(shopcartBO.getSpecId())) {
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if (!isHaving) {
                shopcartList.add(shopcartBO);
            }
        } else {
            // redis中没有购物车
            shopcartList = new ArrayList<>();
            // 直接添加到购物车中
            shopcartList.add(shopcartBO);
        }

        // 覆盖现有redis中的购物车
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));

        /*String shopcartJson = redisOperator.get(FOODIE_SHOPCART+":"+userId);
        List<ShopcartBO> shopcartList = null;

        //如果redis中查询数据shopcatJson为空,
        if(StringUtils.isNotBlank(shopcartJson)){
            shopcartList = JsonUtils.jsonToList(shopcartJson,ShopcartBO.class);
            Boolean isHaving = false;
            for(ShopcartBO sc: shopcartList){
                String tmpSpecId = sc.getSpecId();
                if(tmpSpecId.equals(shopcartBO.getSpecId())){
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving=true;
                }
            }
            if(!isHaving){
                shopcartList.add(shopcartBO);
            }

        } else {
            //REDIS中没有购物车
            shopcartList = new ArrayList<>();
            //直接添加到购物车中
            shopcartList.add(shopcartBO);
        }
        redisOperator.set(FOODIE_SHOPCART+":"+userId,JsonUtils.objectToJson(shopcartList));*/
        return IMOOCJSONResult.ok();
    }
    @ApiOperation(value = "从购物车删除商品",notes = "从购物车删除商品",httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(
            @RequestParam String userId,
            @RequestParam String itemSpecId,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        //判断userid是否为空
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)){
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }

        // TODO 用户在页面删除购物中的商品数据，如果此时用户已经登录，则需要同时同步删除后端购物车中的商品数据

        String shopcartJson = redisOperator.get(FOODIE_SHOPCART+":"+userId);
        if(StringUtils.isNotBlank(shopcartJson)){
            List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson,ShopcartBO.class);
            for (ShopcartBO sc:shopcartList){
                String tmpSpecId = sc.getSpecId();
                if(tmpSpecId.equals(sc.getSpecId())){
                    shopcartList.remove(sc);
                    break;
                }
            }
            redisOperator.set(FOODIE_SHOPCART+":"+userId,JsonUtils.objectToJson(shopcartList));
        }

        return IMOOCJSONResult.ok();
    }

}
