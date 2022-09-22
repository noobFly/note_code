package com.noob.sign.handler;

import java.util.Map;

import com.noob.sign.domain.ApplyLimit;
import com.noob.sign.domain.Customer;
import com.noob.sign.domain.TemplateInput;

/**
 * eg. customer.customerName，对应pdf模板中的${customer.customerName}
 * <p>
 * 默认域对象下的属性可以不指定域，eg.applyLimit.channel跟channel(默认域为applyLimit)意义是一样的
 * <p>
 * 添加其它域参数({@link TemplateDomain} )添加一个get{Domain}的方法即可
 * <p>
 * 
 *
 */
public class ApplyLimitTemplateParamsHandler extends AbstractTemplateParamsHandler {

	protected String getDefaultDomain() {
		return TemplateDomain.applyLimit;
	};

	public ApplyLimit getApplyLimit() throws Exception {

		return getDomain(() -> {
			Map<String, Object> cache = threadCache.get();
			TemplateInput input = (TemplateInput) cache.get(TemplateDomain.templateInput);
			ApplyLimit applyLimit = new ApplyLimit(); // 这里写真是获取域对象的方法
			applyLimit.setCertificateNo("440441111111");
			applyLimit.setFlowNo(input.getFlowNo());
			applyLimit.setChannel(input.getChannel());
			applyLimit.setCustomerId(100L);
			return applyLimit;
		}, TemplateDomain.applyLimit);
	}

	public Customer getCustomer() throws Exception {
		ApplyLimit applyLimit = getApplyLimit();
		return getCustomer(applyLimit.getCertificateNo(), applyLimit.getCustomerId());
	}

}
