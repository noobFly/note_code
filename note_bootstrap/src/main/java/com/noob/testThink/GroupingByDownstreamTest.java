
package com.noob.testThink;

import com.noob.json.JSON;
import lombok.Data;
import org.assertj.core.util.Lists;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


public class GroupingByDownstreamTest {
    @Data
    public static class Person {

        private String realName;
        // 任务类型
        private String taskType;
        private int time;

    }

    public static void main(String[] args) {
        List<Person> list = Lists.newArrayList();
        for (int index = 0; index < 30; index++) {
            Person person = new Person();
            person.setRealName("RealName" + index % 4);
            person.setTaskType("TaskType" + index % 5);
            person.setTime(index % 6);
            list.add(person);
        }
        // RealName分组并排序 (用LinkedHashMap来保存Map插入的顺序)
        Map<String, List<Person>> collect = list.stream().sorted(Comparator.comparing(Person::getRealName).reversed()).
                collect(Collectors.groupingBy(Person::getRealName, LinkedHashMap::new, Collectors.toList()));
        System.out.println(JSON.toJson(collect));

        // 先RealName分组 -> 只取taskType
        Map<String, List<String>> collect1 = list.stream().collect(Collectors.groupingBy(Person::getRealName,
                Collectors.mapping(Person::getTaskType, Collectors.toList())));
        System.out.println(JSON.toJson(collect1));


        // 先RealName分组 -> 再统计time
        Map<String, IntSummaryStatistics> collect2 = list.stream().collect(Collectors.groupingBy(Person::getRealName, Collectors.summarizingInt(Person::getTime)));
        System.out.println(JSON.toJson(collect2));

        // 先RealName分组 -> 再取最大/最小的time
        Map<String, Person> collect3 = list.stream().collect(Collectors.groupingBy(Person::getRealName,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(Person::getTime)), Optional::get)));
        System.out.println(JSON.toJson(collect3));


        // RealName分组 -> TaskType分组 -> 按time排序
        Map<String, Map<String, TreeSet<Person>>> collect4 =  list.stream().collect(Collectors.groupingBy(Person::getRealName,
                Collectors.groupingBy(Person::getTaskType,
                        Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Person::getTime)
                        )), Function.identity()))));
        System.out.println(JSON.toJson(collect4));


    }


}
