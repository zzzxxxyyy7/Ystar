package ystar.framework.web.starter.Error;

/**
 * 自定义断言类
 */
public class ErrorAssert {
    
    /**
     * 判断参数不能为空
     */
    public static void isNotNull(Object obj, YStarBaseError YStarBaseError) {
        if (obj == null) {
            throw new YStarErrorException(YStarBaseError);
        }
    }

    /**
     * 判断字符串不能为空
     */
    public static void isNotBlank(String str, YStarBaseError YStarBaseError) {
        if (str == null || str.trim().length() == 0) {
            throw new YStarErrorException(YStarBaseError);
        }
    }

    /**
     * flag == true
     */
    public static void isTure(boolean flag, YStarBaseError YStarBaseError) {
        if (!flag) {
            throw new YStarErrorException(YStarBaseError);
        }
    }

    /**
     * flag == true
     */
    public static void isTure(boolean flag, YStarErrorException yStarErrorException) {
        if (!flag) {
            throw yStarErrorException;
        }
    }
}