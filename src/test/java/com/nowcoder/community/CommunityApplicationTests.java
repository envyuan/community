package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    void contextLoads() {
    }

    @Test
    public void sensitiveFilterTest(){
        String text = "美国可以赌小博a★吸◎★毒★也可以★开★票★";
        String filted = sensitiveFilter.filter(text);
        System.out.println(filted);
    }

}
