package com.noob;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// 在PersonHandler中处理对应的HTTP请求，等同于MVC架构中的Service层

/**
 * 将response body 转换为对象/集合
 *
 * bodyToMono：如果返回结果是一个Object，WebClient将接收到响应后把JSON字符串转换为对应的对象。
 *
 * bodyToFlux: 如果响应的结果是一个集合，则不能继续使用bodyToMono()，应该改用bodyToFlux()，然后依次处理每一个元素。
 */
@Service
public class PersonHandler {


    public Mono<ServerResponse> listPeople(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.just(new Person(), new Person()), Person.class);
    }

    public Mono<ServerResponse> createPerson(ServerRequest request) {
        Mono<Person> person = request.bodyToMono(Person.class);
        Person element = person.block();
        return ServerResponse.ok().body(Mono.just(element), Person.class);
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) {
        return ServerResponse.notFound().build();
    }

    @Data
    static class Person {
        String name = "张三";
        Integer id = 1;
    }


}