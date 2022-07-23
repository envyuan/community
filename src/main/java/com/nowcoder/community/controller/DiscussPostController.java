package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DIScussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private DIScussPostService diScussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){//未登录
            return CommunityUtil.getJSONString(403,"您尚未登录！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        diScussPostService.addDiscussPost(post);

        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost post = diScussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //发布者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //帖子的赞数
        long postLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("postLikeCount",postLikeCount);
        //帖子点赞状态
        int likeStatus;
        if (hostHolder.getUser() == null){
            likeStatus = 0;
        } else {
            likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        }
        model.addAttribute("likeStatus",likeStatus);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给评论的评论
        //评论列表
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        //评论view object列表
        List<Map<String,Object>> commentVoList = new ArrayList<>();//Vo:view object
        if (commentList != null){
            for (Comment comment :commentList) {
                //评论vo
                Map<String,Object> commentvo = new HashMap<>();
                //评论
                commentvo.put("comment",comment);
                //作者
                commentvo.put("user",userService.findUserById(comment.getUserId()));

                //评论赞的个数
                long commentLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentvo.put("commentLikeCount",commentLikeCount);
                //评论点赞状态
                if (hostHolder.getUser() == null){
                    likeStatus = 0;
                } else {
                    likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                }
                commentvo.put("likeStatus",likeStatus);

                //回复列表
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);//不分页，每页也无限制
                //回复vo列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyList != null){
                    for (Comment reply:replyList) {
                        Map<String,Object> replyvo = new HashMap<>();
                        //回复
                        replyvo.put("reply",reply);
                        //作者
                        replyvo.put("user",userService.findUserById(reply.getUserId()));
                        //回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyvo.put("target",target);

                        //回复赞的个数
                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyvo.put("replyLikeCount",replyLikeCount);
                        //评论点赞状态
                        if (hostHolder.getUser() == null){
                            likeStatus = 0;
                        } else {
                            likeStatus = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        }
                        replyvo.put("likeStatus",likeStatus);

                        replyVoList.add(replyvo);
                    }
                }

                commentvo.put("replys",replyVoList);
                //回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentvo.put("replyCount",replyCount);
                commentVoList.add(commentvo);
            }
        }

        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

}
