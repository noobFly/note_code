package com.noob.test.controller;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.noob.BootstrapApplication;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BootstrapApplication.class)
public class BaseTest {
	@Autowired
	public MockMvc mockMvc;// 去掉javamedlody 才能有效启动mockMvc

	@Autowired
	public ApplicationContext applicationContext;

	@Before
	public void pre() {
		// 提前处理逻辑

	}
}
