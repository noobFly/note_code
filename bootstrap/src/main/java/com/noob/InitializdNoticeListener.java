package com.noob;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializdNoticeListener implements ApplicationListener<ContextRefreshedEvent> {

	public static String notice = String.join("\n",

			"******************************************************",
			"***                                                ***",
			"***                {}  START SUCCESS!              ***",
			"***                                                ***",
			"******************************************************");

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		boolean context = event.getApplicationContext().getParent() != null;
		log.info(notice, context ? "【PARENT_CONTEXT】" : "【MVC_CONTEXT】");
	}
}
