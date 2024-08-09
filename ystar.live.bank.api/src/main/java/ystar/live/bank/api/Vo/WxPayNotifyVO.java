package ystar.live.bank.api.Vo;

import lombok.Data;

@Data
public class WxPayNotifyVO {
    
    private String orderId;
    private Long userId;
    private Integer bizCode;
}
