package com.noob.sign;

import java.util.Map;

import com.noob.sign.domain.ApplyLimit;

/**
 * 
 * 合同签署.
 */
public interface SignService {

	public Map<String, Object> sign(ApplyLimit domain, String templateParameters) throws Exception;
}
