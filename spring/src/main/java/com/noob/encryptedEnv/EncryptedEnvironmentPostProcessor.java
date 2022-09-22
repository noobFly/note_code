package com.noob.encryptedEnv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.stream.Collectors;

/** 对application.yml文件里的指定
 * 在\resources\META-INF\spring.factories里配置
 * org.springframework.boot.env.EnvironmentPostProcessor=com.noob.encryptedEnv.EncryptedEnvironmentPostProcessor
 * <p>
 *     在AbstractApplicationContext#refresh()之前的SpringApplication#prepareEnvironment阶段，
 *     由ConfigFileApplicationListener#onApplicationEvent装载EnvironmentPostProcessor集合来依次执行它们的#postProcessEnvironment;
 *     本类order优先级最低！这样可以在由ConfigFileApplicationListener（它也实现了EnvironmentPostProcessor）加载好配置文件后来封装PropertySource
 *
 */
@Order
public class EncryptedEnvironmentPostProcessor implements EnvironmentPostProcessor {
    // 只针对项目本身配置文件
    private final String name = "applicationConfig: [classpath:/application.yml]";
    // 外置文件
    private final String name2 = "applicationConfig: [file:./config/application.yml]";


    /**
     * PropertySource有很多种 SystemEnvironmentPropertySource、MapPropertySource、EnumerablePropertySource
     * <p>
     * applicaiton.yml文件的是:  OriginTrackedMapPropertySource extends MapPropertySource ，
     * 如果是项目内配置它的名字是： "applicationConfig: [classpath:/application.yml]";
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();

        mutablePropertySources.stream().filter(t -> t instanceof MapPropertySource).
                map(t -> new EncryptedPropertySourceWrapper((MapPropertySource) t)).collect(Collectors.toList()).
                forEach(
                        t -> mutablePropertySources.replace(t.getName(), t)); // 屏蔽配置文件加载方式不同的差异
    }
}