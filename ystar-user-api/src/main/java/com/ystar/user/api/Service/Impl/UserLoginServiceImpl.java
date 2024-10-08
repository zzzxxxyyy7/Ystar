package com.ystar.user.api.Service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.ystar.common.VO.WebResponseVO;
import com.ystar.user.api.Service.IUserLoginService;
import com.ystar.user.api.Vo.UserLoginVO;
import com.ystar.user.api.error.YStarApiError;
import com.ystar.user.dto.UserLoginDTO;
import com.ystar.user.interfaces.IUserPhoneRPC;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import ystar.auth.account.interfaces.IAccountTokenRPC;
import ystar.framework.web.starter.Error.ErrorAssert;
import ystar.msg.Constants.MsgSendResultEnum;
import ystar.msg.Dto.MsgCheckDTO;
import ystar.msg.Interfaces.ISmsRpc;

import java.util.regex.Pattern;

@Service
public class UserLoginServiceImpl implements IUserLoginService {

    private static final String PHONE_REG = "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLoginServiceImpl.class);

    @DubboReference
    private ISmsRpc smsRpc;

    @DubboReference
    private IUserPhoneRPC userPhoneRpc;
    
    @DubboReference
    private IAccountTokenRPC accountTokenRPC;

    @Override
    public WebResponseVO sendLoginCode(String phone) {
        // 参数校验
        ErrorAssert.isNotNull(phone, YStarApiError.PHONE_NOT_BLANK);
        ErrorAssert.isTure(Pattern.matches(PHONE_REG, phone), YStarApiError.PHONE_IN_VALID);
        MsgSendResultEnum msgSendResultEnum = smsRpc.sendLoginCode(phone);
        if (msgSendResultEnum == MsgSendResultEnum.SEND_SUCCESS) {
            return WebResponseVO.success();
        }
        return WebResponseVO.sysError("短信发送太频繁，请稍后再试");
    }

    @Override
    public WebResponseVO login(String phone, Integer code, HttpServletResponse response) {
        // 参数校验
        ErrorAssert.isNotNull(phone, YStarApiError.PHONE_NOT_BLANK);
        ErrorAssert.isTure(Pattern.matches(PHONE_REG, phone), YStarApiError.PHONE_IN_VALID);
        ErrorAssert.isTure(code != null || code >= 1000, YStarApiError.LOGIN_CODE_IN_VALID);
        // 检查验证码是否匹配
        MsgCheckDTO msgCheckDTO = smsRpc.checkLoginCode(phone, code);
        if (!msgCheckDTO.isCheckStatus()) {// 校验没通过
            return WebResponseVO.bizError(msgCheckDTO.getDesc());
        }
        // 封装token到cookie返回
        UserLoginDTO userLoginDTO = userPhoneRpc.login(phone);
        String token = accountTokenRPC.createAndSaveLoginToken(userLoginDTO.getUserId());

        ResponseCookie responseCookie = ResponseCookie.from("ystar", token)
                .maxAge(30 * 24 * 3600)
                .secure(true)
                .domain("localhost")
                .path("/")
                .sameSite(Cookie.SameSite.NONE.attributeValue())
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE , responseCookie.toString());
        return WebResponseVO.success(BeanUtil.copyProperties(userLoginDTO, UserLoginVO.class));
    }
}