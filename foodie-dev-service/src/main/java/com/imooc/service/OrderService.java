package com.imooc.service;

import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.OrderVO;
import java.util.List;

/**
 *
 * 订单模块的服务，创建订单
 */

public interface OrderService {

    /**
     * 创建订单
     * @param submitOrderBO
     * @return
     */
    public OrderVO createOrder(List<ShopcartBO> shopcatList, SubmitOrderBO submitOrderBO);

    /**
     * 更新订单状态
     * @param orderId
     * @param orderStatus
     */

    public void updateOrderStatus(String orderId, Integer orderStatus);

    /**
     *  查询订单状态信息
     * @param orderId
     * @return
     */

    public OrderStatus queryOrderStatusInfo(String orderId);

    /**
     * 关闭超时未支付订单
     */

    public void closeOrder();


}
