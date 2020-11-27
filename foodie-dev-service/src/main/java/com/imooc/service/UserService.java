package com.imooc.service;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.UserBO;

public interface UserService {

    //判断用户是否存在
    public boolean queryUsernameIsExist(String username);

    //注册或者创建用户
    public Users createUser(UserBO userBO);

    // 用户安全退出
    public Users queryUserForLogin(String username,String password);

}
