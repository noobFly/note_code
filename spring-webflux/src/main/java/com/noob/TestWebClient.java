package com.noob;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
/**
 * mono表示0~1的序列，flux用来表示0~N个元素序列，mono是flux的简化版，flux可以用来表示流
  */
@Slf4j
public class TestWebClient {

    public static void main(String[] args) {
        WebClient webClient = WebClient.create();
// 如果是调用特定服务的API，可以在初始化webclient时使用baseUrl, ur就可以设置为path不同的部分
        WebClient webClient2 = WebClient.create("https://api.github.com");
        WebClient webClient1 = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "Spring 5 WebClient").build();


        // get
        Mono<String> resp = WebClient.create()
                .method(HttpMethod.GET)
                .uri("http://www.baidu.com")
                .cookie("token", "xxxx")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve().bodyToMono(String.class); // retrieve方法是直接获取响应body，但是，如果需要响应的头信息、Cookie等，可以使用exchange方法，该方法可以访问整个ClientResponse。由


        // post
        MultiValueMap<String, String> formData = new LinkedMultiValueMap();
        formData.add("name1", "value1");
        formData.add("name2", "value2");
        Mono<String> resp2 = WebClient.create().post()
                .uri("http://www.w3school.com.cn/test/demo_form.asp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData)) //使用BodyInserters类提供的各种工厂方法来构造BodyInserter对象并将其传递给body方法。BodyInserters类包含从Object，Publisher，Resource，FormData，MultipartData等创建BodyInserter的方法。
                .retrieve().bodyToMono(String.class);
        System.out.print("result:" + resp2.block());
    }

    @Data
    static class Book {
        String name;
        String title;
    }

    public void testPostJson() {
        Book book = new Book();
        book.setName("name");
        book.setTitle("this is title");
        Mono<String> resp = WebClient.create().post()
                .uri("http://localhost:8080/demo/json")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(book), Book.class)
                .retrieve().bodyToMono(String.class);
        System.out.print("result:" + resp.block());
    }

    public void testFormParam4xx() {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/vnd.github.v3+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "Spring 5 WebClient")
                .build();
        WebClient.ResponseSpec responseSpec = webClient.method(HttpMethod.GET)
                .uri("/user/repos?sort={sortField}&direction={sortDirection}",
                        "updated", "desc")
                .retrieve();
        Mono<String> mono = responseSpec
                .onStatus(e -> e.is4xxClientError(), resp -> {
                    log.error("error:{},msg:{}", resp.statusCode().value(), resp.statusCode().getReasonPhrase());
                    return Mono.error(new RuntimeException(resp.statusCode().value() + " : " + resp.statusCode().getReasonPhrase()));
                })
                .bodyToMono(String.class)
                .doOnError(WebClientResponseException.class, err -> {
                    log.info("ERROR status:{},msg:{}", err.getRawStatusCode(), err.getResponseBodyAsString());
                    throw new RuntimeException(err.getMessage());
                }) // 异常适配
                .onErrorReturn("fallback"); // 返回默认值
        String result = mono.block();
        System.out.print(result);
    }
}
