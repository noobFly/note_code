package com.noob.responseAutoWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 替换RequestMappingHandlerAdapter中的RequestResponseBodyMethodProcessor
 */
@Configuration
public class AutoWrapResponseBodyConfiguration {
    @Autowired
    private RequestMappingHandlerAdapter adapter;

    @PostConstruct
    public void init() {
        replaceResponseBodyReutrnValue();
    }

    public void replaceResponseBodyReutrnValue() {
        int i = 0;

        Iterator<HttpMessageConverter<?>> iterator = adapter.getMessageConverters().iterator();
        List<HttpMessageConverter<?>> converterList = new ArrayList<>();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof MappingJackson2XmlHttpMessageConverter) { //排除XML转换
                continue;
            }
            converterList.add(converter);
        }

        List<HandlerMethodReturnValueHandler> list = new ArrayList<>();
        list.addAll(adapter.getReturnValueHandlers());
        for (HandlerMethodReturnValueHandler handler : list) {
            if (RequestResponseBodyMethodProcessor.class.isAssignableFrom(handler.getClass())) {
                list.remove(i);
                list.add(i, new AutoWrapRequestResponseBodyMethodProcessor(converterList)); // 替换原有DispatcherServlet默认入参出参解析处理类RequestResponseBodyMethodProcessor
                break;
            }
            i++;
        }

        adapter.setReturnValueHandlers(list);
    }
}
