package com.imooc.service.center;

import com.imooc.pojo.OrderItems;
import com.imooc.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

public interface MyCommentsService {

    /**
     * 根据订单查询用户评价
     * @param orderId
     * @return
     */

    public List<OrderItems> queryPendingComment(String orderId);

    /**
     * 根据订单id和用户id,订单评价列表保存点评
     * @param orderId
     * @param userId
     * @param commentList
     */

    public void saveComments(String orderId,String userId,List<OrderItemsCommentBO> commentList);

    /**
     * 查询我的订单评价
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */

    public PagedGridResult queryMyComments(String userId,Integer page,Integer pageSize);
}
