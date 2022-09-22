package com.noob.request.component;

import org.springframework.transaction.annotation.Transactional;

public interface ITestTransactionOnInterfaceService {
	@Transactional
	void testTransactionOnInterface();
	@Transactional
	default void testInterfaceDefault() {
		
		System.out.println("default");
	};
}
