package org.wl.test.demo.controller;

import org.wl.test.demo.entity.UserInfo;
import org.wl.test.demo.service.IUserService;
import org.wl.test.demo.service.impl.UserService;
import org.wl.test.spring.ioc.annotation.Autowired;
import org.wl.test.spring.ioc.annotation.Controller;
import org.wl.test.spring.ioc.annotation.Qualifier;
import org.wl.test.spring.mvc.ModelAndView;
import org.wl.test.spring.mvc.annotation.RequestMapping;
import org.wl.test.spring.mvc.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wl
 * @date 2018/12/22 11:22
 * @description
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 接口可能会有多个实现类，所以在注入接口时必须用Autowired或者Qualifier指定具体的实现类
     */
    @Autowired
    @Qualifier("userService2")
    private IUserService userService2;

    public void save(){
        UserInfo user = new UserInfo();
        userService.saveUser(user);
        userService2.saveUser(user);
    }

    @RequestMapping("/query")
    public ModelAndView query(@RequestParam("username") String username, HttpServletRequest request,
                              HttpServletResponse response, @RequestParam("password") String password) {
        System.out.println("执行query方法，参数：" + username+" "+ password);
        ModelAndView mv = new ModelAndView("/WEB-INF/hello.jsp");
        UserInfo user = new UserInfo();
        user.setId(1);
        user.setUsername(username);
        user.setPassword(password);
        mv.addAttribute("user", user);
        return mv;
    }

}
