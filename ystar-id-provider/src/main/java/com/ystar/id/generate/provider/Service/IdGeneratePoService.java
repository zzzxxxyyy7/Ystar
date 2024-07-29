package com.ystar.id.generate.provider.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ystar.id.generate.provider.Domain.po.IdGeneratePo;

/**
* @author Rhss
* @description 针对表【ystar_id_generate_config】的数据库操作Service
* @createDate 2024-07-29 19:22:43
*/
public interface IdGeneratePoService extends IService<IdGeneratePo> {

    Long getSeqId(Integer id);

    Long getUnSeqId(Integer id);
}
