package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class MenuController {
    @GetMapping("/api/menus")
    public List<Map<String, Object>> getMenus() {
        return List.of(
            Map.of(
                "index", "home",
                "title", "首页",
                "children", List.of(
                    Map.of("index", "/", "title", "首页")
                )
            ),
            Map.of(
                "index", "jobs",
                "title", "岗位模块",
                "children", List.of(
                    Map.of("index", "/jobs/add", "title", "新增岗位"),
                    Map.of("index", "/jobs", "title", "岗位列表")
                )
            ),
            Map.of(
                "index", "users",
                "title", "用户模块",
                "children", List.of(
                    Map.of("index", "/users", "title", "用户列表")
                )
            )
        );
    }
}
