package ystar.live.bank.service.Impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ystar.id.generate.interfaces.IdGenerateRpc;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ystar.live.bank.Domain.Mapper.PayOrderMapper;
import ystar.live.bank.Domain.Po.PayOrderPO;
import ystar.live.bank.Domain.Po.PayTopicPO;
import ystar.live.bank.constants.OrderStatusEnum;
import ystar.live.bank.constants.PayProductTypeEnum;
import ystar.live.bank.dto.PayOrderDTO;
import ystar.live.bank.dto.PayProductDTO;
import ystar.live.bank.service.IPayOrderService;
import ystar.live.bank.service.IPayProductService;
import ystar.live.bank.service.IPayTopicService;
import ystar.live.bank.service.YStarCurrencyAccountService;

@Service
public class PayOrderServiceImpl implements IPayOrderService {


    private static final Logger LOGGER = LoggerFactory.getLogger(PayOrderServiceImpl.class);
    
    @Resource
    private PayOrderMapper payOrderMapper;

    @Resource
    private IdGenerateRpc idGenerateRpc;

    @Resource
    private YStarCurrencyAccountService yStarCurrencyAccountService;

    @Resource
    private IPayTopicService iPayTopicService;

    @Resource
    private IPayProductService iPayProductService;

    /**
     * 订单支付成功的回调函数
     * @param payOrderDTO
     * @return
     */
    @Override
    public boolean payNotify(PayOrderDTO payOrderDTO) {

        // bizCode 与 order 校验
        PayOrderPO payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
        if (payOrderPO == null) {
            LOGGER.error("[PayOrderServiceImpl] payOrderPO is null, create a payOrderPO, userId is {}", payOrderDTO.getUserId());
            yStarCurrencyAccountService.insertOne(payOrderDTO.getUserId());
            payOrderPO = this.queryByOrderId(payOrderDTO.getOrderId());
        }
        PayTopicPO payTopicPO = iPayTopicService.getByCode(payOrderDTO.getBizCode());
        if (payTopicPO == null || StringUtils.isEmpty(payTopicPO.getTopic())) {
            LOGGER.error("[PayOrderServiceImpl] error payTopicPO, payTopicPO is {}", payOrderDTO);
            return false;
        }
        // 调用bank层相应的一些操作
        payNotifyHandler(payOrderPO);

        // TODO 支付成功后：根据bizCode发送mq 异步通知对应的关心的 服务

        return true;
    }

    /**
     * 在bank层处理一些操作：
     * 如 判断充值商品类型，去做对应的商品记录（如：购买虚拟币，进行余额增加，和流水记录）
     */
    private void payNotifyHandler(PayOrderPO payOrderPO) {
        // 更新订单状态为已支付
        this.updateOrderStatus(payOrderPO.getOrderId(), OrderStatusEnum.PAYED.getCode());
        Integer productId = payOrderPO.getProductId();
        PayProductDTO payProductDTO = iPayProductService.getByProductId(productId);
        if (payProductDTO != null && payProductDTO.getType().equals(PayProductTypeEnum.QIYU_COIN.getCode())) {
            // 类型是充值虚拟币业务：
            Long userId = payOrderPO.getUserId();
            JSONObject jsonObject = JSON.parseObject(payProductDTO.getExtra());
            Integer coinNum = jsonObject.getInteger("coin");
            yStarCurrencyAccountService.incr(userId, coinNum);
        }
    }

    @Override
    public PayOrderPO queryByOrderId(String orderId) {
        LambdaQueryWrapper<PayOrderPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PayOrderPO::getOrderId, orderId);
        queryWrapper.last("limit 1");
        return payOrderMapper.selectOne(queryWrapper);
    }

    @Override
    public String insertOne(PayOrderPO payOrderPO) {
        String orderId = String.valueOf(idGenerateRpc.getSeqId(2));
        payOrderPO.setOrderId(orderId);
        payOrderMapper.insert(payOrderPO);
        return payOrderPO.getOrderId();
    }

    @Override
    public boolean updateOrderStatus(Long id, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setId(id);
        payOrderPO.setStatus(status);
        return payOrderMapper.updateById(payOrderPO) > 0;
    }

    @Override
    public boolean updateOrderStatus(String orderId, Integer status) {
        PayOrderPO payOrderPO = new PayOrderPO();
        payOrderPO.setStatus(status);
        LambdaUpdateWrapper<PayOrderPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PayOrderPO::getOrderId, orderId);
        return payOrderMapper.update(payOrderPO, updateWrapper) > 0;
    }
}