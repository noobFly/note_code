package com.noob.controller.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.netflix.zuul.ZuulFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseZuulFilter extends ZuulFilter {

	protected String readAsString(InputStream inputStream, String charset) throws IOException {
		List<String> lines = IOUtils.readLines(inputStream, charset == null ? "UTF-8" : charset);
		StringBuilder stringBuilder = new StringBuilder();
		for (String line : lines) {
			stringBuilder.append(line);
		}

		return stringBuilder.toString();
	}

}
