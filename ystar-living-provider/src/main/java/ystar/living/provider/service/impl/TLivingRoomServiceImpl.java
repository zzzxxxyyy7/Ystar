package ystar.living.provider.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ystar.common.VO.PageWrapper;
import com.ystar.common.utils.CommonStatusEnum;
import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ystar.living.dto.LivingRoomReqDTO;
import ystar.living.dto.LivingRoomRespDTO;
import ystar.living.provider.Domain.Mapper.TLivingRoomMapper;
import ystar.living.provider.Domain.Mapper.TLivingRoomRecordMapper;
import ystar.living.provider.Domain.Po.TLivingRoomPo;
import ystar.living.provider.Domain.Po.TLivingRoomRecordPo;
import ystar.living.provider.service.TLivingRoomService;
import org.springframework.stereotype.Service;
import ystart.framework.redis.starter.key.LivingProviderCacheKeyBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author Rhss
* @description 针对表【t_living_room】的数据库操作Service实现
* @createDate 2024-08-05 15:45:50
*/
@Service
public class TLivingRoomServiceImpl extends ServiceImpl<TLivingRoomMapper, TLivingRoomPo>
    implements TLivingRoomService{

    @Resource
    private TLivingRoomMapper tLivingRoomMapper;

    @Resource
    private TLivingRoomRecordMapper tLivingRoomRecordMapper;

    @Resource
    private RedisTemplate<String , Object> redisTemplate;

    @Resource
    private LivingProviderCacheKeyBuilder livingProviderCacheKeyBuilder;

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




