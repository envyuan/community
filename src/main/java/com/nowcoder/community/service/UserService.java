package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant{

    @Autowired
    private UserMapper usermapper ;
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    //@Autowired
    //private LoginTicketMapper loginTicketMapper;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;
    @Value(("${server.servlet.context-path}"))
    private  String contextPath;

    public User findUserById(int id){
//        User user = usermapper.selectById(id);
        //首先从缓存中获取
        User user = getCache(id);
        if (user == null){
            //缓存中没有，进行初始化
            user = initCache(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        //验证账号
        User user1 = usermapper.selectByName(user.getUsername());
        if (user1 != null){
            map.put("usernameMsg","账号已存在");
            return map;
        }
        //验证邮箱
        user1 = usermapper.selectByEmail(user.getEmail());
        if (user1 != null){
            map.put("emailMsg","邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        usermapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活您的账号",content);

        return map;
    }

    public int activation(int userId, String code){
        User user = usermapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            usermapper.updateStatus(userId,1);
            //清除redis缓存
            clearCache(userId);

            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredsecond){
        Map<String,Object> map = new HashMap<>();
        //验证空值
        if (username == null){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if (password == null){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        //验证用户名和密码是否正确
        User user = usermapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","账号不正确！");
            return map;
        }
        String inputPassword = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(inputPassword)){
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        //验证账号状态
        if (user.getStatus() == 0){
            map.put("usernameMsg","账号未激活！");
            return map;
        }

        //验证通过，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredsecond*1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeader(int userId, String headUrl){
        int rows = usermapper.updateHeader(userId, headUrl);
        //清除redis缓存
        clearCache(userId);
        return rows;
    }

    public Map<String,String> updatePassword(String password, String newPassword){
        Map<String,String> map = new HashMap<>();

        if (password == null){
            map.put("pwdmsg","原始密码不能为空！");
            return map;
        }
        if (newPassword == null){
            map.put("npwdmsg","新密码不能为空！");
            return map;
        }

        User user = hostHolder.getUser();
        //验证原始密码
        String inputPassword = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(inputPassword)){
            //密码不正确
            map.put("pwdmsg","原密码错误！");
            return map;
        }
        //验证通过，更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        int res = usermapper.updatePassword(user.getId(), newPassword);
        if (res == 0){//更新失败
            map.put("pwdmsg","服务器异常，修改失败！");
            return map;
        }
        return map;
    }

    public User findUserByName(String username){
        return usermapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = usermapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
}
