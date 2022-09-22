package com.noob.responseAutoWrapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResponseBody 响应数据自动包装处理器<br>
 * 如果使用ResponseBodyAdvice处理会引起一些的问题，此种用MethodProcessor的方法最好
 */
public class AutoWrapRequestResponseBodyMethodProcessor extends
        RequestResponseBodyMethodProcessor {
    private final ConcurrentHashMap<Method, Boolean> wrapperAssetCache = new ConcurrentHashMap<>();
    private static final String DEFAULT_MESSAGE = "操作成功";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    /**
     * 当集成了第三方包中的RestControler，不需要对返回值进行包装时，而又无法去添加注解声明不需要包装，这时可通过applicatioin.yml(application.properties)配置排序<br>
     * 举例：autowrap.excludeUrlPatterns: /foo/bar/*,/foo/quz<br>
     * 使用AntPathMatcher进行匹配
     */
    @Value("${autowrap.excludeUrlPatterns:}")
    private String excludeUrlPatternsStr;
    private List<String> excludeUrlPatterns = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (StringUtils.isNotBlank(excludeUrlPatternsStr)) {
            for (String pattern : excludeUrlPatternsStr.split("[,;]")) {
                if (StringUtils.isNotBlank(pattern)) {
                    excludeUrlPatterns.add(pattern.trim());
                }
            }
        }
    }

    public AutoWrapRequestResponseBodyMethodProcessor(
            List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    public AutoWrapRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters,
                                                      ContentNegotiationManager manager) {

        super(converters, manager);
    }

    public AutoWrapRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters,
                                                      List<Object> requestResponseBodyAdvice) {

        super(converters, null, requestResponseBodyAdvice);
    }

    public AutoWrapRequestResponseBodyMethodProcessor(List<HttpMessageConverter<?>> converters,
                                                      ContentNegotiationManager manager, List<Object> requestResponseBodyAdvice) {

        super(converters, manager, requestResponseBodyAdvice);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return super.supportsReturnType(returnType);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer, NativeWebRequest webRequest)
            throws IOException, HttpMediaTypeNotAcceptableException, HttpMessageNotWritableException {

        Boolean needWrap = wrapperAssetCache.computeIfAbsent(returnType.getMethod(), method -> {
            boolean wrap = true;
           // 优先Mehtod级别，其次Controller
            AutoWrap autoWrap = returnType.getMethod().getAnnotation(AutoWrap.class); //如果Controller被增强，可能无法获取
            if (autoWrap == null) {
                autoWrap = returnType.getDeclaringClass().getAnnotation(AutoWrap.class);
            }
            if (autoWrap != null) {
                wrap = autoWrap.value();
            }
            for (String urlPattern : excludeUrlPatterns) {
                if (pathMatcher.match(urlPattern, webRequest.getNativeRequest(HttpServletRequest.class).getRequestURI())) {
                    wrap = false;
                    break;
                }
            }
            return wrap;
        });

        if (needWrap) {
            int code = WrapperHelper.getCode();
            String message = WrapperHelper.getMessage();
            if (message == null) {
                message = DEFAULT_MESSAGE;
            }
            super.handleReturnValue(new Result<>(code, message, returnValue), returnType, mavContainer, webRequest);
        } else {
            super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
        }
    }
}
