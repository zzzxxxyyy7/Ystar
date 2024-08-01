package ystar.msg.provider.Service;


import com.baomidou.mybatisplus.extension.service.IService;
import ystar.msg.Constants.MsgSendResultEnum;
import ystar.msg.Dto.MsgCheckDTO;
import ystar.msg.provider.Domain.Po.SmsPo;

/**
* @author Rhss
* @description 针对表【t_sms】的数据库操作Service
* @createDate 2024-07-31 17:49:20
*/
public interface ISmsService extends IService<SmsPo> {

    MsgSendResultEnum sendLoginCode(String phone);

    MsgCheckDTO checkLoginCode(String phone, Integer code);

    void insertOne(String phone, Integer code);
}
