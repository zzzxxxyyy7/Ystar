package ystar.living.provider.Config;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import ystar.living.constants.LivingRoomTypeEnum;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.provider.service.TLivingRoomService;
import ystart.framework.redis.starter.key.LivingProviderCacheKeyBuilder;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 用于定期刷新Redis中缓存的直播间列表的list集合
 */
@Configuration
public class RefreshLivingRoomListJob implements InitializingBean {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshLivingRoomListJob.class);
    
    @Resource
    private TLivingRoomService tLivingRoomService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private LivingProviderCacheKeyBuilder cacheKeyBuilder;
    
    private static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1);

    @Override
    public void afterPropertiesSet() throws Exception {
        //一秒钟刷新一次直播间列表数据
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleWithFixedDelay(new RefreshCacheListJob(), 3000, 3000, TimeUnit.MILLISECONDS);
    }
    
    class RefreshCacheListJob implements Runnable{
        @Override
        public void run() {
            String cacheKey = cacheKeyBuilder.buildRefreshLivingRoomListLock();
            //这把锁等他自动过期
            Boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKey, 1, 1L, TimeUnit.SECONDS);
            if (lockStatus) {
                LOGGER.info("[RefreshLivingRoomListJob] starting  更新数据库中记录的直播间到Redis中去");
                refreshDBTiRedis(LivingRoomTypeEnum.DEFAULT_LIVING_ROOM.getCode());
                refreshDBTiRedis(LivingRoomTypeEnum.PK_LIVING_ROOM.getCode());
                LOGGER.info("[RefreshLivingRoomListJob] end  更新数据库中记录的直播间到Redis中去");
            }
        }
    }

    /**
     * 采用创建一个新集合并重命名替换掉方式
     * 因为在高并发场景下 取出集合 -> 删除集合 -> 一步一步 Push 元素
     * 如果此时恰好有用户在访问集合，会出现数据突然清空，或者数据完整性不一致的情况
     * 这种方式可以改善高并发场景下的用户体验
     * @param type
     */
    private void refreshDBTiRedis(Integer type) {
        String cacheKey = cacheKeyBuilder.buildLivingRoomList(type);
        List<LivingRoomRespDTO> resultList = tLivingRoomService.listAllLivingRoomFromDB(type);
        if (CollectionUtils.isEmpty(resultList)) {
            redisTemplate.unlink(cacheKey);
            return;
        }
        String tempListName = cacheKey + "_temp";
        //需要一行一行push进去，pushAll方法有bug，会添加到一条记录里去
        for (LivingRoomRespDTO livingRoomRespDTO : resultList) {
            redisTemplate.opsForList().rightPush(tempListName, livingRoomRespDTO);
        }
        //直接修改重命名这个list，不要直接对原来的list进行修改，减少阻塞的影响
        redisTemplate.rename(tempListName, cacheKey);
        redisTemplate.unlink(tempListName);
    }
}