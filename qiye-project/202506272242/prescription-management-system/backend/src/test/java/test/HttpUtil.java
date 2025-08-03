package test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP 请求工具类
 */
public class HttpUtil {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 发送 GET 请求 (同步)
     * @param url 请求地址
     * @param headers 请求头 (可选)
     * @return 响应字符串
     */
    public static String get(String url, Map<String, String> headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 发送 GET 请求 (异步)
     * @param url 请求地址
     * @param headers 请求头 (可选)
     * @return CompletableFuture 包含响应字符串
     */
    public static CompletableFuture<String> getAsync(String url, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request = builder.build();
        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * 发送 POST 请求 (同步)
     * @param url 请求地址
     * @param body 请求体
     * @param headers 请求头 (可选)
     * @return 响应字符串
     */
    public static String post(String url, String body, Map<String, String> headers) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(BodyPublishers.ofString(body));

        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request = builder.build();
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    /**
     * 发送 POST 请求 (异步)
     * @param url 请求地址
     * @param body 请求体
     * @param headers 请求头 (可选)
     * @return CompletableFuture 包含响应字符串
     */
    public static CompletableFuture<String> postAsync(String url, String body, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(BodyPublishers.ofString(body));

        if (headers != null) {
            headers.forEach(builder::header);
        }

        HttpRequest request = builder.build();
        return httpClient.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * 发送 POST 请求 (JSON格式)
     * @param url 请求地址
     * @param jsonBody JSON请求体
     * @return 响应字符串
     */
    public static String postJson(String url, String jsonBody) throws Exception {
        return post(url, jsonBody, Map.of(
                "Content-Type", "application/json",
                "Accept", "application/json"
        ));
    }

    /**
     * 发送 POST 请求 (表单格式)
     * @param url 请求地址
     * @param formData 表单数据 (key=value&key2=value2)
     * @return 响应字符串
     */
    public static String postForm(String url, String formData) throws Exception {
        return post(url, formData, Map.of(
                "Content-Type", "application/x-www-form-urlencoded"
        ));
    }
}