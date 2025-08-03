package com.example.prescription.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.*;

public class RequestLogUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 打印请求参数（支持GET、POST、表单、JSON、文件等）
     */
    public static String printRequestParams(HttpServletRequest request, Object body) {
        StringBuilder sb = new StringBuilder();
        sb.append("请求方法: ").append(request.getMethod()).append("\n");
        sb.append("请求路径: ").append(request.getRequestURI()).append("\n");

        // 打印URL参数
        Map<String, String[]> paramMap = request.getParameterMap();
        if (!paramMap.isEmpty()) {
            sb.append("URL参数: ");
            paramMap.forEach((k, v) -> sb.append(k).append("=").append(Arrays.toString(v)).append("; "));
            sb.append("\n");
        }

        // 打印Header
        sb.append("Headers: ");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String h = headerNames.nextElement();
            sb.append(h).append("=").append(request.getHeader(h)).append("; ");
        }
        sb.append("\n");

        // 打印Body参数（JSON/表单）
        if (body != null) {
            try {
                sb.append("Body参数: ").append(objectMapper.writeValueAsString(body)).append("\n");
            } catch (Exception e) {
                sb.append("Body参数: ").append(body.toString()).append("\n");
            }
        } else if ("POST".equalsIgnoreCase(request.getMethod()) || "PUT".equalsIgnoreCase(request.getMethod())) {
            try {
                StringBuilder raw = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    raw.append(line);
                }
                if (raw.length() > 0) {
                    sb.append("Raw Body: ").append(raw).append("\n");
                }
            } catch (Exception ignored) {}
        }

        // 打印文件参数（如果有）
        if (request instanceof org.springframework.web.multipart.MultipartHttpServletRequest multipartRequest) {
            MultiValueMap<String, MultipartFile> fileMap = multipartRequest.getMultiFileMap();
            if (fileMap != null && !fileMap.isEmpty()) {
                sb.append("文件参数: ");
                fileMap.forEach((k, v) -> sb.append(k).append("=").append(v.size()).append("个文件; "));
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
