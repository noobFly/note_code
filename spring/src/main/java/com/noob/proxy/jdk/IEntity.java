package com.noob.proxy.jdk;

public interface IEntity {
	public static void testStatic() {
		 System.out.println("testStatic");
	}
	void test();

    default void testdefault() {
        System.out.println("testdefault");
    }

	void pre();
}
