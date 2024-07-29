package com.ystar.id.generate.provider.Domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ystar.id.generate.provider.Domain.po.IdGeneratePo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* @author Rhss
* @description 针对表【ystar_id_generate_config】的数据库操作Mapper
* @createDate 2024-07-29 19:22:43
* @Entity generator.domain.YstarIdGenerateConfig
*/
@Mapper
public interface YstarIdGenerateConfigMapper extends BaseMapper<IdGeneratePo> {

    @Update("update ystar_id_generate_config set next_threshold=next_threshold + step," +
    "current_start= current_start + step, version = version + 1 where id = #{id} and version = #{version}")
    int updateNewIdCountAndVersion(@Param("id") int id , @Param("version") int version);

    @Select("select * from ystar_id_generate_config")
    List<IdGeneratePo> selectAll();
}




