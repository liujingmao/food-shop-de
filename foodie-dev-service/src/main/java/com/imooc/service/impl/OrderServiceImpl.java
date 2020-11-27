package com.imooc.service.impl;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.OrderItemsMapper;
import com.imooc.mapper.OrderStatusMapper;
import com.imooc.mapper.OrdersMapper;
import com.imooc.pojo.*;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.MerchantOrdersVO;
import com.imooc.pojo.vo.OrderVO;
import com.imooc.service.AddressService;
import com.imooc.service.ItemService;
import com.imooc.service.OrderService;
import com.imooc.utils.DateUtil;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by liujingmao on 2019-11-23.
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    /**
     * 创建订单，传入submitOrderBO
     * @param submitOrderBO
     * @return
     */

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(List<ShopcartBO>shopcartList, SubmitOrderBO submitOrderBO) {

        String userId = submitOrderBO.getUserId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        String addressId = submitOrderBO.getAddressId();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        Integer postAmount = 0 ; // 包邮费用设置为o

        // 1. Saving New Orders
        String orderId = sid.nextShort();
        UserAddress address = addressService.queryUserAddres(userId,addressId);

        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);

        newOrder.setReceiverName(address.getReceiver());
        newOrder.setReceiverMobile(address.getMobile());
        newOrder.setReceiverAddress(address.getProvince()+""+address.getCity()+""+
                address.getDistrict()+""+address.getDetail());

        //newOrder.setTotalAmount();
         // newOrder.setRealPayAmount();
        newOrder.setPostAmount(postAmount);

        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);

        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());


        // 2. 循环itemSpecIds保存订单商品信息表

        String itemSpecIdArr [] = itemSpecIds.split(",");
        Integer totalAmount =0 ;     //
        Integer realPayAmount =0;    //
        List<ShopcartBO> toBeRemovedShopcartList = new ArrayList<>();
        for(String itemSpecId : itemSpecIdArr){

           //从购物车中获取shopcartBO类
            ShopcartBO cartItem = getBuyCountsFromShopcart(shopcartList,itemSpecId);

            //TODO 整合Redis后，商品购买的数量重新从Redis获取
            int buyCounts = cartItem.getBuyCounts();
            toBeRemovedShopcartList.add(cartItem);

            // 2.1 根据规格id,查询规格的具体信息，查询规格的具体信息，主要是获取商品价格
            ItemsSpec itemsSpec = itemService.queryItemSpecById(itemSpecId);
            totalAmount+= itemsSpec.getPriceNormal()*buyCounts;
            realPayAmount+= itemsSpec.getPriceDiscount()*buyCounts;

            //2.2 获取商品信息，根据商品Id

            String itemId = itemsSpec.getItemId();
            Items item = itemService.queryItemById(itemId);
            String imgUrl = itemService.queryItemMainImgById(itemId);

            // 2.3 保存商品数据到数据库
            String subOrderId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemName(item.getItemName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemsSpec.getName());
            subOrderItem.setPrice(itemsSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);
            //2.4 在用户提交订单以后，规格表中需要扣除库存
            itemService.decreaseItemSpecStock(itemSpecId,buyCounts);
        }

            newOrder.setTotalAmount(totalAmount);
            newOrder.setRealPayAmount(realPayAmount);
            ordersMapper.insert(newOrder);
            //3. 保存订单状态表
            OrderStatus waitPayOrderStatus = new OrderStatus();
            waitPayOrderStatus.setOrderId(orderId);
            waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
            waitPayOrderStatus.setCreatedTime(new Date());
            orderStatusMapper.insert(waitPayOrderStatus);

            //4 构建商户订单，用于传给支付中心
            MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
            merchantOrdersVO.setMerchantOrderId(orderId);
            merchantOrdersVO.setMerchantUserId(userId);
            merchantOrdersVO.setAmount(realPayAmount+postAmount);
            merchantOrdersVO.setPayMethod(payMethod);

            //5 自定义订单vo

            OrderVO orderVO = new OrderVO();
            orderVO.setOrderId(orderId);
            orderVO.setMerchantOrdersVO(merchantOrdersVO);
            orderVO.setToBeRemovedShopcatdList(toBeRemovedShopcartList); //
            return orderVO;

    }

    /**
     * 从购物车中获取数量
     * @param shopcartBOList
     * @param specId
     * @return
     */

    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartBOList,String specId){
        for(ShopcartBO cart:shopcartBOList){
            if(cart.getSpecId().equals(specId)){
                return cart;
            }
        }
        return null;
    }

    /**
     * 更新订单状态
     * @param orderId
     * @param orderStatus
     */

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    /**
     * 查询订单状态信息
     * @param orderId
     * @return
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }


    /**
     * 关闭超时订单
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void closeOrder() {
        //1.查询所有未支付订单，判断是否超过1day,超过就关闭订单
        OrderStatus queryOrder=new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);
        for(OrderStatus os:list){
            //
            Date createTime=os.getCreatedTime();
            int days= DateUtil.daysBetween(createTime,new Date());
            if(days>1){
                doCloseOrder(os.getOrderId());
            }
        }
    }

    //根据订单id关闭订单
    void doCloseOrder(String orderId){
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }

}
