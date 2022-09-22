package com.noob.sign.impl;

import java.util.Map;

import com.noob.sign.SignService;
import com.noob.sign.domain.ApplyLimit;
import com.noob.sign.domain.TemplateInput;
import com.noob.sign.handler.ApplyLimitTemplateParamsHandler;
import com.noob.sign.handler.TemplateParamsHandler;

public class SignServiceImpl implements SignService {

	private TemplateParamsHandler handler = new ApplyLimitTemplateParamsHandler();

	@Override
	public Map<String, Object> sign(ApplyLimit domain, String templateParameters) throws Exception {
		TemplateInput input = new TemplateInput();
		input.setFlowNo(domain.getFlowNo());
		input.setChannel(domain.getChannel());
		input.setLoanType(domain.getLoanType());
		input.setTemplateParams(templateParameters);
		handler.setDefaultDomain(domain);
		return handler.handle(input);

	}

}
