package com.noob.spring.beanDefinition;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
public class CustomizerClientScannerConfigurer implements BeanFactoryAware, BeanDefinitionRegistryPostProcessor {
	@Setter
	@Getter
	private String basePackage;

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;

	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// do nothing
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
		if (log.isDebugEnabled()) {
			packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
		}
		ClassPathCustomizerClientScanner scanner = new ClassPathCustomizerClientScanner(registry);
		scanner.setAnnotationClass(CustomizerClient.class); // 指定扫描class
		scanner.registerFilters();
		scanner.scan(StringUtils.toStringArray(packages));
		if (!Strings.isNullOrEmpty(basePackage)) {
			scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
					ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}
	}

}
