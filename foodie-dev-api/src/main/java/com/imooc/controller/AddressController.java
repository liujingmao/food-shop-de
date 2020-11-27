package com.imooc.controller;

import com.imooc.pojo.UserAddress;
import com.imooc.pojo.bo.AddressBO;
import com.imooc.service.AddressService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by liujingmao on 2019-11-09.
 */

@Api(value = "地址相关的api接口",tags = {"地址相关的api接口"})
@RequestMapping("address")
@RestController
public class AddressController {
    /**
     * 用户在确认订单页面后，可以针对收货地址做如下操作
     * 1.查询用户所有收货地址列表
     * 2.新增收货地址
     * 3.删除收货地址
     * 4.修改收货地址
     * 5.设置默认地址
     */

    @Autowired
    private AddressService addressService;

    /**
     * 根据用户id查询收货地址
     * @param userId
     * @return
     */

    @ApiOperation(value = "根据用户id查询收货地址",notes = "根据用户id查询收货地址",httpMethod = "POST")
    @PostMapping("/list")
    public IMOOCJSONResult list(@RequestParam String userId){
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        List<UserAddress> list = addressService.queryAll(userId);
        return IMOOCJSONResult.ok(list);
    }

    /**
     * 用户新增收货地址
     * @param addressBO
     * @return
     */

    @ApiOperation(value = "用户新增收货地址",notes = "用户新增收货地址",httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(@RequestBody AddressBO addressBO){
        IMOOCJSONResult checRes = checkAddress(addressBO);
        if(checRes.getStatus()!=200) {
            return checRes;
        }
        addressService.addNewUserAddress(addressBO);
        return IMOOCJSONResult.ok();
    }



    private IMOOCJSONResult checkAddress(AddressBO addressBO){
        String receiver = addressBO.getReceiver();
        if(StringUtils.isBlank(receiver)){
            return IMOOCJSONResult.errorMsg("收货人不难为空");
        }
        if(receiver.length()>12){
            return IMOOCJSONResult.errorMsg("收货人姓名不能超过12位");
        }
        String mobile = addressBO.getMobile();
        if(StringUtils.isBlank(mobile)){
            return IMOOCJSONResult.errorMsg("收货人手机不能为空，否则会影响收货");
        }
        if(mobile.length()!=11){
            return IMOOCJSONResult.errorMsg("手机号码不对，长度不正确");
        }
        boolean isMobileOk = MobileEmailUtils.checkMobileIsOk(mobile);
        if(!isMobileOk) {
            return IMOOCJSONResult.errorMsg("格式非法");
        }

        String province = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();
        if (StringUtils.isBlank(province) ||
                StringUtils.isBlank(city) ||
                StringUtils.isBlank(district) ||
                StringUtils.isBlank(detail)) {
            return IMOOCJSONResult.errorMsg("收货地址信息不能为空");
        }
        return IMOOCJSONResult.ok();
    }

    /**
     * 用户修改收货地址
     * @param addressBO
     * @return
     */

    @ApiOperation(value = "用户修改收货地址",notes = "用户修改收货地址",httpMethod = "POST")
    @PostMapping("/update")
    public IMOOCJSONResult update(@RequestBody AddressBO addressBO){

        if(StringUtils.isBlank(addressBO.getAddressId())) {
            IMOOCJSONResult.errorMsg("修改地址id不能为空");
        }

        IMOOCJSONResult checRes = checkAddress(addressBO);

        if(checRes.getStatus()!=200) {
            return checRes;
        }

        addressService.updateUserAddress(addressBO);
        return IMOOCJSONResult.ok();
    }

    /**
     * 用户删除收货地址
     * @param addressId
     * @param userId
     * @return
     */

    @ApiOperation(value = "用户删除收货地址",notes = "用户删除收货地址",httpMethod = "POST")
    @PostMapping("/delete")
    public IMOOCJSONResult delete(@RequestParam String addressId,@RequestParam String userId){
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            IMOOCJSONResult.errorMsg("");
        }
        addressService.deleteUserAddress(userId,addressId);
        return IMOOCJSONResult.ok();
    }

    /**
     * 设置默认地址
     * @param addressId
     * @param userId
     * @return
     */

    @ApiOperation(value = "设置默认地址",notes = "设置默认地址",httpMethod = "POST")
    @PostMapping("/setDefault")
    public IMOOCJSONResult setDefault(@RequestParam String userId,@RequestParam String addressId){
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(addressId)) {
            IMOOCJSONResult.errorMsg("参数不能为空");
        }
        addressService.updateUserAddressToBeDefault(userId,addressId);
        return IMOOCJSONResult.ok();
    }
}
