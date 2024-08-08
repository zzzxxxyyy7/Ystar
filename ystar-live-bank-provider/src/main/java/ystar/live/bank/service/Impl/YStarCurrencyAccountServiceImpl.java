package ystar.live.bank.service.Impl;

import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import ystar.live.bank.Domain.Mapper.QiyuCurrencyAccountMapper;
import ystar.live.bank.Domain.Po.YStarCurrencyAccountPO;
import ystar.live.bank.dto.YStarCurrencyAccountDTO;
import ystar.live.bank.service.YStarCurrencyAccountService;

@Service
public class YStarCurrencyAccountServiceImpl implements YStarCurrencyAccountService {

    @Resource
    private QiyuCurrencyAccountMapper qiyuCurrencyAccountMapper;

    @Override
    public boolean insertOne(Long userId) {
        try {
            YStarCurrencyAccountPO accountPO = new YStarCurrencyAccountPO();
            accountPO.setUserId(userId);
            qiyuCurrencyAccountMapper.insert(accountPO);
            return true;
        } catch (Exception e) {
            //有异常但是不抛出，只为了避免重复创建相同userId的账户
        }
        return false;
    }

    @Override
    public void incr(Long userId, int num) {
        qiyuCurrencyAccountMapper.incr(userId, num);
    }

    @Override
    public void decr(Long userId, int num) {
        qiyuCurrencyAccountMapper.decr(userId, num);
    }

    @Override
    public YStarCurrencyAccountDTO getByUserId(Long userId) {
        return ConvertBeanUtils.convert(qiyuCurrencyAccountMapper.selectById(userId), YStarCurrencyAccountDTO.class);
    }
}