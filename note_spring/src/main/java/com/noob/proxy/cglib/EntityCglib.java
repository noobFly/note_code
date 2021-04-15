package com.noob.proxy.cglib;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;

public class EntityCglib {
	public void pre() {
		System.out.println(" pre ");
	}

	public void testPublic() {
		pre();
		testprivate();
		System.out.println(" testPublic.");
	}

	private void testprivate() {
		System.out.println("testprivate.");
	}

	public final void testfinal() {
		System.out.println("testfinal.");
	}

	protected void testProtected() {
		System.out.println("testProtected.");
	}

	public static void teststatic() {
		System.out.println("teststatic.");
	}

	 void testdefault() {
		System.out.println("testdefault.");
	}

	public static EntityCglib getCglibInstance(MethodInterceptor proxy) {
		Enhancer en = new Enhancer();
		en.setSuperclass(EntityCglib.class);
		en.setCallbacks(new Callback[] { proxy, NoOp.INSTANCE });
		en.setCallbackFilter(new EntityCallbackFilter());
		return (EntityCglib) en.create();
	}

}
