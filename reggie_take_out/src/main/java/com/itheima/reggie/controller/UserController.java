package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送验证码
     *
     * @param session
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpSession session, @RequestBody User user) {
        //获取手机号
        String phone = user.getPhone();
        //生成验证码
        if (StringUtils.hasText(phone)) {
            String code = ValidateCodeUtils.generateValidateCode4String(6).toString();
            log.info("瑞吉外卖验证码code为{}", code);
            //调用阿里云短信服务api完成发送短信
            //SMSUtils.sendMessage("瑞吉外卖",phone,validateCode4String);
            //保存验证码
            session.setAttribute(phone, code);
            return R.success("发送验证码成功");
        }
        return R.error("发送验证码失败");

    }

    /**
     * 登录
     *
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //与session中的验证码进行比对
        if (session.getAttribute(phone) != null && session.getAttribute(phone).equals(code)) {
            //成功 登录 判断是否为新用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //新用户自动插入注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登陆失败！");
    }

    /**
     * 用户登出
     *
     * @param httpSession
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpSession httpSession) {
        httpSession.removeAttribute("user");
        return R.success("登出成功");
    }

}