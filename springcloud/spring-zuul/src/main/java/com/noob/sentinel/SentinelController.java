package com.noob.sentinel;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SentinelController {


    @RequestMapping("/test_1")
    @SentinelResource(value = "ip_info")
    public String test_1() {
        throw new RuntimeException("熔断");
    }
// SentinelResourceAspect -> AbstractSentinelAspectSupport.handleBlockException   优先handleBlockException再handleFallback
    @RequestMapping("/test_2")
    @SentinelResource(value = "ip_info", blockHandler = "handle", blockHandlerClass = SentinelHandler.class, fallbackClass = SentinelHandler.class, fallback = "fallback")
    public String test_2() {
        return "test_2";
    }
}
