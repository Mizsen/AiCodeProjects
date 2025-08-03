package com.example.backend;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JobApiTest {
    public static void main(String[] args) throws Exception {
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:8003/api/jobs/repo-page?page=0&size=10";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("分页查询结果: " + response.body().string());
            } else {
                System.out.println("请求失败: " + response.code());
            }
        }
    }
}
