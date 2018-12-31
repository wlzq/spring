package org.wl.test.demo.service.impl;

import org.wl.test.demo.entity.UserInfo;
import org.wl.test.demo.service.IUserService;
import org.wl.test.spring.ioc.annotation.Service;

/**
 * @author wl
 * @date 2018/12/22 18:00
 * @description
 */
@Service
public class UserService2 implements IUserService {
    @Override
    public void saveUser(UserInfo user) {
        System.out.println("实现类2 保存用户信息到数据库 " + user.toString());
    }
}
