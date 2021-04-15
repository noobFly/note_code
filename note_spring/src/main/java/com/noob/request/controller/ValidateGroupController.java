package com.noob.request.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;

import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noob.request.Interceptor.OpLog;
import com.noob.request.component.BService;
import com.noob.request.controller.ValidateGroupController.GroupTestDTO.InitAction;
import com.noob.request.controller.ValidateGroupController.GroupTestDTO.MegreAction;
import com.noob.spring.beanDefinition.ITest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 验证校验分组
 * 
 * @author admin
 *
 */

@RestController
@RequestMapping("/validate")
public class ValidateGroupController {
	//@Resource
	BService bService;
	@Resource
	ITest itest;

	@RequestMapping("/testGroupDefault")
	public String testGroupDefault(@RequestBody @Validated GroupTestDTO test) {
		return "testGroupDefault";
	}

	@RequestMapping("/testGroupParent")
	public String testGroupNormal(@RequestBody @Validated(InitAction.class) GroupTestDTO test) {
		return "testGroupParent";
	}

	@RequestMapping("/testGroupExtends")
	public String testGroupExtends(@RequestBody @Validated(MegreAction.class) GroupTestDTO test) {
		return "testGroupExtends";
	}

	@OpLog(model = 99)
	@RequestMapping("/testAdvice")
	public String testAdvice(@RequestBody GroupTestDTO test, HttpServletRequest requestXX) {
		requestXX.getParameterMap(); //只能拿到queryString方式的传参。
		requestXX.getAttributeNames(); // 先setAttribute才有getAttribute
		return bService.testAdvice();
	}

	@NoArgsConstructor
	@AllArgsConstructor
	public static class GroupTestDTO {
		@Length(max = 1, message = "address错误")
		public String address;
		@Length(max = 1, message = "name错误", groups = { Default.class, InitAction.class })
		public String name;
		@Length(max = 1, message = "code错误", groups = { InitAction.class })
		public String code;
		@Length(max = 1, message = "phone错误", groups = { MegreAction.class })
		public String phone;

		public interface InitAction {
		}

		public interface MegreAction extends InitAction {
		}
	}

}
