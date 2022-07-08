package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于代替session对象
 */
@Component
public class HostHolder {
    //threadlocal的本质相当于一个容器
    private ThreadLocal<User> tl_user = new ThreadLocal<>();

    public void setUser(User user){
        tl_user.set(user);
    }

    public User getUser(){
        return tl_user.get();
    }

    public void clean(){
        tl_user.remove();
    }
}
