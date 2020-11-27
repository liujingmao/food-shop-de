package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.Orders;
import com.imooc.pojo.vo.OrderStatusCountsVO;
import com.imooc.service.center.MyOrdersService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by liujingmao on 2019-12-28.
 */

@Api(value = "用户中心订单",tags = {"用户中心订单相关的接口"})
@RestController
@RequestMapping("myorders")
public class MyOrdersController extends BaseController {

    @Autowired
    private MyOrdersService myOrdersService;

    @ApiOperation(value = "获得订单状态数概况",notes="获得订单状态数概况",httpMethod = "POST")
    @PostMapping("/statusCounts")
    public IMOOCJSONResult statusCounts(@ApiParam(name="userId",value = "用户id",required = true)
                                 @RequestParam String userId){
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }

        OrderStatusCountsVO countsVO = myOrdersService.getMyOrderStatusCounts(userId);
        return IMOOCJSONResult.ok(countsVO);
    }

    @ApiOperation(value = "查询订单信息",notes="查询订单信息",httpMethod = "POST")
    @PostMapping("/query")
    public IMOOCJSONResult query(@ApiParam(name="userId",value = "用户id",required = true)
                                     @RequestParam String userId,
                                 @ApiParam(name = "orderStatus",value = "订单状态",required = false)
                                 @RequestParam Integer orderStatus,
                                 @ApiParam(name = "page",value = "查询一一页的第几页",required = false)
                                 @RequestParam Integer page,
                                 @ApiParam(name = "pageSize",value = "分页的每一页显示的条数",required = false)
                                 @RequestParam Integer pageSize){
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }
        if(page==null){
            page=1;
        }
        if (pageSize==null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult result = myOrdersService.queryMyOrders(userId,orderStatus,page,pageSize);
        return IMOOCJSONResult.ok(result);
    }

    //商家发货没有后端，所以这个接口仅仅只是用户模拟
    @ApiOperation(value = "商家发货",notes = "商家发货",httpMethod ="GET" )
    @GetMapping("/deliver")
    public IMOOCJSONResult deliver(
            @ApiParam(name = "orderId",value = "订单id",required = true)
            @RequestParam String orderId) throws Exception{
        if(StringUtils.isBlank(orderId)){
            return IMOOCJSONResult.errorMsg("订单id不能为空");
        }
        myOrdersService.updateDeliverOrderStatus(orderId);
        return IMOOCJSONResult.ok();

    }

    @ApiOperation(value = "用户确认收货",notes = "用户确认收货",httpMethod ="POST" )
    @PostMapping("/confirmReceive")
    public IMOOCJSONResult confirmReceive(
            @ApiParam(name = "orderId",value = "订单id",required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId) throws Exception {
        IMOOCJSONResult checkResult=checkUserOrder(userId,orderId);

        if(checkResult.getStatus()!= HttpStatus.OK.value()){
            return checkResult;
        }
        boolean res = myOrdersService.updateReceiveOrderStatus(orderId);
        if(!res){
            return IMOOCJSONResult.errorMsg("订单接收失败");
        }
        return IMOOCJSONResult.ok();

    }

    @ApiOperation(value = "用户收货",notes = "用户收货",httpMethod ="POST" )
    @PostMapping("/delete")
    public IMOOCJSONResult delete(
            @ApiParam(name = "orderId",value = "订单id",required = true)
            @RequestParam String orderId,
            @ApiParam(name = "userId",value = "用户id",required = true)
            @RequestParam String userId) throws Exception {
        IMOOCJSONResult checkResult=checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!= HttpStatus.OK.value()){
            return checkResult;
        }
        boolean res = myOrdersService.deleteOrder(userId,orderId);
        if(!res){
            return IMOOCJSONResult.errorMsg("订单删除不成功");
        }
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "查询订单动向",notes="查询订单动向",httpMethod = "POST")
    @PostMapping("/trend")
    public IMOOCJSONResult trend(@ApiParam(name="userId",value = "用户id",required = true)
                                 @RequestParam String userId,
                                 @ApiParam(name = "page",value = "查询一一页的第几页",required = false)
                                 @RequestParam Integer page,
                                 @ApiParam(name = "pageSize",value = "分页的每一页显示的条数",required = false)
                                 @RequestParam Integer pageSize){
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }
        if(page==null){
            page=1;
        }
        if (pageSize==null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult result = myOrdersService.getMyOrderTrend(userId,page,pageSize);
        return IMOOCJSONResult.ok(result);
    }

    /**
     * 用户和订单关联，
     * @param userId
     * @param orderId
     * @return
     */
    public IMOOCJSONResult checkUserOrder(String userId,String orderId){
        Orders order = myOrdersService.queryMyOrder(orderId,userId);
        if(order == null){
           // System.out.println(order.toString());
            return IMOOCJSONResult.errorMsg("订单不存在");  //TODO 有异常，查询不到数据库中的订单
        }
       return IMOOCJSONResult.ok(order);
    }

    }



