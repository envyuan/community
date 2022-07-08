package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.sun.deploy.net.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaproducer;

    @Value("${server.servlet.context-path}")
    private String contextpath;

    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path ="/kaptcha",method = RequestMethod.GET)
    public void getVerifyCode(HttpServletResponse response, HttpSession session){
        String text = kaptchaproducer.createText();
        BufferedImage image = kaptchaproducer.createImage(text);
        //验证码存入session
        session.setAttribute("kaptcha",text);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> regMsgMap = userService.register(user);
        if (regMsgMap == null || regMsgMap.isEmpty()){//注册成功的情况
            model.addAttribute("msg",
                    "注册成功！我们已向您的邮箱发送了一封激活邮件，请尽快验证！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",regMsgMap.get("usernameMsg"));
            model.addAttribute("passwordMsg",regMsgMap.get("passwordMsg"));
            model.addAttribute("emailMsg",regMsgMap.get("emailMsg"));
            return "/site/register";
        }
    }

    //http://localhost:8080/community/activation/101/hu74lr6ztc48
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int actnum = userService.activation(userId, code);
        if (actnum == ACTIVATION_SUCCESS){
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if(actnum == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该账号已激活！");
            model.addAttribute("target","/index");
        } else{
            model.addAttribute("msg", "激活失败，您的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path="/login", method = RequestMethod.POST)
    public String login(String username, String password, String verifycode, boolean rememberme,
                        Model model, HttpSession session, HttpServletResponse response){
        //验证验证码
        String inputcode = (String) session.getAttribute("kaptcha");
        if (StringUtils.isBlank(inputcode) || StringUtils.isBlank(verifycode) || !inputcode.equalsIgnoreCase(verifycode)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = rememberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextpath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }@RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }



}