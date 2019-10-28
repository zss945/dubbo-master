package com.dubbo.consumer.controller;

import com.duboo.api.DubboService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class LoginController {

    // random 随机，默认的
    // roundrobin 轮询
    // leastactive //最少活跃
    // consistenthash //一致性Hash

    @Reference(loadbalance = "roundrobin")
    private DubboService dubboService;

    @GetMapping("getInfo")
    public Object getServiceInfo() {
        return dubboService.getServiceInfo();
    }

}
