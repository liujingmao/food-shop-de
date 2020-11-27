package com.imooc.controller;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.PayMethod;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.MerchantOrdersVO;
import com.imooc.pojo.vo.OrderVO;
import com.imooc.service.OrderService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

//import com.imooc.utils.CookieUtils;


/**
 * Created by liujingmao on 2019-12-01.
 */


@Api(value = "订单相关接口",tags = {"订单相关接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

        @Autowired
        private OrderService orderService;

        @Autowired
        private RestTemplate restTemplate;

        @Autowired
        private RedisOperator redisOperator;

        @ApiOperation(value = "用户下单",notes = "用户下单",httpMethod = "POST")
        @PostMapping("/create")
        public IMOOCJSONResult create(@RequestBody SubmitOrderBO submitOrderBO,
                                      HttpServletRequest request,
                                      HttpServletResponse response){

            if(submitOrderBO.getPayMethod()!= PayMethod.WEIXI.type
                    && submitOrderBO.getPayMethod()!=PayMethod.ALIPAY.type)
                    return IMOOCJSONResult.errorMsg("请选择支付方式");

           // System.out.println(submitOrderBO.toString());

            //创建订单之间，要在redis中写判断
            String shopcartJson = redisOperator.get(FOODIE_SHOPCART+":"+submitOrderBO.getUserId());
            if(StringUtils.isBlank(shopcartJson)){
                return IMOOCJSONResult.errorMsg("购物车数据不对！！！");
            }

            List<ShopcartBO> shopcartlist = new ArrayList<>();
            shopcartlist= JsonUtils.jsonToList(shopcartJson,ShopcartBO.class);


            //1. 创建订单
            OrderVO orderVO = orderService.createOrder(shopcartlist,submitOrderBO);
            String orderId = orderVO.getOrderId();

            //2. 创建订单以后，移除购物中已经结算（已经提交）商品
            /**
             * 1001
             * 2002
             * 3003
             * 4004
             */
            //创建订单后，清除覆盖
            shopcartlist.removeAll(orderVO.getToBeRemovedShopcatdList());
            redisOperator.set(FOODIE_SHOPCART+":"+submitOrderBO.getUserId(),JsonUtils.objectToJson(shopcartlist));
            //TODO 整合Redis之后，完善购物车中的已经结算商品清除，并同步到前端的cookies上
            CookieUtils.setCookie(request,response,FOODIE_SHOPCART,JsonUtils.objectToJson(shopcartlist),true);

            //3. 向支付中心发送当前订单，用于保存动支付中心的订单

            MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
            merchantOrdersVO.setReturnUrl(payReturnUrl);

            merchantOrdersVO.setAmount(1);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("imoocUserId","5746948-160143836");
            headers.add("password","eokr-rogk-pto1-vkz1");

            HttpEntity<MerchantOrdersVO> entity =
                    new HttpEntity<>(merchantOrdersVO,headers);

            ResponseEntity<IMOOCJSONResult> responseEntity=
                    restTemplate.postForEntity(paymentUrl,
                                                entity,
                                                IMOOCJSONResult.class);

            IMOOCJSONResult paymentResult = responseEntity.getBody();

            /*if(paymentResult.getStatus() == 200){
                return IMOOCJSONResult.errorMsg("创建的订单有问题，请联系管理员");
            }*/

           return IMOOCJSONResult.ok(orderId);


            // String orderId =orderService.createOrder(submitOrderBO);

        }

    @PostMapping("notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId) {
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    @PostMapping("getPaidOrderInfo")
    public IMOOCJSONResult getPaidOrderInfo(String orderId){

        OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
        return IMOOCJSONResult.ok(orderStatus);

    }

}
