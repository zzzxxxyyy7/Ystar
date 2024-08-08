package ystar.gift.provider.service;

import ystar.gift.dto.GiftRecordDTO;

public interface IGiftRecordService {

    /**
     * 插入一条送礼记录
     */
    void insertOne(GiftRecordDTO giftRecordDTO);
}