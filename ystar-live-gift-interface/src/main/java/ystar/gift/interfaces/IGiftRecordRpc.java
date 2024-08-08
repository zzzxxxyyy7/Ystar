package ystar.gift.interfaces;

import ystar.gift.dto.GiftRecordDTO;

import java.util.List;

public interface IGiftRecordRpc {

    /**
     * 插入一条送礼记录
     */
    void insertOne(GiftRecordDTO giftRecordDTO);
}
