package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated //使用redis优化了此功能，添加此注解后变为不推荐使用的
public interface LoginTicketMapper {
    //采用注解方式，不用写mapper.xml文件。一条SQL语句可以用 "", 断开，注解会将其自动拼接
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            //此判断没有意义，只是演示条件判断的写法
          "<script>",
          "update login_ticket set status=#{status} where ticket=#{ticket} ",
          "<if test=\"ticket!=null\"> and 1=1 </if>",
          "</script>"
    })
    int updateStatus(String ticket, int status);
}
