package com.noob.Introspector;


import com.noob.json.JSON;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * 内省
 * <p>
 * PropertyDescriptor会通过解析public （含父类）的 Setter及Getter方法（任何一个匹配语法规范即可），合并解析结果最终得到对应的PropertyDescriptor实例。
 * <P>
 * 以 public的Getter、Setter成员方法 为准， 不管成员变量有没有。
 * <p>
 * 除了Bean属性的之外，还会带有一个属性名为class的PropertyDescriptor实例，它的来源是Class的getClass方法
 * <p> 约束条件：
 * Getter: 返回一定非void, 方法入参可选.
 * <p>
 * Setter: 返回一定是void, 一定要有方法入参.
 *
 */
public class TestIntrospector {
	public static String ignoreException(Supplier<String> supplier) {
		try {
			return supplier.get();
		} catch (Exception e) {
			return e.getClass().getName().toString();
		}
	}


	/**
	 * propertyDescriptors = {PropertyDescriptor[6]@621}
	 *  0 = {PropertyDescriptor@623} "java.beans.PropertyDescriptor[name=age; propertyType=int; writeMethod=public void com.noob.Introspector.TestIntrospector$University.setAge(int)]"
	 *  1 = {PropertyDescriptor@624} "java.beans.PropertyDescriptor[name=class; propertyType=class java.lang.Class; readMethod=public final native java.lang.Class java.lang.Object.getClass()]"
	 *  2 = {IndexedPropertyDescriptor@625} "java.beans.IndexedPropertyDescriptor[name=day; indexedPropertyType=int; indexedReadMethod=public int com.noob.Introspector.TestIntrospector$University.getDay(int)]"
	 *  3 = {PropertyDescriptor@626} "java.beans.PropertyDescriptor[name=parentAddress; propertyType=class java.lang.String; writeMethod=public void com.noob.Introspector.TestIntrospector$School.setParentAddress(java.lang.String)]"
	 *  4 = {PropertyDescriptor@627} "java.beans.PropertyDescriptor[name=parentAmount; propertyType=int; readMethod=public int com.noob.Introspector.TestIntrospector$School.getParentAmount()]"
	 *  5 = {PropertyDescriptor@628} "java.beans.PropertyDescriptor[name=title; propertyType=class java.lang.String; readMethod=public java.lang.String com.noob.Introspector.TestIntrospector$University.getTitle()]"
	 * @param args
	 */
	public static void main(String[] args) {
		University university = new University();
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(University.class);
			PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				String name = propertyDescriptor.getName();
				System.out.println("Name:   " + name);
				System.out.println("WriteMethod:   " + ignoreException(() -> {
					Method writeMethod = propertyDescriptor.getWriteMethod();
					writeMethod.setAccessible(true);
					try {
						writeMethod.invoke(university, 11);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return writeMethod.getName();
				}));
				System.out.println("ReadMethod:   " + ignoreException(() -> {
					Method readMethod = propertyDescriptor.getReadMethod();
					return readMethod.getName();
				}));
				System.out.println("=======================");
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
		System.out.println(JSON.toPrettyJSONString(university));
	}


	/**
		Name:   age
		WriteMethod:   setAge
		ReadMethod:   java.lang.NullPointerException
		=======================
		Name:   class
		WriteMethod:   java.lang.NullPointerException
		ReadMethod:   getClass
		=======================
		Name:   day
		WriteMethod:   java.lang.NullPointerException
		ReadMethod:   java.lang.NullPointerException
		=======================
		Name:   parentAddress
		WriteMethod:   setParentAddress
		ReadMethod:   java.lang.NullPointerException
		=======================
		Name:   parentAmount
		WriteMethod:   java.lang.NullPointerException
		ReadMethod:   getParentAmount
		=======================
		Name:   title
		WriteMethod:   java.lang.NullPointerException
		ReadMethod:   getTitle
		=======================


	 * 
	 */

	public static class School {
		protected int level;

		// success
		public int getParentAmount() {
			return 1;
		}

		// success
		public void setParentAddress(String str) {
			this.level = 2;
		}

		// protected fail
		protected int getParentCity() {
			return this.level;
		}
	}

	public static class University extends School {

		private String title;

		// success
		public void setAge(int age) {
			this.level = age;
		}

		// 非Setter语法 fail
		public void setParentNo() {

		}

		// 非Setter语法 fail
		public String setMonth(String no) {
			return no;
		}

		// success
		public int getDay(int no) {
			return no;
		}

		// 非严格Getter语法 fail
		public void getYear(int no) {

		}

		// success
		public String getTitle() {
			return title;
		}


		// private fail
		private int getLevel() {
			return this.level;
		}

		// default fail
		int getFun() {
			return 1;
		}

	}

}
