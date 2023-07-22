package com.wework.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.wework.service.WeworkExternalService;
import jakarta.validation.Valid;

@RestController
public class TestController {

    @Autowired
    private WeworkExternalService weworkExternalService;

    @PostMapping("/test")
    public String test() {
        return "test";
    }

    /**
     * 创建联系我（非临时会话）
     */
    @PostMapping("/contact_way/create")
    public WeworkExternalService.CreateContactWayResp createContactWay(@RequestBody @Valid WeworkExternalService.CreateContactWayReq req) {
        return weworkExternalService.createContactWay(req);
    }
}
