package com.noob.testThink;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.Lists;

// 反射可以跳过编译时，父类申明就可以使用子类实例私有方法。
//  反射可以跳过泛型在编译期的校验。 通过反射可以写入不同类的对象。且查询时用Object也能正确被处理。
public class TestReflect {
	public static void main(String[] args) throws Exception {
		testGeneric();
		testPolymorphic();

	}

	/**
	 * 验证多态， 运行时的后期动态绑定，都是用对象的实际类型去处理，会扫描匹配子类与父类的方法表进行匹配。
	 * <p>
	 * 只有final 、static、 private 和构造方法是前期静态绑定
	 * <p>
	 * getMehtods、getFileds 首先扫描的是本类的public方法。
	 * static方法用反射invoke执行时，传任何对象都能正确执行，传null都行！具体执行子类的还是父类的与其绑定的class属性有关
	 * <p>
	 * 静态属性和静态方法可以被继承， 也能被子类申明相同的给隐藏。
	 * <p>
	 * 重写的功能是：“重写”后子类的优先级要高于父类的优先级！，但是“隐藏”是优先父类的，因此不能实现多态！。
	 * <p>
	 * 两个相同类的不同实例的class对象是同一个. 对象的存储是有 对象头的， instanceOOPDesc对象。 有一个klass-oop指针指向了JVM
	 * metaSpace中的 CompressedClassSpace 的 klass对象，标识类的元数据（方法、属性、类签名等），
	 * 这个klass对象有个_java_mirror属性（oop）指向了class实例对象。
	 * 
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	private static void testPolymorphic()
			throws IllegalAccessException, InvocationTargetException, NoSuchFieldException, SecurityException {
		Storage a = new Storage();
		Storage b = new Storage();
		System.out.println(a.getClass() == b.getClass()); // true 两个相同父类的不同实例的class对象是同一个
		Storage c = new StorageChild();
		System.out.println(c.getClass()); // class com.noob.testThink.TestReflect$StorageChild 多态 -> 动态绑定，
											// 运行时知道实例的实际类型，会扫描匹配的方法或属性签名。
		System.out.println(c.test); // 父类的值 10 !
		c.testStatic(); // 父类的静态方法被执行！ 我是parent testStatic

		StorageChild d = new StorageChild();
		d.testStatic();//
		System.out.println(d.getClass().getName()); // 实际类型 com.noob.testThink.TestReflect$StorageChild
		System.out.println(c.getClass().equals(d.getClass())); // true class是使用默认的Object.equals, 其实就是判定 ==
		Method[] methods = d.getClass().getDeclaredMethods(); // 实际类型是子类， 返回的是子类的方法 [public static void
																// com.noob.testThink.TestReflect$StorageChild.childMethod()]
		for (Method method : methods) {
			method.setAccessible(true);
			method.invoke(c); // 可以正确执行， 虽然是 “父类引用指向子类实例”， 但依然可以通过反射跳过编译检查。
			method.invoke(d); //  yu正确执行。 Method只与实际的class有关， 和具体的实例无关。
			try {
				method.invoke(null); // 顺利执行的是子类静态方法，与invoke里面是什么类型的实例对象无关, 传null都行！！！
			} catch (Exception e) {
				e.printStackTrace(); // 非静态Method内部绑定了来源Class类型， 执行会校验obj是否是该类型的实例。 异常：//
										// java.lang.IllegalArgumentException: object is not an instance of declaring  class
			}
		}

		Field[] field = d.getClass().getFields(); // 父、子定义的field都有， 子类定义排在首位
		Field[] field2 = d.getClass().getDeclaredFields(); // 子类定义 [public int
															// com.noob.testThink.TestReflect$StorageChild.test]

		System.out.println(field);
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
			String listb = list.get(1); // 使用Object能正确得到结果，但如果使用String则报错： java.lang.ClassCastException:
										// java.lang.Integer cannot be cast to java.lang.String
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方法的重写规则：
	 * <p>
	 * 参数列表与被重写方法的参数列表必须完全相同。
	 * <p>
	 * 访问权限不能比父类中被重写的方法的访问权限更低
	 * <p>
	 * 声明为 static 的方法不能被重写，但是能够被再次声明。 声明为 final 的方法不能被重写。
	 * <p>
	 * 重写的方法能够抛出任何非强制异常，无论被重写的方法是否抛出异常。但是，重写的方法不能抛出新的强制性异常，或者比被重写方法声明的更广泛的强制性异常，反之则可以。
	 * <p>
	 * 子类和父类在同一个包中，那么子类可以重写父类所有方法，除了声明为 private 和 final 的方法。
	 * <p>
	 * 子类和父类不在同一个包中，那么子类只能够重写父类的声明为 public 和 protected 的非 final 方法。
	 * <p>
	 * 
	 * @author admin
	 *
	 */
	public static class StorageChild extends Storage {
		public int test = 1;

		// 父类与子类名称相同时， 修饰词static需要相同 权限子类需要 >= 父类 !! 这里private的话编译报错
		protected static void testStatic() throws RuntimeException {
			System.out.println("我是child  testStatic");
		}

		public void childMethod() {
			System.out.println("我是chid");
		}
	}
}
