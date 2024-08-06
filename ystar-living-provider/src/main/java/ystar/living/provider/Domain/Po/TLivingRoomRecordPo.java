package ystar.living.provider.Domain.Po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName t_living_room_record
 */
@TableName(value ="t_living_room_record")
@Data
public class TLivingRoomRecordPo implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主播id
     */
    @TableField("anchor_id")
    private Long anchorId;

    /**
     * 直播间类型（0默认类型）
     */
    private Integer type;

    /**
     * 状态（0无效1有效）
     */
    private Integer status;

    /**
     * 直播间名称
     */
    @TableField("room_name")
    private String roomName;

    /**
     * 直播间封面
     */
    @TableField("covert_img")
    private String covertImg;

    /**
     * 观看数量
     */
    private Integer watch_num;

    /**
     * 点赞数量
     */
    private Integer good_num;

    /**
     * 开播时间
     */
    private Date start_time;

    /**
     * 关播时间
     */
    private Date end_time;

    /**
     * 
     */
    private Date update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}