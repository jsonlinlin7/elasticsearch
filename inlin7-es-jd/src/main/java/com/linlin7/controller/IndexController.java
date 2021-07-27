package com.linlin7.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping({"/","/index"}) //thymeleaf的默认连接前端编写规则
    public String index(){
        return "index";
    }
}
