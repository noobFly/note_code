package com.noob.responseAutoWrapper;

import java.lang.annotation.*;

/**
 * 自动包装设置<br>
 *
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoWrap {
	/**
	 * 是否自动包装，默认为true
	 * @return
	 */
	boolean value()	default true;
}
