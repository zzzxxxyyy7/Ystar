package com.ystar.id.generate.interfaces;

/**
 * 分布式 ID 提供接口
 */
public interface IdGenerateRpc {

    /**
     * 获取有序 ID
     * @param id
     * @return
     */
    Long getSeqId(Integer id);

    /**
     * 获取无序 ID
     * @param id
     * @return
     */
    Long getUnSeqId(Integer id);
}
