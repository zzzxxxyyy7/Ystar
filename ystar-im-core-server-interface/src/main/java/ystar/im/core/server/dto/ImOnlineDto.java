package ystar.im.core.server.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class ImOnlineDto implements Serializable {

    private Long userId;

    private Integer appId;

    private Integer roomId;

    private Long loginTime;
}
