package com.noob.testThink;

public class TestSwitch {
    public static void  main(String args[]) {

        String  s = "a";
        /**
         *  switch 匹配的表达式可以是：
         *     byte、short、char、int类型及这4种类型的包装类型；
         *     枚举类型；
         *     String 类型；
         * case 匹配的表达式可以是：
         *     常量表达式；
         *     枚举常量；
         *
         * 	case提供了switch表达式的入口地址，一旦switch表达式与某个case分支匹配，则从该分支的语句开始执行，
         * 	    其后的所有case分支的语句也会被执行，直到遇到break、return语句。
         */
        switch (s) {
            case "a": //a分支
                System.out.println("匹配成功a");
            case "b": //b分支
                System.out.println("匹配成功b");
                return;
            case "c": //c分支
                System.out.println("匹配成功c");
                break;
            case "d": //d分支
                System.out.println("匹配成功d");
                break;
            default:
                System.out.println("匹配成功default");
                break;
        }
    }
}
