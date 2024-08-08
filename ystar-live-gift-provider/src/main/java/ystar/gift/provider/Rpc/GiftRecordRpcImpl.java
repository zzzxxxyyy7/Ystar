package ystar.gift.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.gift.dto.GiftRecordDTO;
import ystar.gift.interfaces.IGiftRecordRpc;
import ystar.gift.provider.service.IGiftRecordService;

@DubboService
public class GiftRecordRpcImpl implements IGiftRecordRpc {
    
    @Resource
    private IGiftRecordService giftRecordService;

    /**
     * 新增一条送礼记录
     * @param giftRecordDTO
     */
    @Override
    public void insertOne(GiftRecordDTO giftRecordDTO) {
        giftRecordService.insertOne(giftRecordDTO);
    }
}