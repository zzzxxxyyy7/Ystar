package ystar.live.bank.service.Impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ystar.common.utils.CommonStatusEnum;
import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ystar.live.bank.Domain.Mapper.PayProductMapper;
import ystar.live.bank.Domain.Po.PayProductPO;
import ystar.live.bank.dto.PayProductDTO;
import ystar.live.bank.service.IPayProductService;
import ystart.framework.redis.starter.key.BankProviderCacheKeyBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class IPayProductServiceImpl implements IPayProductService {

    @Resource
    private PayProductMapper payProductMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private BankProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public List<PayProductDTO> products(Integer type) {
        String cacheKey = cacheKeyBuilder.buildPayProductCache(type);
        List<PayProductDTO> cacheList = redisTemplate.opsForList().range(cacheKey, 0, 30).stream().map(x -> (PayProductDTO) x).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(cacheList)) {
            if (cacheList.get(0).getId() == null) {
                return Collections.emptyList();
            }
            return cacheList;
        }

        LambdaQueryWrapper<PayProductPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayProductPO::getType, type);
        queryWrapper.eq(PayProductPO::getValidStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.orderByDesc(PayProductPO::getPrice);
        List<PayProductDTO> payProductDTOS = ConvertBeanUtils.convertList(payProductMapper.selectList(queryWrapper), PayProductDTO.class);

        if (CollectionUtils.isEmpty(payProductDTOS)) {
            redisTemplate.opsForList().leftPush(cacheKey, new PayProductDTO());
            redisTemplate.expire(cacheKey, 1L, TimeUnit.MINUTES);
            return Collections.emptyList();
        }

        // List类型的putAll放入集合有bug，需要转换为数组
        redisTemplate.opsForList().leftPushAll(cacheKey, payProductDTOS.toArray());
        redisTemplate.expire(cacheKey, 30L, TimeUnit.MINUTES);
        return payProductDTOS;
    }
}