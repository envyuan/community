package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /*
    //点赞(4.3节代码)
    public void like(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        Boolean ismember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (ismember){ //已点过赞
            redisTemplate.opsForSet().remove(entityLikeKey,userId);//取消此赞
        }else{ //未点过
        redisTemplate.opsForSet().add(entityLikeKey,userId);
        }
    }
     */

    //4.4节，添加事务对其进行重构

    public void like(int userId,int entityType,int entityId,int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                Boolean ismember = operations.opsForSet().isMember(entityLikeKey, userId);//redis查询要放在事务之外

                operations.multi();//开启事务

                if (ismember){ //已点过赞
                    operations.opsForSet().remove(entityLikeKey,userId);//取消此赞
                    operations.opsForValue().decrement(userLikeKey);
                }else{ //未点过
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    //查询某实体点赞数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0;//1已赞，0未赞
    }

    //查询某个用户获得的赞
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);

        return count == null ? 0 : count.intValue();
    }

}
