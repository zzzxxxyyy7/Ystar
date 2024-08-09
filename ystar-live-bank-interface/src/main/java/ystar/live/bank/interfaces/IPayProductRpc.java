package ystar.live.bank.interfaces;

import ystar.live.bank.dto.PayProductDTO;

import java.util.List;

public interface IPayProductRpc {

    /**
     * 根据产品类型，返回批量的商品信息
     */
    List<PayProductDTO> products(Integer type);

    /**
     * 根据产品id查询
     */
    PayProductDTO getByProductId(Integer productId);
}