package com.noob;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 根据bean类获取bean
     *
     * @param clazz
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        if (applicationContext == null) {
            throw new RuntimeException("applicationContext未初始化");
        }
        return applicationContext.getBean(clazz);
    }

    /**
     * 根据beanID获取注入对像
     *
     * @param beanId
     * @return 设定文件
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanId) {
        return (T) applicationContext.getBean(beanId);
    }

    public static ApplicationContext getContext() {
        return applicationContext;
    }
}
