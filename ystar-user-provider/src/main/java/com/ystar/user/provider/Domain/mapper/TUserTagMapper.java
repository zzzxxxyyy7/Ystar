package com.ystar.user.provider.Domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ystar.user.provider.Domain.po.TUserTagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
* @author Rhss
* @description 针对表【t_user_tag_(用户标签记录)】的数据库操作Mapper
* @createDate 2024-07-30 05:27:19
* @Entity com.ystar.user.provider.Domain.po.TUserTag00
*/
@Mapper
public interface TUserTagMapper extends BaseMapper<TUserTagPO> {

    @Update("update t_user_tag set ${fieldName} = ${fieldName} | #{tag} where user_id = #{userId} and ${fieldName} & #{tag} = 0")
    int setTag(Long userId, String fieldName , long tag);

    @Update("update t_user_tag set ${fieldName} = ${fieldName} &~ #{tag} where user_id = #{userId} and ${fieldName} & #{tag} = #{tag}")
    int cancelTag(Long userId, String fieldName, long tag);
}