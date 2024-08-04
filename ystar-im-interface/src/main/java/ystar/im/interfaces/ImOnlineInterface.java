package ystar.im.interfaces;

public interface ImOnlineInterface {

    /**
     * 判断用户是否在指定的业务线中处于在线状态
     * @param userId
     * @param appId
     * @return
     */
    boolean isOnline(Long userId , int appId) ;

}
