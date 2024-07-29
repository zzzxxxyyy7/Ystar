package com.ystar.id.generate.provider.Service.bo;

import lombok.Data;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 分布式唯一 ID 本地内存对象
 */
@Data
public class LocalUnSeqIdBO {

    /**
     * 业务id
     */
    private int id;

    /**
     * 将提前存储好的无序 ID 放入队列
     */
    private ConcurrentLinkedQueue<Long> idQueue;

    /**
     * 记录 ID 段的起始位置
     */
    private Long currentStart;

    /**
     * 记录 ID 段的结束位置
     */
    private Long nextThreshold;
}
