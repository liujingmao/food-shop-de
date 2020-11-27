package com.imooc.service;

import com.imooc.pojo.UserAddress;
import com.imooc.pojo.bo.AddressBO;

import java.util.List;

public interface AddressService {
    /**
     *根据用户id查询用户收货地址
     * @param
     * @return
     */

    public List<UserAddress> queryAll(String userId);

    /**
     * 新增用户地址
     * @param addressBO
     */
    public void addNewUserAddress(AddressBO addressBO);

    /**
     * 修改用户地址
     * @param addressBO
     */

    public void updateUserAddress(AddressBO addressBO);

    /**
     * 根据用户id和地址id删除地址
     * @param userId
     * @param addressId
     */
    public void deleteUserAddress(String userId,String addressId);

    /**
     * 将地址设置为默认地址
     * @param userId
     * @param addressId
     */
    public void updateUserAddressToBeDefault(String userId,String addressId);

    /**
     * 根据用户id和地址id查询用户地址
     * @param userId
     * @param addressId
     * @return
     */

    public UserAddress queryUserAddres(String userId,String addressId);

}
