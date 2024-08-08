package ystar.live.bank.dto;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class YStarCurrencyAccountDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4594540862190026761L;
    
    private Long userId;
    private int currentBalance;
    private int totalCharged;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}