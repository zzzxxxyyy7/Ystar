package ystar.gift.provider.Domain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ystar.gift.provider.Domain.Po.GiftRecordPO;

@Mapper
public interface GiftRecordMapper extends BaseMapper<GiftRecordPO> {
}