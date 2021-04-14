package com.noob.test;

public class TestObjectPadding {

	public static void main(String[] args) throws InterruptedException {
		testPointer(new Pointer());
	}

	private static void testPointer(Pointer pointer) throws InterruptedException{
		long start  = System.currentTimeMillis();
		Thread t1 = new Thread(() -> {
			for(int i=0;i<1000000000;i++){
				pointer.x++;
			}
		});

		Thread t2 = new Thread(() -> {
			for(int i=0;i<1000000000;i++){
				pointer.y++;
			}
		});
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		System.out.println("cost ["+(System.currentTimeMillis()-start)+"] ms");
		System.out.println(pointer);
	}

 // -XX:-RestrictContended 与 @sun.misc.Contended 合用;  类前加上代表整个类的每个变量都会在单独的cache line中。 属性前加上时需要加上组标签，相同标签的分配在同一cache line中
	static class Pointer{
		@sun.misc.Contended("group1")
		volatile long x;
		// long p1, p2, p3, p4, p5, p6, p7;
		@sun.misc.Contended("group2")
		volatile long y;
		// long p8, p9, p10, p11, p12, p13, p14;

		@Override
		public String toString() {
			return "Pointer{" +
					"x=" + x +
					", y=" + y +
					'}';
		}
	}
}
