package ystar.msg.provider.Domain.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import ystar.msg.provider.Domain.Po.SmsPo;

/**
* @author Rhss
* @description 针对表【t_sms】的数据库操作Mapper
* @createDate 2024-07-31 17:49:20
* @Entity generator.domain.SmsPo
*/
@Mapper
public interface ISmsMapper extends BaseMapper<SmsPo> {

}



