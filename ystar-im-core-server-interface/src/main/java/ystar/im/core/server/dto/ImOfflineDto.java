package ystar.im.core.server.dto;

import lombok.Data;

import java.io.Serializable;


@Data
public class ImOfflineDto implements Serializable {

    private Long userId;

    private Integer appId;

    private Integer roomId;

    private Long logoutTime;

}
