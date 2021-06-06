package com.noob.sign;

import java.util.Map;

import com.noob.sign.domain.ApplyLimit;

/**
 *用LibreOffice将Office文档转换PDF：  https://segmentfault.com/a/1190000015129654?utm_source=channel-hottest
 * 
 * 合同签署.
 */
public interface SignService {

	public Map<String, Object> sign(ApplyLimit domain, String templateParameters) throws Exception;
}
