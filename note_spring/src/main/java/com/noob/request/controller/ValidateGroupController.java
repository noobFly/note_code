package com.noob.request.controller;

import com.noob.request.Interceptor.OpLog;
import com.noob.request.component.BService;
import com.noob.validateCustomize.CustomGroupForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * 验证校验分组
 *
 * @author admin
 */

@RestController
@RequestMapping("/validate")
public class ValidateGroupController {
    @Resource
    BService bService;
    @Autowired
    private javax.validation.Validator jxValidator;
    @Autowired
    private org.springframework.validation.Validator springValidator;

    @RequestMapping("/test2")
    public String test2(@RequestBody GroupTestDTO test) {
        for (GroupTestDTO element : test.getList()) {
            Set<ConstraintViolation<Object>> validateResult = jxValidator.validate(element);
            if (validateResult.size() > 0) {
                ConstraintViolation<Object> fail = validateResult.stream().findFirst().get();
                System.out.println(fail.getPropertyPath() + fail.getMessage());
            }
        }

        return "success";
    }

    @RequestMapping("/test3")
    public String test3(@RequestBody GroupTestDTO test, BindingResult result) {
        springValidator.validate(test, result);
        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                System.out.println(error.getDefaultMessage());
            }
        }
        return "success";
    }


    @RequestMapping("/testGroupDefault")
    public String testGroupDefault(@RequestBody @Validated GroupTestDTO test) {
        return "testGroupDefault";
    }

    @RequestMapping("/testGroupParent")
    public String testGroupNormal(@RequestBody @Validated(GroupTestDTO.InitAction.class) GroupTestDTO test) {
        return "testGroupParent";
    }

    @RequestMapping("/testGroupExtends")
    public String testGroupExtends(@RequestBody @Validated(GroupTestDTO.MegreAction.class) GroupTestDTO test) {
        return "testGroupExtends";
    }

    @RequestMapping("/testGroupSequenceProvider")
    public String testGroupSequenceProvider(@RequestBody @Validated CustomGroupForm test) {
        return "GroupSequenceProvider";
    }

    @OpLog(model = 99)
    @RequestMapping("/testAdvice")
    public String testAdvice(@RequestBody GroupTestDTO test, HttpServletRequest requestXX) {
        requestXX.getParameterMap(); //只能拿到queryString方式的传参。
        requestXX.getAttributeNames(); // 先setAttribute才有getAttribute
        return bService.testAdvice();
    }


}
