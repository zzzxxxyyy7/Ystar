package ystar.live.bank.Domain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ystar.live.bank.Domain.Po.PayOrderPO;

@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrderPO> {
}