package com.ystar.user.provider.Utils;

import com.ystar.user.provider.Domain.po.TUserTagPO;
import org.springframework.stereotype.Component;

import static com.ystar.user.constant.UserTagFieldNameConstants.*;

@Component
public class TagFactors{

    public Long getTagByFileName(TUserTagPO tUserTagPO, String fieldName) {
        return switch (fieldName) {
            case TAT_INFO_01 -> tUserTagPO.getTagInfo01();
            case TAT_INFO_02 -> tUserTagPO.getTagInfo02();
            case TAT_INFO_03 -> tUserTagPO.getTagInfo03();
            default -> null;
        };
    }

}
