package com.ystar.id.generate.provider.Rpc;

import com.ystar.id.generate.interfaces.IdGenerateRpc;
import com.ystar.id.generate.provider.Service.IdGeneratePoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 分布式 ID 代理对象
 */
@DubboService
public class IdGenerateRpcImpl implements IdGenerateRpc {
    @Resource
    private IdGeneratePoService idGeneratePoService;

    @Override
    public Long getSeqId(Integer id) {
        return idGeneratePoService.getSeqId(id);
    }

    @Override
    public Long getUnSeqId(Integer id) {
        return idGeneratePoService.getUnSeqId(id);
    }
}
