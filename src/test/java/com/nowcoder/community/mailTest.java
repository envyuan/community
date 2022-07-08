package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class mailTest {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;//手动调用thymeleaf模板引擎

    @Test
    public void testTextMail(){
        mailClient.sendMail("yuanenv@163.com","SpringMail Test","Hello SpringMail!");
    }

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","小袁");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("yuanenv@163.com","SpringMail Test",content);
    }
}
