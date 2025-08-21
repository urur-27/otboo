package com.team3.otboo.domain.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// API 및 정적 자원 관련 경로를 제외한 모든 GET요청을 index.html로 forwarding하는 컨트롤러

@Controller
public class FowardingController {

    @GetMapping(value = "/{path:^(?!api|assets|static|index\\.html|.*\\\\..*).*}/**")
    public String forward(){
        // index.html로 forwarding
        return "forward:/index.html";
    }
}
