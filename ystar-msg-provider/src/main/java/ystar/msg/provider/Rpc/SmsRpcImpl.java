package ystar.msg.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.msg.Constants.MsgSendResultEnum;
import ystar.msg.Dto.MsgCheckDTO;
import ystar.msg.Interfaces.ISmsRpc;
import ystar.msg.provider.Service.ISmsService;


@DubboService
public class SmsRpcImpl implements ISmsRpc {

    @Resource
    private ISmsService smsService;

    @Override
    public MsgSendResultEnum sendLoginCode(String phone) {
        return smsService.sendLoginCode(phone);
    }

    @Override
    public MsgCheckDTO checkLoginCode(String phone, Integer code) {
        return smsService.checkLoginCode(phone,code);
    }

    @Override
    public void insertOne(String phone, Integer code) {
        smsService.insertOne(phone,code);
    }
}