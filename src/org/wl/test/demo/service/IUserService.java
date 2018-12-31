package org.wl.test.demo.service;

import org.wl.test.demo.entity.UserInfo;

/**
 * @author wl
 * @date 2018/12/22 11:16
 * @description
 */
public interface IUserService {

    /**
     * 保存用户信息
     * @param user
     */
    void saveUser(UserInfo user);

}
