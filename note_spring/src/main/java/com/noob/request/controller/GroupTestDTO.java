package com.noob.request.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.groups.Default;
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

    @Valid
    public List<GroupTestDTO> list;

    public interface InitAction {
    }

    public interface MegreAction extends InitAction {
    }
}