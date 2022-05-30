package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper usermapper ;

    @Test
    public void testSelectUser(){
        User user = usermapper.selectById(101);
        System.out.println(user);

        User user1 = usermapper.selectByName("guanyu");
        System.out.println(user1);
    }

    @Test
    public void testInsertUser(){
        User user2 = new User();
        user2.setUsername("test");
        user2.setPassword("123456");
        user2.setEmail("test@qq.com");

        int i = usermapper.insertUser(user2);
        System.out.println(i);
    }

    @Test
    public void testupdateUser(){
        int i = usermapper.updateHeader(150, "http:www.yeway.top/150.png");
        System.out.println(i);
    }


    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public void testdiscusspost(){
        List<DiscussPost> postList = discussPostMapper.selectDiscussPosts(149, 0, 10);

        for (DiscussPost dp: postList
             ) {
            System.out.println(dp);
        }

        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }
}
