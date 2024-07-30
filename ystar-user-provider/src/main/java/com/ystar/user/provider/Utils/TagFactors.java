package com.ystar.user.provider.Utils;

import com.ystar.user.dto.UserTagDTO;
import org.springframework.stereotype.Component;

import static com.ystar.user.constant.UserTagFieldNameConstants.*;

@Component
public class TagFactors{

    public Long getTagByFileName(UserTagDTO userTagDTO, String fieldName) {
        return switch (fieldName) {
            case TAT_INFO_01 -> userTagDTO.getTagInfo01();
            case TAT_INFO_02 -> userTagDTO.getTagInfo02();
            case TAT_INFO_03 -> userTagDTO.getTagInfo03();
            default -> null;
        };
    }

}
