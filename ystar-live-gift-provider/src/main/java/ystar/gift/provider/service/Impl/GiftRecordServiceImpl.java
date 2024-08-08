package ystar.gift.provider.service.Impl;

import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import ystar.gift.dto.GiftRecordDTO;
import ystar.gift.provider.Domain.Mapper.GiftRecordMapper;
import ystar.gift.provider.Domain.Po.GiftRecordPO;
import ystar.gift.provider.service.IGiftRecordService;

@Service
public class GiftRecordServiceImpl implements IGiftRecordService {
    
    @Resource
    private GiftRecordMapper giftRecordMapper;
    
    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        GiftRecordPO giftRecordPO = ConvertBeanUtils.convert(giftRecordDTO, GiftRecordPO.class);
        giftRecordMapper.insert(giftRecordPO);
    }
}