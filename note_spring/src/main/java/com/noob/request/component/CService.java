package com.noob.request.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Service
public class CService {
	@Autowired
	public BService bService;
}
