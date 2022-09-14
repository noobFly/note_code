package com.noob;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * WebFlux提供了与之前WebMVC相同的一套注解来定义请求的处理，使得Spring使用者迁移到响应式开发方式的过程变得异常轻松。
 * <p>
 * 依赖spring-boot-starter-webflux， 整个技术栈从命令式的、同步阻塞的【spring-webmvc + servlet + Tomcat】变成了响应式的、异步非阻塞的【spring-webflux + Reactor + Netty】。
 * <p>
 * spring-boot-starter-web和spring-boot-starter-webflux能否一起工作？
 * 当两者一起时配置的并不是webflux web application, 仍然时一个spring mvc web application。
 * 官方文档中有这么一段注解：很多开发者添加spring-boot-start-webflux到他们的spring mvc web applicaiton去是为了使用reactive WebClient.
 * 如果希望更改webApplication 类型需要显示的设置，如SpringApplication.setWebApplicationType(WebApplicationType.REACTIVE).
 */
@RestController
public class HelloController {


    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Welcome to reactive world ~");
    }
}