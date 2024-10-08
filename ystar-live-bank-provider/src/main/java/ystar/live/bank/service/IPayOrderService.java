package ystar.live.bank.service;

import ystar.live.bank.Domain.Po.PayOrderPO;
import ystar.live.bank.dto.PayOrderDTO;

public interface IPayOrderService {
    
    /**
     * 根据orderId查询订单信息
     */
    PayOrderPO queryByOrderId(String orderId);

    /**
     *插入订单 ，返回主键id
     */
    String insertOne(PayOrderPO payOrderPO);

    /**
     * 根据主键id更新订单状态
     */
    boolean updateOrderStatus(Long id, Integer status);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(String orderId, Integer status);

    /**
     * 支付回调请求的接口
     */
    boolean payNotify(PayOrderDTO payOrderDTO);
}