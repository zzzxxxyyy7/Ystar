package ystar.framework.web.starter.Error;

/**
 * 我们自定义异常的接口规范
 */
public interface YStarBaseError {

    int getErrorCode();

    String getErrorMsg();
}