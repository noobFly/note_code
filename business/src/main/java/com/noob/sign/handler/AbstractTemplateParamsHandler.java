package com.noob.sign.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import com.noob.sign.domain.Customer;
import com.noob.sign.domain.TemplateInput;

public abstract class AbstractTemplateParamsHandler implements TemplateParamsHandler {
	// 按线程缓存
	protected final ThreadLocal<Map<String, Object>> threadCache = new ThreadLocal<Map<String, Object>>() {
		public Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	public TemplateInput getTemplateInput() {
		return (TemplateInput) threadCache.get().get(TemplateDomain.templateInput);
	}

	@Override
	public Map<String, Object> handle(TemplateInput data) {
		threadCache.get().put(TemplateDomain.templateInput, data);
		Map<String, Object> paramsMap;
		try {
			paramsMap = buildParams(data);
		} finally {
			threadCache.remove(); // 一定要清空线程缓存
		}

		return paramsMap;
	}

	/**
	 * 获取域对象.
	 * <p>
	 * 使用BeanIntrospector,依据于类的Setter和Getter方法，可以获取到类的描述符。 注意子类的访问权限要public！
	 */
	protected Map<String, Object> buildParams(TemplateInput data) {
		Map<String, Object> result = new HashMap<>();

		String templateParams = data.getTemplateParams();
		if (StringUtils.isBlank(templateParams)) {
			return result;
		}

		String[] params = templateParams.split(",");
		for (String paramName : params) {
			if (StringUtils.isBlank(paramName)) {
				continue;
			}
			paramName = paramName.trim();

			String[] paramDomain = paramName.split("\\.");
			if (paramDomain.length > 2) {
				throw new IllegalArgumentException("不正确的参数名：" + paramName);
			}

			Object domainObject = null;
			try {
				domainObject = paramDomain.length == 1 ? PropertyUtils.getProperty(this, getDefaultDomain())
						: PropertyUtils.getProperty(this, paramDomain[0]);
			} catch (Exception e) {
				throw new IllegalArgumentException("获取" + getDefaultDomain() + "对象异常", e);
			}

			try {
				String domainPropName = paramDomain.length == 1 ? paramDomain[0] : paramDomain[1];
				Object value = domainObject == null ? StringUtils.EMPTY
						: PropertyUtils.getProperty(domainObject, domainPropName);

				result.put(paramName, value);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}

		return result;
	}

	/**
	 * 用户信息
	 * 
	 * @return
	 */
	protected Customer getCustomer(String certificateNo, Long customerId) throws Exception {
		return getDomain(() -> {
			Customer customer = new Customer(); // 这里写真正获取Customer数据方法
			customer.setId(customerId);
			customer.setCertificateNo(certificateNo);
			customer.setCustomerName("雄霸");
			return customer;
		}, TemplateDomain.customer);
	}

	/**
	 * 获取指定的域对象，并缓存
	 */
	protected <T> T getDomain(Supplier<T> realSupplier, String key) {
		Map<String, Object> cache = threadCache.get();
		T data = (T) cache.get(key);
		if (data != null) {
			return data;
		}
		data = realSupplier.get();
		if (data != null) {
			threadCache.get().put(key, data);

		}
		return data;
	}

	@Override
	public <T> void setDefaultDomain(T domain) {
		setDomian(domain, getDefaultDomain());
	}

	public <T> void setDomian(T domain, String key) {
		if (domain != null) {
			Map<String, Object> cache = threadCache.get();
			cache.put(key, domain);
		}
	}

	protected abstract String getDefaultDomain();

}
