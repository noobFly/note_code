package com.noob.testProtected.other;

import com.noob.testProtected.A;

/**
 * 结论：
 * <P>
 * protected比无修饰词时多支持的是： 不在用一个包内时，只能通过子类继承父类后new子类的实例来访问protected成员方法或者变量。
 * <P>
 * 
 * @param args
 */
public class C extends A {
	protected C() {
		super("C"); // 可以在构造器中访问父类protected的构造函数
	}

	public static void main(String[] args) {
		C b = new C();
		b.A_method();
		b.A_filed = "1";
		/*
		 * A a = new A("a"); a.A_filed = "2"; a.A_method();
		 */
	}
}
