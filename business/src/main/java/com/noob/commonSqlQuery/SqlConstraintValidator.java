package com.noob.commonSqlQuery;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlConstraintValidator implements ConstraintValidator<SqlValid, CharSequence> {
    private static final String badStr = "script|and|exec|execute|insert|select|delete|update|count|drop|into|chr|mid|master|truncate|" +

            "char|declare|sitename|net user|xp_cmdshell|or|like|and|exec|execute|insert|create|drop|" +

            "table|from|grant|use|group_concat|column_name|" +

            "information_schema|table_schema|union|where|select|delete|update|order|by|count|" +

            "or|;|--|\\+|,|\\*|like|\\//|\\/|%|#";//过滤掉的sql关键字
    private final Pattern pattern = Pattern.compile(badStr);

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        if (charSequence == null) {
            return true;
        }
        String str = charSequence.toString().trim();
        if (str.length() == 0) {
            return true;
        }
        return !pattern.matcher(str.toLowerCase()).find();
    }


    public static void main(String[] args) {
        Pattern pattern = Pattern.compile(badStr);
        Matcher matcher = pattern.matcher(" select ");
        System.out.println(matcher.find());
    }
}