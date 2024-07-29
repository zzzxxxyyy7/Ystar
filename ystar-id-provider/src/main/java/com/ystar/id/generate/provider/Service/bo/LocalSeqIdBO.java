package com.ystar.id.generate.provider.Service.bo;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 分布式唯一 ID 本地内存对象
 */
@Data
public class LocalSeqIdBO {

    /**
     * 业务id
     */
    private int id;

    /**
     * 本地内存记录的当前值
     */
    private AtomicLong currentNum;

    /**
     * 记录 ID 段的起始位置
     */
    private Long currentStart;

    /**
     * 记录 ID 段的结束位置
     */
    private Long nextThreshold;
}
