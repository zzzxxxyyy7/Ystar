package ystar.living.provider.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.VO.PageWrapper;
import com.ystar.common.utils.CommonStatusEnum;
import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ystar.im.Domain.Dto.ImMsgBody;
import ystar.im.constant.AppIdEnum;
import ystar.im.core.server.dto.ImOfflineDto;
import ystar.im.core.server.dto.ImOnlineDto;
import ystar.im.router.Constants.ImMsgBizCodeEnum;
import ystar.im.router.interfaces.ImRouterRpc;
import ystar.living.dto.LivingPkRespDTO;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.provider.Domain.Mapper.TLivingRoomMapper;
import ystar.living.provider.Domain.Mapper.TLivingRoomRecordMapper;
import ystar.living.provider.Domain.Po.TLivingRoomPo;
import ystar.living.provider.Domain.Po.TLivingRoomRecordPo;
import ystar.living.provider.service.TLivingRoomService;
import org.springframework.stereotype.Service;
import ystart.framework.redis.starter.key.LivingProviderCacheKeyBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
* @author Rhss
* @description 针对表【t_living_room】的数据库操作Service实现
* @createDate 2024-08-05 15:45:50
*/
@Service
public class TLivingRoomServiceImpl extends ServiceImpl<TLivingRoomMapper, TLivingRoomPo>
    implements TLivingRoomService{

    private static final Logger LOGGER = LoggerFactory.getLogger(TLivingRoomServiceImpl.class);

    @Resource
    private TLivingRoomMapper tLivingRoomMapper;

    @Resource
    private TLivingRoomRecordMapper tLivingRoomRecordMapper;

    @Resource
    private RedisTemplate<String , Object> redisTemplate;

    @Resource
    private LivingProviderCacheKeyBuilder livingProviderCacheKeyBuilder;

    @Resource
    private LivingProviderCacheKeyBuilder cacheKeyBuilder;

    @Resource
    private ImRouterRpc imRouterRpc;

    /**
     * 接入 PK
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public LivingPkRespDTO onlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        LivingRoomRespDTO currentLivingRoom = this.queryByRoomId(livingRoomReqDTO.getRoomId());
        LivingPkRespDTO respDTO = new LivingPkRespDTO();
        respDTO.setOnlineStatus(false);

        if (currentLivingRoom.getAnchorId().equals(livingRoomReqDTO.getPkObjId())) {
            respDTO.setMsg("主播不可以连线参与PK");
            return respDTO;
        }

        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(livingRoomReqDTO.getRoomId());

        // 使用setIfAbsent防止被后来者覆盖
        Boolean tryOnline = redisTemplate.opsForValue().setIfAbsent(cacheKey, livingRoomReqDTO.getPkObjId(), 12L, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(tryOnline)) {
            // 通知直播间所有人，有人上线PK了
            List<Long> userIdList = this.queryUserIdsByRoomId(livingRoomReqDTO);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("pkObjId", livingRoomReqDTO.getPkObjId());
            jsonObject.put("pkObjAvatar", "../svga/img/爱心.png");
            new Thread(() -> this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.LIVING_ROOM_PK_ONLINE.getCode(), jsonObject));
            respDTO.setMsg("连线成功");
            respDTO.setOnlineStatus(true);
            return respDTO;
        }

        respDTO.setMsg("目前有人在线，请稍后再试");
        return respDTO;
    }

    /**
     * 除了礼物服务需要批量通知、直播间连线 PK 服务也需要批量通知
     * @param userIdList
     * @param bizCode
     * @param jsonObject
     */
    private void batchSendImMsg(List<Long> userIdList, Integer bizCode, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.YStar_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(bizCode);
            imMsgBody.setData(jsonObject.toJSONString());
            imMsgBody.setUserId(userId);
            return imMsgBody;
        }).collect(Collectors.toList());
        imRouterRpc.batchSendMsg(imMsgBodies);
    }

    /**
     * 断开 PK
     * @param livingRoomReqDTO
     * @return
     */
    @Override
    public boolean offlinePk(LivingRoomReqDTO livingRoomReqDTO) {
        Integer roomId = livingRoomReqDTO.getRoomId();
        Long pkObjId = this.queryOnlinePkUserId(roomId);

        // 如果他是pkObjId本人，才删除
        if (!livingRoomReqDTO.getPkObjId().equals(pkObjId)) {
            System.out.println("删除失败");
            return false;
        }

        System.out.println("删除成功");
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(roomId);

        //删除PK进度条值缓存
        redisTemplate.delete("ystar-live-gift-provider:living_pk_key:" + roomId);

        //删除PK直播间pkObjId缓存
        return Boolean.TRUE.equals(redisTemplate.delete(cacheKey));
    }

    /**
     * 查询直播间发起 PK 的 userId
     * @param roomId
     * @return
     */
    @Override
    public Long queryOnlinePkUserId(Integer roomId) {
        String cacheKey = cacheKeyBuilder.buildLivingOnlinePk(roomId);
        return (Long) redisTemplate.opsForValue().get(cacheKey);
    }

    @Override
    public void userOnlineHandler(ImOnlineDto imOnlineDto) {
        Long userId = imOnlineDto.getUserId();
        Integer roomId = imOnlineDto.getRoomId();
        Integer appId = imOnlineDto.getAppId();

        /**
         * 默认基于直播间存在的最大时间，存入用户 ID 和直播间 ID的绑定关系
         */
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomUserSet(roomId , appId);

        // 把用户存入队列
        redisTemplate.opsForSet().add(cacheKey , userId);
        redisTemplate.expire(cacheKey , 12 , TimeUnit.HOURS);
        LOGGER.info("用户 {} 已经成功登录直播间：{}" , userId , roomId);
    }

    @Override
    public void userOfflineHandler(ImOfflineDto imOfflineDto) {
        Long userId = imOfflineDto.getUserId();
        Integer roomId = imOfflineDto.getRoomId();
        Integer appId = imOfflineDto.getAppId();
        /**
         * 退出时，移除用户和直播间的绑定关系
         */
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomUserSet(roomId , appId);
        redisTemplate.opsForSet().remove(cacheKey , userId);
        LOGGER.info("用户 {} 已经成功退出直播间：{}" , userId , roomId);

        // 新增PK直播间 下线调用
        LivingRoomReqDTO reqDTO = new LivingRoomReqDTO();
        reqDTO.setRoomId(roomId);
        reqDTO.setPkObjId(userId);

        // TODO 暂时写死，断开 Channel 连接的时候断开 PK 连接
        this.offlinePk(reqDTO);
    }

    @Override
    public List<Long> queryUserIdsByRoomId(LivingRoomReqDTO livingRoomReqDTO) {
        Integer roomId = livingRoomReqDTO.getRoomId();
        Integer appId = livingRoomReqDTO.getAppId();
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomUserSet(roomId, appId);
        // 使用 scan 命令 分批查询数据，否则 set 元素太多容易造成 redis 和网络阻塞(scan会自动分成多次请求去执行)
        // scan 递增式遍历
        List<Long> userIdList = new ArrayList<>();
        Set<Object> members = redisTemplate.opsForSet().members(cacheKey);

        if (members != null) {
            members.forEach(x -> userIdList.add((Long) x));
        }

        return userIdList;
    }

    @Override
    public PageWrapper<LivingRoomRespDTO> list(LivingRoomReqDTO livingRoomReqDTO) {
        // 因为平台 同时会开直播的人数不算多，属于读多写少场景，所以将其缓存进Redis进行提速
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomList(livingRoomReqDTO.getType());
        int page = livingRoomReqDTO.getPage();
        int pageSize = livingRoomReqDTO.getPageSize();
        Long total = redisTemplate.opsForList().size(cacheKey);

        List<Object> resultList = redisTemplate.opsForList().range(cacheKey, (long) (page - 1) * pageSize, (long) page * pageSize);
        PageWrapper<LivingRoomRespDTO> pageWrapper = new PageWrapper<>();

        if (CollectionUtils.isEmpty(resultList)) {
            pageWrapper.setList(Collections.emptyList());
            pageWrapper.setHasNext(false);
        } else {
            pageWrapper.setList(ConvertBeanUtils.convertList(resultList, LivingRoomRespDTO.class));
            pageWrapper.setHasNext((long) page * pageSize < total);
        }

        return pageWrapper;
    }

    @Override
    public List<LivingRoomRespDTO> listAllLivingRoomFromDB(Integer type) {
        LambdaQueryWrapper<TLivingRoomPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TLivingRoomPo::getType, type);
        queryWrapper.eq(TLivingRoomPo::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        //按照时间倒序展示
        queryWrapper.orderByDesc(TLivingRoomPo::getStart_time);
        //为性能做保证，只查询1000个
        queryWrapper.last("limit 1000");
        return ConvertBeanUtils.convertList(tLivingRoomMapper.selectList(queryWrapper), LivingRoomRespDTO.class);
    }

    @Override
    public Integer startLivingRoom(LivingRoomReqDTO livingRoomReqDTO) {
        TLivingRoomPo livingRoomPO = ConvertBeanUtils.convert(livingRoomReqDTO , TLivingRoomPo.class);
        livingRoomPO.setStatus(CommonStatusEnum.VALID_STATUS.getCode());
        livingRoomPO.setStart_time(new Date());
        tLivingRoomMapper.insert(livingRoomPO);
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomObj(livingRoomPO.getId());

        // 防止之前有空值缓存，这里做移除操作
        redisTemplate.delete(cacheKey);
        return livingRoomPO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeLiving(LivingRoomReqDTO livingRoomReqDTO) {
        TLivingRoomPo livingRoomPO = tLivingRoomMapper.selectById(livingRoomReqDTO.getRoomId());
        if (livingRoomPO == null) return false;

        if (!livingRoomReqDTO.getAnchorId().equals(livingRoomPO.getAnchorId())) return false;

        TLivingRoomRecordPo livingRoomRecordPO = BeanUtil.copyProperties(livingRoomPO, TLivingRoomRecordPo.class);
        livingRoomRecordPO.setEnd_time(new Date());
        livingRoomRecordPO.setStatus(CommonStatusEnum.INVALID_STATUS.getCode());
        tLivingRoomRecordMapper.insert(livingRoomRecordPO);
        tLivingRoomMapper.deleteById(livingRoomPO.getId());

        // 移除掉直播间 Cache
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomObj(livingRoomReqDTO.getRoomId());
        redisTemplate.delete(cacheKey);
        return true;
    }

    /**
     * 判断当前直播间是否在线、有效
     * @param roomId
     * @return
     */
    @Override
    public LivingRoomRespDTO queryByRoomId(Integer roomId) {
        String cacheKey = livingProviderCacheKeyBuilder.buildLivingRoomObj(roomId);
        LivingRoomRespDTO queryResult = (LivingRoomRespDTO) redisTemplate.opsForValue().get(cacheKey);

        if (queryResult != null) {
            // 空值缓存
            if (queryResult.getId() == null) {
                return null;
            }
            return queryResult;
        }

        // 查询数据库
        LambdaQueryWrapper<TLivingRoomPo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TLivingRoomPo::getId, roomId);
        queryWrapper.eq(TLivingRoomPo::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        queryResult = BeanUtil.copyProperties(tLivingRoomMapper.selectOne(queryWrapper), LivingRoomRespDTO.class);

        if (queryResult == null) {
            // 防止缓存穿透，缓存空对象
            redisTemplate.opsForValue().set(cacheKey, new LivingRoomRespDTO(), 1L, TimeUnit.MINUTES);
            return null;
        }

        redisTemplate.opsForValue().set(cacheKey, queryResult, 30, TimeUnit.MINUTES);
        return queryResult;
    }
}




