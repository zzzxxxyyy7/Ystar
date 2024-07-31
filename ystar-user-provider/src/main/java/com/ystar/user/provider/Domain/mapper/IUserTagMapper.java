package com.ystar.user.provider.Domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ystar.user.provider.Domain.po.UserTagPO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
* @author Rhss
* @description 针对表【t_user_tag_(用户标签记录)】的数据库操作Mapper
* @createDate 2024-07-30 05:27:19
* @Entity com.ystar.user.provider.Domain.po.TUserTag00
*/
@Mapper
public interface IUserTagMapper extends BaseMapper<UserTagPO> {

    /**
     * 必须有用户存在，而且没有这个标签，才能设置成功
     * @param userId
     * @param fieldName
     * @param tag
     * @return
     */
    @Update("update t_user_tag set ${fieldName} = ${fieldName} | #{tag} where user_id = #{userId} and ${fieldName} & #{tag} = 0")
    int setTag(Long userId, String fieldName , long tag);

    @Update("update t_user_tag set ${fieldName} = ${fieldName} &~ #{tag} where user_id = #{userId} and ${fieldName} & #{tag} = #{tag}")
    int cancelTag(Long userId, String fieldName, long tag);

    @Delete("delete from t_user_tag where user_id = #{userId} and tag_info_01 = 0 and tag_info_02 = 0 and tag_info_03 = 0")
    int cancelDoubleAcquire(Long userId);
}