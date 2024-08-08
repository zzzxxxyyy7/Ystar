package ystar.live.bank.Domain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ystar.live.bank.Domain.Po.PayProductPO;

@Mapper
public interface PayProductMapper extends BaseMapper<PayProductPO> {
}