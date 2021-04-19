package com.noob.testProtected.other;

import com.noob.testProtected.A;

/**
 * 结论： 不在同一包内的子类时
 * <P>
 * protected比[无修饰词]时多支持的是： 只能通过子类继承父类后new子类的实例来访问protected成员方法或者变量。 静态方法通过[类型.方法]的形式都能访问
 * <P>
 * 无法直接在子类成员方法里实例化父类protected的构造函数, 只能在构造函数方法里
 * @param args
 */
public class C extends A {
	protected C() {
		super("C"); // 可以在构造器中访问父类protected的构造函数
		A.static_method();
	}

	public static void main(String[] args) {
		C b = new C();
		b.A_method();
		b.A_filed = "1";

		// A a = new A("a"); //编译报错 ！！  无法直接在子类成员方法里实例化父类protected的构造函数
		// a.A_filed = "2";
		// a.A_method();

	}
}
