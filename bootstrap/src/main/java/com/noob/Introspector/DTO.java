package com.noob.Introspector;

import lombok.Getter;

public class DTO {
	@Getter
	private Integer test1 = 0;

	public Integer test2 = 0;
	Integer test3 = 0;
	protected Integer test4 = 0;
	public final Integer test5 = 0;
}