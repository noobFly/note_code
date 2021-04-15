package com.noob.spring.beanDefinition;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

public class ClassPathCustomizerClientScanner extends ClassPathBeanDefinitionScanner {

	private Class<? extends Annotation> annotationClass;

	public void setAnnotationClass(Class<? extends Annotation> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public ClassPathCustomizerClientScanner(BeanDefinitionRegistry registry) {
		super(registry, false);
	}

	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

		if (beanDefinitions.isEmpty()) {
			logger.warn("在包'" + Arrays.toString(basePackages) + "'下未找到相关CustomizerClient接口，请检查你的配置");
		} else {
			processBeanDefinitions(beanDefinitions);
		}

		return beanDefinitions;
	}

	private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
		ScannedGenericBeanDefinition definition;
		for (BeanDefinitionHolder holder : beanDefinitions) {
			definition = (ScannedGenericBeanDefinition) holder.getBeanDefinition();

			if (logger.isDebugEnabled()) {
				logger.debug("Creating CustomizerClientFactoryBean with name '" + holder.getBeanName() + "' and '"
						+ definition.getBeanClassName() + "' CustomizerClient");
			}
			
			definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName()); // 设置构造器的参数，definition.getBeanClassName()需要先于 definition.setBeanClass执行才能得到源真实类名！ 
			definition.setBeanClass(CustomizerClientFactoryBean.class); // 设置工厂类

			// 这里也可以通过设置属性方式注入代理工厂类必要的属性值
			definition.getPropertyValues().add("proxyTargetClass", true); 
			definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
		}
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent(); // 接口 或者 独立类
	}

	@Override
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) {
		if (super.checkCandidate(beanName, beanDefinition)) {
			return true;
		} else {
			logger.warn("Skipping CustomizerClientFactoryBean with name '" + beanName + "' and '"
					+ beanDefinition.getBeanClassName() + "' CustomizerClient"
					+ ". Bean already defined with the same name!");
			return false;
		}
	}

	/**
	 * 注册过滤器
	 */
	public void registerFilters() {
		if (this.annotationClass != null) {
			addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
		} else {
			addIncludeFilter(new TypeFilter() {
				@Override
				public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
						throws IOException {
					return true;
				}
			});
		}
	}
}
