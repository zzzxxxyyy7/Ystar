package ystar.living.Vo.resp;

import lombok.Data;

import java.util.List;

@Data
public class LivingRoomPageRespVO {

    /**
     * 直播间列表展示
     */
    private List<LivingRoomRespVO> list;

    /**
     * 是否有下一个
     */
    private boolean hasNext;
}