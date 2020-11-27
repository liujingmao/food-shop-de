package com.imooc.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.enums.YesOrNo;
import com.imooc.pojo.OrderItems;
import com.imooc.pojo.Orders;
import com.imooc.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.service.center.MyCommentsService;
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
import java.util.List;


/**
 * Created by liujingmao on 2019-12-28.
 */

@Api(value = "用户中心评价相关的接口",tags = {"用户中心评价相关的接口"})
@RestController
@RequestMapping("mycomments")
public class MyCommentsController extends BaseController {

    @Autowired
    private MyCommentsService myCommentsService;

    @Autowired
    private MyOrdersService myOrdersService;

    @ApiOperation(value = "查询订单信息",notes="查询订单信息",httpMethod = "POST")
    @PostMapping("/pending")
    public IMOOCJSONResult pending(
                                @ApiParam(name="userId",value = "用户id",required = true)
                                @RequestParam String userId,
                                 @ApiParam(name = "orderId",value = "订单id",required = true)
                                 @RequestParam String orderId){
        // 判断用户和订单是否关联
        IMOOCJSONResult checkResult = checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!=HttpStatus.OK.value()){
            return checkResult;
        }
        Orders myOrder = (Orders) checkResult.getData();
        if(myOrder.getIsComment() == YesOrNo.YES.type){
            IMOOCJSONResult.errorMsg("该订单已经评价过了！！");
        }

        List<OrderItems> list = myCommentsService.queryPendingComment(orderId);
        return IMOOCJSONResult.ok(list);
    }

    @ApiOperation(value = "查询订单信息",notes="查询订单信息",httpMethod = "POST")
    @PostMapping("/saveList")
    public IMOOCJSONResult saveList(
            @ApiParam(name="userId",value = "用户id",required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId",value = "订单id",required = true)
            @RequestParam String orderId,
            @RequestBody List<OrderItemsCommentBO> commentList){

       // System.out.println(commentList);
        // 判断用户和订单是否关联
        IMOOCJSONResult checkResult = checkUserOrder(userId,orderId);
        if(checkResult.getStatus()!=HttpStatus.OK.value()){
            return checkResult;
        }

        //判断评论内容list不能为空
        if(commentList==null||commentList.isEmpty()||commentList.size()==0){
            return IMOOCJSONResult.errorMsg("评论内容不能为空");
        }

        myCommentsService.saveComments(orderId,userId,commentList);

        return IMOOCJSONResult.ok();
    }

    public IMOOCJSONResult checkUserOrder(String userId,String orderId){
        Orders order = myOrdersService.queryMyOrder(orderId,userId);
        if(order == null){
            return IMOOCJSONResult.errorMsg("订单不存在");  //TODO 有异常，查询不到数据库中的订单
        }
        return IMOOCJSONResult.ok(order);
    }

    /**
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询我的商品评价",notes="查询我的商品评价",httpMethod = "POST")
    @PostMapping("/query")
    public IMOOCJSONResult query(
            @ApiParam(name="userId",value = "用户id",required = true)
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
        PagedGridResult grid = myCommentsService.queryMyComments(userId,
                page,pageSize);
        return IMOOCJSONResult.ok(grid);
    }

}



