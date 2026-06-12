package com.kb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * 首页控制器，重定向根路径到静态首页
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("index.html");
    }
}
