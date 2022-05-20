package com.noob.validateCustomize;

import lombok.Data;
import org.hibernate.validator.group.GroupSequenceProvider;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
@GroupSequenceProvider(value = CustomSequenceProvider.class)
public class CustomGroupForm {

    @Pattern(regexp = "[A|B]", message = "类型必须为 A|B")
    private String type;
    @NotBlankM
    @NotEmpty(message = "参数A不能为空", groups = {WhenTypeIsA.class})
    private String paramA;

    @NotEmpty(message = "参数B不能为空", groups = {WhenTypeIsB.class})
    private String paramB;

    public interface WhenTypeIsA {
    }

    public interface WhenTypeIsB {
    }
}
