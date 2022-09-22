package com.noob.testProtected.other;

import com.noob.testProtected.A;

/**
 * 结论： 不在同一包内的子类时
 * <P>
 * protected比[无修饰词]时多支持的是： 只能通过子类继承父类后, new子类的实例来访问protected成员方法或者变量。 静态方法通过[类型.方法]的形式都能访问
 * <P>
 * 无法直接在子类成员方法里实例化父类protected的构造函数, 只能在构造函数方法里
 */
public class C extends A {
// 如果在构造方法内有指定父类的其他构造器，那就不会走父类默认构造器； 否则的话一定会先执行父类的默认构造器
	protected C() {
		super("C"); // 在分属不同包路径下： 可以在子类构造器中访问父类protected的构造函数、成员变量和属性。 无论是否静态 ！ 如果是[缺省] 就都不可以
		A.static_method();
		A_method();
		A_filed = "1";
		System.out.print("CCC");
	}
	
	void local() {
		A_method();
	}

	public static void main(String[] args) throws Exception {
		C b = new C();
		b.A_method();
		b.A_filed = "1";

		 A a = new A();
		 //  A a = new A(""); 编译报错 ！！  无法直接在子类成员方法里实例化父类protected的构造函数
		//   a.A_filed = "2"; 编译报错 ！！ 无法在子类里实例化父类后直接使用它的protected , 但静态方法可以使用，它属于类方法
		  a.static_method();

	}
}
