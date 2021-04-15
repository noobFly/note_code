package com.noob.overflow;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * -XX:MetaspaceSize=3M -XX:MaxMetaspaceSize=10M
 * <p>
 * Compressed class space:  https://www.zhihu.com/question/268392125
 *
 */
public class MetaSpaceError extends ClassLoader {
	public void handler() {
		System.out.println("handler");
	}
	
	/**
	 * Exception in thread "main" java.lang.OutOfMemoryError: Compressed class space
	at java.lang.ClassLoader.defineClass1(Native Method)
	at java.lang.ClassLoader.defineClass(Unknown Source)
	at java.lang.ClassLoader.defineClass(Unknown Source)
	at com.noob.overflow.MetaSpaceError.createClasses(MetaSpaceError.java:37)
	at com.noob.overflow.MetaSpaceError.main(MetaSpaceError.java:44)

	 * @return
	 * @throws InterruptedException
	 */
	public static List<Class<?>> compressedClassSpaceError() throws InterruptedException {
		// 类持有
		List<Class<?>> classes = new ArrayList<Class<?>>();
		// 循环1000w次生成1000w个不同的类。
		for (int i = 0; i < 10000000; ++i) {
			Thread.sleep(1);
			ClassWriter cw = new ClassWriter(0);
			// 定义一个类名称为Class{i}，它的访问域为public，父类为java.lang.Object，不实现任何接口
			cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, "Class" + i, null, "java/lang/Object", null);
			// 定义构造函数<init>方法
			MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			// 第一个指令为加载this
			mw.visitVarInsn(Opcodes.ALOAD, 0);
			// 第二个指令为调用父类Object的构造函数
			mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			// 第三条指令为return
			mw.visitInsn(Opcodes.RETURN);
			mw.visitMaxs(1, 1);
			mw.visitEnd();
			MetaSpaceError test = new MetaSpaceError();
			byte[] code = cw.toByteArray();
			// 定义类
			Class<?> exampleClass = test.defineClass("Class" + i, code, 0, code.length);
			classes.add(exampleClass);
		}
		return classes;
	}
	
/**
 * Exception in thread "main" java.lang.OutOfMemoryError: Metaspace
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Unknown Source)
	at net.sf.cglib.core.ReflectUtils.defineClass(ReflectUtils.java:386)
	at net.sf.cglib.core.AbstractClassGenerator.create(AbstractClassGenerator.java:219)
	at net.sf.cglib.proxy.Enhancer.createHelper(Enhancer.java:377)
	at net.sf.cglib.proxy.Enhancer.create(Enhancer.java:285)
	at com.noob.overflow.MetaSpaceError.metaspaceError(MetaSpaceError.java:87)
	at com.noob.overflow.MetaSpaceError.main(MetaSpaceError.java:92)
 */
	public static void metaspaceError() {
		  int i = 0;
	        while (true) {
	            System.out.println(i++);
	            Enhancer enhancer = new Enhancer();
	            enhancer.setSuperclass(MetaSpaceError.class);
	            enhancer.setUseCache(false);
	            enhancer.setCallback(
	                    new MethodInterceptor() {
	                        @Override
	                        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
								return methodProxy.invokeSuper(o, null);
	                        }
	                    }
	            );
	            System.out.println(enhancer.create() instanceof MetaSpaceError);
	        }
	}
	
	public static void main(String[] args) throws InterruptedException {
		// metaspaceError();
		
		compressedClassSpaceError();
	}

}
