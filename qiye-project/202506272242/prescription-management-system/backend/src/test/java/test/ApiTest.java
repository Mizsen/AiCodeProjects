package test;

public class ApiTest {
    public static void main(String[] args) throws Exception {

// 同步POST JSON
/*        String jsonResponse = HttpUtil.postJson(
                "http://localhost:8080/api/auth/register",
                "{\"username\":\"John64\",\"password\":\"dssds\",\"role\":\"ADMIN\"}"
        );
        System.out.println(jsonResponse);*/

        String jsonResponse =  HttpUtil.postJson("http://localhost:8080/api/auth/login",
                "{\"username\":\"John64\",\"password\":\"dssds\",\"role\":\"ADMIN\"}");
        System.out.println(jsonResponse);

/*
// 同步POST表单
        String formResponse = HttpUtil.postForm(
                "https://api.example.com/login",
                "username=admin&password=123456"
        );
        System.out.println(formResponse);

// 异步POST
        HttpUtil.postAsync("https://api.example.com/data",
                        "{\"query\":\"test\"}",
                        Map.of("Content-Type", "application/json"))
                .thenAccept(System.out::println);
*/

    }
}
