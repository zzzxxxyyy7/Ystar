package com.ystar.user.provider.Domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ystar.user.provider.Domain.po.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IUserMapper extends BaseMapper<UserPO> {

}