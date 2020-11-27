package com.imooc.service.center;

import com.imooc.pojo.Orders;
import com.imooc.pojo.vo.OrderStatusCountsVO;
import com.imooc.utils.PagedGridResult;

public interface MyOrdersService {
    
    /**
     * 
     * 
     * @param userId
     * @param orderStatus
     * @param page
     * @param pageSize
     * @return
     */

    public PagedGridResult queryMyOrders(String userId,
                                         Integer orderStatus,
                                         Integer page,
                                         Integer pageSize);

    /**
     * 更新订单，确认收货
     * @param orderId
     */

    public void updateDeliverOrderStatus(String orderId);

    /**
     * 查询我的订单
     * @param orderId
     * @param userId
     * @return
     */

    public Orders queryMyOrder(String orderId, String userId);

    /**
     * 更新接收商品的订单状态
     * @param orderId
     * @return
     */

    public boolean updateReceiveOrderStatus(String orderId);

    /**
     * 删除订单
     * @param userId
     * @param orderId
     * @return
     */

    public boolean deleteOrder(String userId,String orderId);

    /**
     * 获取订单状态数目
     * @param userId
     * @return
     */

    public OrderStatusCountsVO getMyOrderStatusCounts(String userId);


    /**
     * 获取订单动向
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getMyOrderTrend(String userId,
                                         Integer page,
                                         Integer pageSize);


}
