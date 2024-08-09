package ystar.live.bank.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ystar.common.utils.CommonStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import ystar.live.bank.Domain.Mapper.PayTopicMapper;
import ystar.live.bank.Domain.Po.PayTopicPO;
import ystar.live.bank.service.IPayTopicService;

/**
 * 查询支付主题实现类
 */
@Service
public class PayTopicServiceImpl implements IPayTopicService {

    @Resource
    private PayTopicMapper payTopicMapper;

    @Override
    public PayTopicPO getByCode(Integer code) {
        LambdaQueryWrapper<PayTopicPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayTopicPO::getBizCode, code);
        queryWrapper.eq(PayTopicPO::getStatus, CommonStatusEnum.VALID_STATUS.getCode());
        queryWrapper.last("limit 1");
        return payTopicMapper.selectOne(queryWrapper);
    }
}