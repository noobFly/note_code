package com.noob.validateCustomize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotBlankMValidator implements ConstraintValidator<NotBlankM, CharSequence> {

    public NotBlankMValidator(){
        System.out.println("12");
    }
    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        if (charSequence == null) {
            return false;
        }
        return charSequence.toString().trim().length() > 0;
    }
}