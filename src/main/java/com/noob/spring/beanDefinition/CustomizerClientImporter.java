package com.noob.spring.beanDefinition;

import java.util.List;

import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.noob.spring.beanDefinition.CustomizerClientImporter.BeanDefinitionRegistrarForImport;
import com.noob.spring.beanDefinition.CustomizerClientImporter.TestNestConfiguration;

import lombok.extern.slf4j.Slf4j;

// 验证 ImportBeanDefinitionRegistrar 单独使用 @Configuration 或者 @Component 并不会执行ImportBeanDefinitionRegistrar.registerBeanDefinitions . 一定需要@Import 方式
@Configuration
@Import(value = { BeanDefinitionRegistrarForImport.class, TestNestConfiguration.class })
@AutoConfigurationPackage
@Slf4j
public class CustomizerClientImporter implements BeanFactoryAware, ImportBeanDefinitionRegistrar {

	private BeanFactory beanFactory;

	// 不会执行进入
	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		System.out.println(importingClassMetadata.getClassName());
		execute(registry);
	}

	private void execute(BeanDefinitionRegistry registry) {
		List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
		if (log.isDebugEnabled()) {
			packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
		}

		ClassPathCustomizerClientScanner scanner = new ClassPathCustomizerClientScanner(registry);
		scanner.setAnnotationClass(CustomizerClient.class); // 指定扫描class
		scanner.registerFilters();
		scanner.scan(StringUtils.toStringArray(packages));
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;

	}

	@Bean
	ImportBeanDefinitionRegistrar createBean() {
		return new BeanDefinitionRegistrarForBean();
	}

	public class BeanDefinitionRegistrarForBean implements ImportBeanDefinitionRegistrar {
		// 同样不会执行进入
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {
			System.out.println(importingClassMetadata.getClassName());
			execute(registry);
		}
	}

//  因为ConfigurationClassPostProcessor -> ConfigurationClassParser 优先解析底层的@Configuration|@Component修饰的装配类，也就是优先处理它@Import的资源类
	@Slf4j
	public static class BeanDefinitionRegistrarForImport implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
		private BeanFactory beanFactory;

		/**
		 * 这里可以执行进入，但是 AnnotationMetadata 是 WebServiceServiceScannerConfigurer2 的信息 。
		 * 按源码分析， 因为是被@Configuration修饰的WebServiceServiceScannerConfigurer2 引进来的。
		 * <p>
		 * 同时 AutoConfigurationPackages.get 是获取不到的。
		 * <p>
		 * 把@Import(BeanDefinitionRegistrarForImport.class) 挂在启动类上就可以。
		 * 启动类的@SpringBootApplication->@EnableAutoConfiguration
		 * -> @AutoConfigurationPackage
		 * 会 @Import(AutoConfigurationPackages.Registrar.class) 同时 AnnotationMetadata 是
		 * 启动类BootstrapApplication的信息
		 */
		@Override
		public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {
			System.out.println(importingClassMetadata.getClassName()); // com.noob.spring.beanDefinition.CustomizerClientImporter

			List<String> packages = null;
			try {
				packages = AutoConfigurationPackages.get(this.beanFactory); // 正常返回启动类所在的包路径[com.noob]
				if (log.isDebugEnabled()) {
					packages.forEach(pkg -> log.debug("Using auto-configuration base package '{}'", pkg));
				}
			} catch (Exception e) {
				log.error("获取根扫描包路径失败", e); // 如果非@EnableAutoConfiguration修饰的类@Import, 将报错
				packages = Lists.newArrayList("com.noob");
			}

			ClassPathCustomizerClientScanner scanner = new ClassPathCustomizerClientScanner(registry);
			scanner.setAnnotationClass(CustomizerClient.class); // 指定扫描class
			scanner.registerFilters();
			scanner.scan(StringUtils.toStringArray(packages));

		}

		// 在ConfigurationClassPostProcessor.postProcessBeanDefinitionRegistry执行过程中，会提前创建ImportBeanDefinitionRegistrar对象，并执行BeanFactoryAware
		@Override
		public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
			this.beanFactory = beanFactory;

		}
	}

	@Import(TestNestConfiguration2.class)
	@Component
	/**
	 * @Component无法被创建为ConfigurationClassEnhancer.newEnhancer的代理对象
	 *                                                             <p>
	 *                                                             在ConfigurationClassParser#ImportStack#getImportingClassFor(String
	 *                                                             importedClass)入参“bean.getClass().getSuperclass()”是Object
	 *                                                             所以就无法获取到AnnotationMetadata,
	 *                                                             也就不执行ImportAware.setImportMetadata
	 *
	 */

	public static class TestNestConfiguration implements ImportAware {
		public TestNestConfiguration() {
			System.out.println("TestNestConfiguration");
		}

		@Override
		public void setImportMetadata(AnnotationMetadata importMetadata) {
			System.out.println(importMetadata.getClassName()); // com.noob.spring.beanDefinition.CustomizerClientImporter
		}
	}

	@Configuration // 保证ImportAware.setImportMetadata可以正常执行
	public static class TestNestConfiguration2 implements ImportAware {
		public TestNestConfiguration2() {
			System.out.println("TestNestConfiguration2");
		}

		@Override
		public void setImportMetadata(AnnotationMetadata importMetadata) {
			System.out.println(importMetadata.getClassName()); // com.noob.spring.beanDefinition.CustomizerClientImporter$TestNestConfiguration
		}
	}
}
