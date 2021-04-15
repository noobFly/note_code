package com.noob.proxy.jdk;

public class EntityJdk implements IEntity {

	@Override
	public void test() {
		pre();
		System.out.println("real test.");

	}

	@Override
	public void pre() {
		System.out.println("real pre .");

	}

}