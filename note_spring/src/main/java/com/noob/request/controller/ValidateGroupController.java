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
 * <p>
 *  对于 post 请求 @RequestBody 的方式而言， 如果符合 【@Validated 修饰类 + @Valid 修饰入参 】的要求，
 *  它在 RequestResponseBodyMethodProcessor 里校验之后也还是会再进入 MethodValidationInterceptor 的拦截逻辑里，它两是独立不相关的。
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


    /**
     * 1、 @Valid或@Validate的参数后必须紧挨着一个BindingResult参数，否则spring会在校验不通过时直接抛出异常
     *    因为 ： RequestResponseBodyMethodProcessor#resolveArgument: 使用WebDataBinderFactory#createBinder创建的WebDataBinder里面持有了BindingResult对象，它绑定了当前请求入参对象和BindingResult一一对应。
     *           当解析参数过程校验失败时的具体错误信息由BindingResult保存，把它以MODEL_KEY_PREFIX 写入到ModelAndViewContainer存起来。 由 ErrorsMethodArgumentResolver 再从ModelAndViewContainer以 MODEL_KEY_PREFIX查找拿出BindingResult当做Controller层方法入参返回。
     * 2、 An Errors/BindingResult argument is expected to be declared immediately after the model attribute, the @RequestBody or the @RequestPart arguments！ 相当于来说是入参一定要是 复杂对象！
      */
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
