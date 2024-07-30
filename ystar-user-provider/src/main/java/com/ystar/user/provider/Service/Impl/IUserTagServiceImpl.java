package com.ystar.user.provider.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.user.constant.UserTagsEnum;
import com.ystar.user.provider.Domain.mapper.TUserTagMapper;
import com.ystar.user.provider.Domain.po.TUserTagPO;
import com.ystar.user.provider.Service.IUserTagService;
import com.ystar.user.provider.Utils.TagFactors;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import ystart.framework.redis.starter.key.UserProviderCacheKeyBuilder;

import java.util.concurrent.TimeUnit;

@Service
public class IUserTagServiceImpl extends ServiceImpl<TUserTagMapper, TUserTagPO> implements IUserTagService {

    @Resource
    private TUserTagMapper tUserTagMapper;

    @Resource
    private TagFactors tagFactors;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean setTag(Long userId, UserTagsEnum userTagsEnum) {
        // 已经设置好的，直接返回成功
        boolean updateStatus = tUserTagMapper.setTag(userId, userTagsEnum.getFieldName(), userTagsEnum.getTag()) > 0;
        if (updateStatus) return true;

        // 判断是否有记录
        // 先判断，只需要走一次数据库，不需要走俩次缓存，否则就是俩次缓存 + 一次数据库
        TUserTagPO tUserTagPO = tUserTagMapper.selectById(userId);
        // 第一重校验锁，如果不存在，线程往下走
        if (tUserTagPO != null) return false;

        /*
          没有记录，全新插入
          在高并发情况下，多线程进入到这一步，重置 ID 插入会导致大量失败，占用大量数据库连接池
          使用 Redisson 构建分布式锁，针对单个 ID 只允许一个线程发送数据库请求，后续请求不再发送数据库
         */
        String UserTagLockKey = userProviderCacheKeyBuilder.buildUserTagLockKey(userId);
        RLock rLock = redissonClient.getLock(UserTagLockKey);

        try {
            boolean acquireResult = rLock.tryLock(1L, 2L, TimeUnit.SECONDS);
            if (acquireResult) {
                TUserTagPO newTUserTagPO = new TUserTagPO();
                newTUserTagPO.setUserId(userId);
                tUserTagMapper.insert(newTUserTagPO);
                // 必须先保留结果再释放锁
                updateStatus = tUserTagMapper.setTag(userId, userTagsEnum.getFieldName() , userTagsEnum.getTag()) > 0;
            } else return false; // 本身就不是第一个请求了，已经有一个线程发送数据库请求了，后续线程直接失败
        } catch (Exception e) {
            throw new RuntimeException("设置 Tag 获取 RLock 分布式锁失败");
        } finally {
            rLock.unlock();
        }

        // 必须先保留结果再释放锁，如果锁已经释放导致返回结果阻塞后续线程就会不断发送数据库请求
        return updateStatus;
    }

    @Override
    public boolean cancelTag(Long userId, UserTagsEnum userTagsEnum) {
        return tUserTagMapper.cancelTag(userId , userTagsEnum.getFieldName() , userTagsEnum.getTag()) > 0;
    }

    /**
     * 是否拥有指定标签
     * @param userId
     * @param userTagsEnum
     * @return
     */
    @Override
    public boolean containTag(Long userId, UserTagsEnum userTagsEnum) {
        // TODO 改进：同时判断是否拥有多个标签
        TUserTagPO tUserTagPO = tUserTagMapper.selectById(userId);
        if (tUserTagPO == null) return false;

        // 通过标签工程取出对应标签
        String fieldName = userTagsEnum.getFieldName();
        Long tagInfo = tagFactors.getTagByFileName(tUserTagPO , fieldName);

        // 调用包含判断函数
        return isContain(tagInfo , userTagsEnum.getTag());
    }

    public static boolean isContain(Long tagInfo , Long matchTag) {
        return tagInfo != null && matchTag != null && (tagInfo & matchTag) == matchTag;
    }
}
