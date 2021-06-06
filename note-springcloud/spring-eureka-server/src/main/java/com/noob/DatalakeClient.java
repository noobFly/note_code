package com.noob;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.noob.DatalakeClient.WithExceptionFallbackFactory;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

// 这里指定的spring.application.name一定要大写！ 优先fallback再fallbackFactory
/**
 * 'name' or 'value' 一定要指定serviceId！  接口提供方没有注册到eureka，可以直接指定url访问地址。
 *
 */

@FeignClient(value = "DATALAKE2", url="localhost:2100", /* fallback = FailBack.class, */fallbackFactory =  WithExceptionFallbackFactory.class, path = "data")
public interface DatalakeClient {
	@RequestMapping(method = RequestMethod.GET, value = "/collect")
	String collect(@RequestParam("log") String log);
	
	@RequestMapping(method = RequestMethod.GET, value = "/filter")
	String filter(@RequestParam("log") String msg);

	// 一定要实现DatalakeClient接口，且必须有@Component; 否则启动报错：
	/**
	 * Caused by: java.lang.IllegalStateException: No fallback instance of type class cn.utrust.microservice.eurekaserver.DatalakeClient$FailBack found for feign client DATALAKE1
	at org.springframework.cloud.openfeign.HystrixTargeter.getFromContext(HystrixTargeter.java:81)
	at org.springframework.cloud.openfeign.HystrixTargeter.targetWithFallback(HystrixTargeter.java:72)
	at org.springframework.cloud.openfeign.HystrixTargeter.target(HystrixTargeter.java:49)
	at org.springframework.cloud.openfeign.FeignClientFactoryBean.loadBalance(FeignClientFactoryBean.java:292)
	at org.springframework.cloud.openfeign.FeignClientFactoryBean.getTarget(FeignClientFactoryBean.java:321)
	at org.springframework.cloud.openfeign.FeignClientFactoryBean.getObject(FeignClientFactoryBean.java:301)
	at org.springframework.beans.factory.support.FactoryBeanRegistrySupport.doGetObjectFromFactoryBean(FactoryBeanRegistrySupport.java:169)
	... 31 common frames omitted
	 *
	 */
	@Slf4j
	@Component
	public static class FailBack implements DatalakeClient {
		
		public String collect(String msg) {
			log.info("进入了failBack collect " + msg);
			return "FallBack_collect " + msg;
		}

		@Override
		public String filter(String msg) {
			log.info("进入了failBack  filter" + msg);
			return "FallBack_filter " + msg;
		}

	}
    // 带异常处理的。 优先fallback再fallbackFactory
	@Slf4j
	@Component
	public static class WithExceptionFallbackFactory implements FallbackFactory<DatalakeClient> {

		@Override
		public DatalakeClient create(Throwable e) {
			log.error("调用data接口异常",e);
			return new DatalakeClient() {
				@Override
				public String collect(String msg) {
					log.info("进入了failBackFactory collect " + msg);
					return "failBackFactory_collect " + msg;
				}

				@Override
				public String filter(String msg) {
					log.info("进入了failBackFactory  filter" + msg);
					return "failBackFactory_filter " + msg;
				}
			};
		}
	}

}
