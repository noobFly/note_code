package com.noob.proxy.cglib;

/**
 * 私有方法直接编译报错, 因为私有权限只能本类使用;
 * <p>
 * no modifier 只有同Package内可用。
 * <p>
 * protected权限只能使用于方法或属性上，不能用于类上，没有这个修饰符权限; protected 可以在同 Package内被父类对象直接使用; protected所谓的可以被子类访问（在其他Package）, 需要new一个子类对象來调用， 即可以被子类通过继承的方式拿来使用 .
 */
class TestCglib {
	public static void main(String[] args) {
		EntityCglib entity = EntityCglib.getCglibInstance(new EntityInterceptor(new EntityCglib()));
		entity.testPublic();
		// entity.testdefault();
		// entity.testprivate();
		entity.teststatic();
		entity.testfinal();
		// entity.testProtected();
	}
}
