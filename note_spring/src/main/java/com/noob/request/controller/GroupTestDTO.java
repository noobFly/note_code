package com.noob.request.controller;

import com.noob.util.TimeUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupTestDTO {
    @Length(max = 1, message = "address错误")
    public String address;
    @Length(max = 1, message = "name错误", groups = {Default.class, InitAction.class})
    public String name;
    @Length(max = 1, message = "code错误", groups = {InitAction.class})
    public String code;
    @Length(max = 1, message = "phone错误", groups = {MegreAction.class})
    public String phone;
    @DateTimeFormat(pattern = TimeUtil.DATE_PATTERN) // 如果请求使用Get方式, 它无法传入Date类型， 需要增加DateTimeFormat注解将请求的String入参转为Date
    public Date time;
    @Valid
    public List<GroupTestDTO> list;

    public interface InitAction {
    }

    public interface MegreAction extends InitAction {
    }
}