package ystar.framework.web.starter.Error;

import java.io.Serial;

/**
 * 自定义异常类
 */
public class YStarErrorException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = -5253282130382649365L;
    private int errorCode;
    private String errorMsg;

    public YStarErrorException(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public YStarErrorException(YStarBaseError YStarBaseError) {
        this.errorCode = YStarBaseError.getErrorCode();
        this.errorMsg = YStarBaseError.getErrorMsg();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}