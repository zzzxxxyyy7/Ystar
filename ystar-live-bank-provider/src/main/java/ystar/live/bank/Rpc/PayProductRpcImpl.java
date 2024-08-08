package ystar.live.bank.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.live.bank.dto.PayProductDTO;
import ystar.live.bank.interfaces.IPayProductRpc;
import ystar.live.bank.service.IPayProductService;

import java.util.List;

@DubboService
public class PayProductRpcImpl implements IPayProductRpc {
    
    @Resource
    private IPayProductService payProductService;

    @Override
    public List<PayProductDTO> products(Integer type) {
        return payProductService.products(type);
    }
}