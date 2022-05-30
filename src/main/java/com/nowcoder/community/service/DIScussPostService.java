package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DIScussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> finddiscusspost(int userId,int offset,int limit){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(userId, offset, limit);
        return discussPosts;
    }

    public int finddiscusspostrows(int userId){
        int i = discussPostMapper.selectDiscussPostRows(userId);
        return i;
    }

}
