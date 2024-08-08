package ystar.live.bank.service.Impl;

import com.ystar.common.utils.CommonStatusEnum;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.live.bank.Domain.Mapper.YStarCurrencyTradeMapper;
import ystar.live.bank.Domain.Po.YStarCurrencyTradePO;
import ystar.live.bank.service.YStarCurrencyTradeService;

@Service
public class YStarCurrencyTradeServiceImpl implements YStarCurrencyTradeService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(YStarCurrencyTradeServiceImpl.class);
    
    @Resource
    private YStarCurrencyTradeMapper yStarCurrencyTradeMapper;

    /**
     * 礼物流水记录
     * @param userId
     * @param num
     * @param type
     * @return
     */
    @Override
    public boolean insertOne(Long userId, int num, int type) {
        try {
            YStarCurrencyTradePO tradePO = new YStarCurrencyTradePO();
            tradePO.setUserId(userId);
            tradePO.setNum(num);
            tradePO.setType(type);
            tradePO.setStatus(CommonStatusEnum.VALID_STATUS.getCode());
            yStarCurrencyTradeMapper.insert(tradePO);
            return true;
        } catch (Exception e) {
            LOGGER.error("[QiyuCurrencyTradeServiceImpl] insert error, error is:", e);
        }
        return false;
    }
}
