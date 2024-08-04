package ystar.im.provider.Rpc;

import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import ystar.im.interfaces.ImOnlineInterface;
import ystar.im.provider.Service.ImOnlineService;

/**
 * 判断用户是否在线
 */
@DubboService
public class ImOnlineRpcImpl implements ImOnlineInterface {

    @Resource
    private ImOnlineService imOnlineService;


    @Override
    public boolean isOnline(Long userId, int appId) {
        return imOnlineService.isOnline(userId , appId);
    }
}
