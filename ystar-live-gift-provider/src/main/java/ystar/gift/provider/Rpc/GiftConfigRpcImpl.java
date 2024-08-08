package ystar.gift.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.gift.dto.GiftConfigDTO;
import ystar.gift.interfaces.IGiftConfigRpc;
import ystar.gift.provider.service.IGiftConfigService;

import java.util.List;

@DubboService
public class GiftConfigRpcImpl implements IGiftConfigRpc {
    
    @Resource
    private IGiftConfigService giftService;

    @Override
    public GiftConfigDTO getByGiftId(Integer giftId) {
        return giftService.getByGiftId(giftId);
    }

    @Override
    public List<GiftConfigDTO> queryGiftList() {
        return giftService.queryGiftList();
    }

    @Override
    public void insertOne(GiftConfigDTO giftConfigDTO) {
        giftService.insertOne(giftConfigDTO);
    }

    @Override
    public void updateOne(GiftConfigDTO giftConfigDTO) {
        giftService.updateOne(giftConfigDTO);
    }
}