package com.noob.request.component;

import com.noob.request.controller.GroupTestDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Random;

/**
 * 不支持 prototype 模式下的 field属性注入循环依赖
 */
//@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
@Validated
public class BService {
    // @Autowired
    public CService cService;

    public String testAdvice() {

        System.out.println("目标方法testAdvice");
        if (new Random().nextBoolean()) {
            throw new RuntimeException("fail");
        }
        return "testAdvice";
    }

    public String getMsg(@NotNull String brandCode) {
        return "test";
    }

    @Validated(value = {GroupTestDTO.InitAction.class})
    public String testAdvice2(@Valid GroupTestDTO test) {

        return "testAdvice";
    }

    /*
     * 不支持 构造器注入循环依赖
     */
    // public BService(CService a) { }
}
