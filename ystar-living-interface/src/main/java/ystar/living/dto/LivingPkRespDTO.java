package ystar.living.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 直播 PK 是否开始的实体类
 */
@Data
public class LivingPkRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -4135802655494838696L;
    private boolean onlineStatus;
    private String msg;
}