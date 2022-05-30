package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DIScussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DIScussPostService diScussPostService;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index" ,method = RequestMethod.GET)
    public String indexPage(Model model, Page page){
        //方法调用前，springmvc会自动实例化model和page，并将page数据注入model，
        //所以在thymeleaf中可以直接访问page对象中的数据
        page.setRows(diScussPostService.finddiscusspostrows(0));
        page.setPath("/index");
        List<DiscussPost> discusspostlist = diScussPostService.finddiscusspost(0, page.getOffset(), page.getLimit());
       List<Map<String,Object>> UserPostList = new ArrayList<>();
       if (discusspostlist != null){
           for (DiscussPost post:discusspostlist) {
                Map<String,Object> dpmap = new HashMap<>();
                dpmap.put("post",post);
               User user = userService.findUserById(post.getUserId());
               dpmap.put("user",user);
               UserPostList.add(dpmap);
           }
       }

        model.addAttribute("UserPostList",UserPostList);
        return "/index";
    }
}