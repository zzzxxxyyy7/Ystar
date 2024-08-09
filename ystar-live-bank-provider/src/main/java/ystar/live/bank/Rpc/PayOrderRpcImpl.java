package ystar.live.bank.Rpc;

import com.ystar.common.utils.ConvertBeanUtils;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.live.bank.Domain.Po.PayOrderPO;
import ystar.live.bank.dto.PayOrderDTO;
import ystar.live.bank.interfaces.IPayOrderRpc;
import ystar.live.bank.service.IPayOrderService;

@DubboService
public class PayOrderRpcImpl implements IPayOrderRpc {
    
    @Resource
    private IPayOrderService payOrderService;
    
    @Override
    public String insertOne(PayOrderDTO payOrderDTO) {
        return payOrderService.insertOne(ConvertBeanUtils.convert(payOrderDTO, PayOrderPO.class));
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        return payOrderService.updateOrderStatus(id, status);
    }

    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        return payOrderService.updateOrderStatus(orderId, status);
    }

    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {
        return payOrderService.payNotify(payOrderDTO);
    }
}