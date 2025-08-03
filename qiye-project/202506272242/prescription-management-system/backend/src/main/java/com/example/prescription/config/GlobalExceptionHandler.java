import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;
import com.example.prescription.utils.JsonUtils;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理文件上传大小超限异常
    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String, Object> handleFileSizeLimitExceeded() {
        String errorMsg = "上传的文件太大，请选择较小的文件。";
        logger.warn("文件上传大小超限: {}", errorMsg);
        return buildErrorResponse(false, errorMsg);
    }

    // 处理空指针异常
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleNullPointerException(NullPointerException ex) {
        logger.error("发生空指针异常", ex);
        return buildErrorResponse(false, "发生空指针异常: " + ex.getMessage());
    }

    // 捕获所有未被单独处理的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneralException(Exception ex) {
        logger.error("系统内部错误", ex);
        return buildErrorResponse(false, "系统内部错误: " + ex.getMessage());
    }

    // 构建统一响应格式
    private Map<String, Object> buildErrorResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("msg", message);
        logger.info("全局一次异常处理响应: {}", JsonUtils.toJson(response));
        return response;
    }
}