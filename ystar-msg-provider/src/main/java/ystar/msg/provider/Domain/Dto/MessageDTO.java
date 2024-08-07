package ystar.msg.provider.Domain.Dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class MessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1259190053670615404L;

    /**
     * 己方用户id（也是发送方用户id）
     */
    private Long userId;

    /**
     * 发往的直播间ID
     */
    private Integer roomId;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送人名称
     */
    private String senderName;

    /**
     * 发送人头像
     */
    private String senderAvatar;

    private Date createTime;

    private Date updateTime;
}