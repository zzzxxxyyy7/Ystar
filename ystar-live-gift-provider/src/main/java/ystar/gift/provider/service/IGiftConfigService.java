package ystar.gift.provider.service;

import ystar.gift.dto.GiftConfigDTO;

import java.util.List;

public interface IGiftConfigService {

    /**
     * 根据id查询礼物信息
     */
    GiftConfigDTO getByGiftId(Integer giftId);

    /**
     * 查询所有礼物信息
     */
    List<GiftConfigDTO> queryGiftList();

    /**
     * 新增一个礼物信息
     */
    void insertOne(GiftConfigDTO giftConfigDTO);

    /**
     * 更新礼物信息
     */
    void updateOne(GiftConfigDTO giftConfigDTO);
}