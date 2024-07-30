package com.ystar.user.provider.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ystar.user.constant.UserTagsEnum;
import com.ystar.user.provider.Domain.po.TUserTagPO;

public interface IUserTagService extends IService<TUserTagPO> {
    /**
     * 设置标签
     *
     * @param userId
     * @param userTagsEnum
     * @return
     */
    boolean setTag(Long userId, UserTagsEnum userTagsEnum);

    /**
     * 取消标签
     *
     * @param userId
     * @param userTagsEnum
     * @return
     */
    boolean cancelTag(Long userId, UserTagsEnum userTagsEnum);

    /**
     * 是否包含某个标签
     *
     * @param userId
     * @param userTagsEnum
     * @return
     */
    boolean containTag(Long userId, UserTagsEnum userTagsEnum);
}
