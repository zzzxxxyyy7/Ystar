package com.ystar.user.api.Vo;

import lombok.Data;

import java.util.List;

@Data
public class PayProductVO {

    /**
     * 当前余额
     */
    private Integer currentBalance;
    /**
     * 一系列付费产品
     */
    private List<PayProductItemVO> payProductItemVOList;
}