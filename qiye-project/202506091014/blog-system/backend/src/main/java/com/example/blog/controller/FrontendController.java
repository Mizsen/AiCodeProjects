// package com.example.blog.controller;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
// import org.springframework.web.bind.annotation.RequestMapping;
// import com.example.blog.util.JsonUtils;
// import org.springframework.stereotype.Controller;



// @Controller  
// public class FrontendController {

//     private static final Logger logger = LogManager.getLogger(FrontendController.class);

//     // 只要不是以/api开头且不包含点的路径都转发到index.html
//     @RequestMapping(value = {"/{path:[^\\.]+}", "/**"})
//     public String forward() {

//         logger.info("Forwarding request to index.html");
//         return "forward:/index.html";
//     }
// }