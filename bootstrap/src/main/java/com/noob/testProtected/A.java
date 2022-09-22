package com.noob.testProtected;

public class A {
	protected String A_filed;
	
	public A() {
		
	}

	protected A(String filed) {
		A_filed = filed;
	}

	protected void A_method() {
		System.out.println("123123");
	}

	// protected 修饰的static 方法 在本包内 或 【其他包内的子类】 都能使用
	protected static void static_method() {
		System.out.println("this is protected static method");
	}
}
