package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

//cookie相关工具
public class CookieUtil {
    //从cookie中获取值
    public static String getValue(HttpServletRequest request,String name){
        if (request == null || name == null){
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null){
            for (Cookie cook: cookies) {
                if (cook.getName().equals(name)){
                    return cook.getValue();
                }
            }
        }
        return null;
    }
}
