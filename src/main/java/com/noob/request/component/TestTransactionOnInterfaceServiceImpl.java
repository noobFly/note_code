package com.noob.request.component;

import org.springframework.stereotype.Service;

@Service
public class TestTransactionOnInterfaceServiceImpl  implements  ITestTransactionOnInterfaceService{

	@Override
	public void testTransactionOnInterface() {
		testInterfaceDefault();
		System.out.println("testTransactionOnInterface");
		
	}

}
