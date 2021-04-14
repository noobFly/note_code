package com.noob.testThink;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

public class TestReflect {
	public static void main(String[] args) throws Exception {
		testGeneric();
		testPolymorphic();

	}

	/**
	 * 验证多态， 运行时的后期动态绑定，都是用对象的实际类型去处理，会扫描匹配子类与父类的方法表进行匹配。  class是使用默认的Object.equals, 其实就是判定 ==
	 * <p>
	 * 只有final 、static、 private 和构造方法是前期静态绑定
	 * <p>
	 * 反射可以跳过编译时，对父类申明使用子类实例私有方法 的校验。
	 * <p>
	 * 两个相同类的不同实例的class对象是同一个. 对象的存储是有 对象头的， instanceOOPDesc对象。 有一个klass-oop指针指向了JVM
	 * metaSpace中的 CompressedClassSpace 的 klass对象，标识类的元数据（方法、属性、类签名等），
	 * 这个klass对象有个_java_mirror属性（oop）指向了class实例对象。
	 */
	private static void testPolymorphic() throws IllegalAccessException, InvocationTargetException {
		Storage a = new Storage();
		Storage b = new Storage();
		System.out.println(a.getClass() == b.getClass()); // true 两个相同父类的不同实例的class对象是同一个
		Storage c = new StorageChild();
		System.out.println(c.getClass().equals(b.getClass())); // false 多态 -> 动态绑定， 运行时知道实例的实际类型，会扫描匹配的方法或属性签名。
		Storage d = new StorageChild();
		System.out.println(d.getClass().getName()); // 实际类型 com.noob.testThink.TestReflect$StorageChild
		System.out.println(c.getClass().equals(d.getClass())); // true
		Method[] methods = d.getClass().getDeclaredMethods(); // 实际类型是子类， 返回的是子类的方法
		for (Method method : methods) {
			method.setAccessible(true);
			method.invoke(c); // 可以正确执行， 虽然是 “父类引用指向子类实例”， 但依然可以通过反射跳过编译检查。
			method.invoke(d); // 正确执行。 Method只与实际的class有关， 和具体的实例无关。
			method.invoke(b); // Method内部绑定了来源Class类型， 执行会校验obj是否是该Class的实例。 异常：
								// java.lang.IllegalArgumentException: object is not an instance of declaring  class
		}
	}

	// 反射可以跳过泛型在编译期的校验。 通过反射可以写入不同类的对象。且查询时用Object也能正确被处理。
	private static void testGeneric() throws IllegalAccessException, InvocationTargetException {
		List<String> list = Lists.newArrayList();
		Method[] listMethod = list.getClass().getMethods();
		for (Method method : listMethod) {
			if (method.getName().equals("add")) {
				method.invoke(list, 0, new Storage()); // 可以执行。 因为泛型校验只在编译阶段， 编译完成后会擦除。
				method.invoke(list, 1, new Integer(1)); // 可以执行。
				method.invoke(list, 2, "1"); // 可以执行。 当执行完成后看list的视图发现： 内部有一个Storage、一个Integer、一个String
				break;
			}
		}

		Object lista = list.get(0);
		Object listc = list.get(2);
		try {
			String listb = list.get(1); // 使用Object能正确得到结果，但如果使用String则报错： java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.String
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class StorageChild extends Storage {
		private void childMethod() {
			System.out.println("我是chid");
		}
	}
}
