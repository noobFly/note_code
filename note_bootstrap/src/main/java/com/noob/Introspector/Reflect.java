package com.noob.Introspector;

import java.lang.reflect.Field;

/**
 * AccessibleObject 类是 Field、Method 和 Constructor 对象的基类。它提供了将反射的对象标记为在使用时取消默认
 * Java 语言访问控制检查的能力。 对于公共成员、默认（打包）访问成员、受保护成员和私有成员，在分别使用 Field、Method 或
 * Constructor 对象来设置或获得字段、调用方法，或者创建和初始化类的新实例的时候，会执行访问检查。
 * <p>
 * Accessible并不是标识方法能否访问的,. public的方法 Accessible仍为false
 * <p>
 * setAccessible(boolean flag)
 * <p>
 * 将此对象的 accessible 标志设置为指示的布尔值。值为 true 则指示反射的对象在使用时应该取消 Java 语言访问检查。值为 false  则指示反射的对象应该实施 Java 语言访问检查。
 * 实际上setAccessible是启用和禁用访问安全检查的开关, 并不是为true就能访问为false就不能访问
 * <p>
 * 在同一个类下 private 可以不用setAccessible(true) . 此时isAccessible() 仍然是false
 * <p>
 * final 则都是需要的
 * 
 */
public class Reflect {
	private Integer test1 = 0;
	private final Integer test2 = 0;

	public static void main(String[] args) throws Exception {
		Reflect reflect = new Reflect();
		Field reflect_field = Reflect.class.getDeclaredField("test1"); // getDeclaredFiled 仅能获取类本身的属性成员（包括私有、共有、保护）
		reflect_field.set(reflect, 1);
		System.out.println(reflect_field.isAccessible()); // 仍然是false
		System.out.println(reflect.test1);// 在同一个类下, private 可以不用setAccessible(true)

		// reflect.test2 = 2; // The final field Reflect.test2 cannot be assigned
		testFinal(reflect);

		DTO entity = new DTO();
		testPrivate(entity);

		Field field2 = DTO.class.getField("test2"); // getField 仅能获取类及其父类 public属性成员
		field2.set(entity, 2); // public 不需要
		System.out.println(field2.isAccessible());
		System.out.println(entity.test2);

		Field field3 = DTO.class.getDeclaredField("test3");
		field3.set(entity, 3); // default 不需要
		System.out.println(field3.isAccessible());
		System.out.println(entity.test3);

		Field field4 = DTO.class.getDeclaredField("test4");
		System.out.println(field4.isAccessible());
		field4.set(entity, 4); // protected 不需要
		System.out.println(entity.test4);

		testFinal(entity);
	}

	/**
	 * Exception in thread "main" java.lang.IllegalAccessException: Can not set
	 * final java.lang.Integer field com.noob.Introspector.DTO.test5 to
	 * java.lang.Integer at
	 * sun.reflect.UnsafeFieldAccessorImpl.throwFinalFieldIllegalAccessException(Unknown
	 * Source) at
	 * sun.reflect.UnsafeFieldAccessorImpl.throwFinalFieldIllegalAccessException(Unknown
	 * Source) at sun.reflect.UnsafeQualifiedObjectFieldAccessorImpl.set(Unknown
	 * Source) at java.lang.reflect.Field.set(Unknown Source) at
	 * com.noob.Introspector.Reflect.main(Reflect.java:46)
	 */
	private static void testFinal(DTO entity) throws NoSuchFieldException, IllegalAccessException {
		Field field5 = DTO.class.getDeclaredField("test5");
		System.out.println(field5.isAccessible());

		field5.set(entity, 5); // final 需要
		System.out.println(entity.test5);
	}

	/**
	 * java.lang.IllegalAccessException: Class com.noob.Introspector.Reflect can not
	 * access a member of class com.noob.Introspector.DTO with modifiers "private"
	 * at sun.reflect.Reflection.ensureMemberAccess(Unknown Source) at
	 * java.lang.reflect.AccessibleObject.slowCheckMemberAccess(Unknown Source) at
	 * java.lang.reflect.AccessibleObject.checkAccess(Unknown Source) at
	 * java.lang.reflect.Field.set(Unknown Source) at
	 * com.noob.Introspector.Reflect.testFinal(Reflect.java:63) at
	 * com.noob.Introspector.Reflect.main(Reflect.java:31)
	 */
	private static void testPrivate(DTO entity) throws NoSuchFieldException, IllegalAccessException {
		Field field = DTO.class.getDeclaredField("test1");
		System.out.println(field.isAccessible()); // false
		try {
			field.set(entity, 1); // 不在同一个类下， private 需要setAccessible(true)
			System.out.println(entity.getTest1());
		} catch (Exception e) {
			e.printStackTrace();
		}

		field.setAccessible(true);
		System.out.println(field.isAccessible()); // true

		field.set(entity, 33);
		System.out.println(entity.getTest1());
	}

	/**
	 * java.lang.IllegalAccessException: Can not set final java.lang.Integer field
	 * com.noob.Introspector.Reflect.test2 to java.lang.Integer at
	 * sun.reflect.UnsafeFieldAccessorImpl.throwFinalFieldIllegalAccessException(Unknown
	 * Source) at
	 * sun.reflect.UnsafeFieldAccessorImpl.throwFinalFieldIllegalAccessException(Unknown
	 * Source) at sun.reflect.UnsafeQualifiedObjectFieldAccessorImpl.set(Unknown
	 * Source) at java.lang.reflect.Field.set(Unknown Source) at
	 * com.noob.Introspector.Reflect.testFinal(Reflect.java:89) at
	 * com.noob.Introspector.Reflect.main(Reflect.java:28)
	 */
	private static void testFinal(Reflect reflect) throws NoSuchFieldException, IllegalAccessException {
		Field reflect_field2 = Reflect.class.getDeclaredField("test2");
		System.out.println(reflect_field2.isAccessible());

		try {
			reflect_field2.set(reflect, 2); // 即使在同一个类下, final 也需要setAccessible(true)
			System.out.println(reflect.test2);

		} catch (Exception e) {
			e.printStackTrace();
		}
		reflect_field2.setAccessible(true);
		System.out.println(reflect_field2.isAccessible()); // true
		reflect_field2.set(reflect, 3);
		System.out.println(reflect.test2);

	}
}
