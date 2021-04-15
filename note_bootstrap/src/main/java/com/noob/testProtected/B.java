package com.noob.testProtected;

public class B extends A {
	protected B() {
		super("B"); // 一定要调用父类带参数的构造器，因为父类不再有默认无参数构造器
	}

	public static void main(String[] args) {
		B b = new B();
		b.A_method();
		b.A_filed = "1";
		A a = new A("a");
		a.A_filed = "2";
		a.A_method();
	}
}
