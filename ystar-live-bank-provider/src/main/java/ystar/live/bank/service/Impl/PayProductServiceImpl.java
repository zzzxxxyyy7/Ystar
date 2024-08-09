package ystar.live.bank.service.Impl;

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

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PayProductServiceImpl implements IPayProductService {

    @Resource
    private PayProductMapper payProductMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private BankProviderCacheKeyBuilder cacheKeyBuilder;

    @Override
    public List<PayProductDTO> products(Integer type) {
        // TODO 后续补齐
        return null;
    }

    @Override
    public PayProductDTO getByProductId(Integer productId) {
        String cacheKey = cacheKeyBuilder.buildPayProductItemCache(productId);
        PayProductDTO payProductDTO = (PayProductDTO) redisTemplate.opsForValue().get(cacheKey);
        if (payProductDTO != null) {
            // 缓存穿透解决方案
            if (payProductDTO.getId() == null) {
                return null;
            }
            return payProductDTO;
        }
        LambdaQueryWrapper<PayProductPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayProductPO::getId, productId);
        queryWrapper.eq(PayProductPO::getValidStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        payProductDTO = ConvertBeanUtils.convert(payProductMapper.selectOne(queryWrapper), PayProductDTO.class);
        if (payProductDTO == null) {
            redisTemplate.opsForValue().set(cacheKey, new PayProductDTO(), 1L, TimeUnit.MINUTES);
            return null;
        }
        redisTemplate.opsForValue().set(cacheKey, payProductDTO, 30L, TimeUnit.MINUTES);
        return payProductDTO;
    }
}