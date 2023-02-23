
package com.noob.testThink;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.collect.Lists;
import com.noob.json.JSON;
import lombok.Data;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GroupingByDownstreamTest {
    @JsonFilter("ignoreVarFilter")
    @Data
    public static class Person {
        private String realName;
        // 任务类型
        private String taskType;
        private int time;
        private BigDecimal decimal;

    }

    public static void main(String[] args) {
        List<Person> list = Lists.newArrayList();
        for (int index = 0; index < 30; index++) {
            Person person = new Person();
            person.setRealName("RealName" + index % 4);
            person.setTaskType("TaskType" + index % 5);
            person.setTime(index % 6);
            person.setDecimal(new BigDecimal(person.getTime()));
            list.add(person);
        }
        // 累加的方式
        int a = list.stream().collect(Collectors.summingInt(Person::getTime));
        System.out.println(a);

        int b = list.stream().mapToInt(Person::getTime).sum();
        System.out.println(b);

        BigDecimal d = list.stream().map(Person::getDecimal).reduce(BigDecimal::add).get();
        System.out.println(d);

        //   list.stream().forEach(t->list.remove(t)); List和Map的stream里也不能直接remove!!! 都会expectedModCount<初始实例化迭代器就固定为修改次数modCount>和modCount(remove操作会++)不一致抛出ConcurrentModificationException

        // 把list转成1:1的map.
       //  Map<String, Person> e = list.stream().collect(Collectors.toMap(Person::getRealName, Function.identity()));   //  这里因为数据源重复问题会报错，如果想支持后覆盖前，可改写底层的mergeFunction
        /** Exception in thread "main" java.lang.IllegalStateException: Duplicate key GroupingByDownstreamTest.Person(realName=RealName0, taskType=TaskType0, time=0, decimal=0)
         at java.util.stream.Collectors.lambda$throwingMerger$0(Collectors.java:133)
         at java.util.HashMap.merge(HashMap.java:1254)
         at java.util.stream.Collectors.lambda$toMap$58(Collectors.java:1320)
         at java.util.stream.ReduceOps$3ReducingSink.accept(ReduceOps.java:169)
         **/
        Map<String, Person> e = list.stream().collect(Collectors.toMap(Person::getRealName, Function.identity(),(k1, k2)-> k1));
        System.out.println(JSON.toJSONString(e));

        // 验证JSON动态屏蔽某个参数
        System.out.println(JSON.toJSON(list, Person.class, "realName"));

        // RealName分组并排序 (用LinkedHashMap来保存Map插入的顺序)
        Map<String, List<Person>> collect = list.stream().sorted(Comparator.comparing(Person::getRealName).reversed()).
                collect(Collectors.groupingBy(Person::getRealName, LinkedHashMap::new, Collectors.toList()));
        System.out.println(JSON.toJSONString(collect));

        List<Person> c = collect.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        //再摊开所有的List
        System.out.println(JSON.toJSONString(c));

        // 先RealName分组 -> 只取taskType
        Map<String, List<String>> collect1 = list.stream().collect(Collectors.groupingBy(Person::getRealName,
                Collectors.mapping(Person::getTaskType, Collectors.toList())));
        System.out.println(JSON.toJSONString(collect1));

        // 先RealName分组 -> 再统计time
        Map<String, IntSummaryStatistics> collect2 = list.stream().collect(Collectors.groupingBy(Person::getRealName, Collectors.summarizingInt(Person::getTime)));
        System.out.println(JSON.toJSONString(collect2));

        Map<String, BigDecimal> collect9 = list.stream().collect(Collectors.groupingBy(Person::getRealName, Collectors.mapping(Person::getDecimal, Collectors.reducing(BigDecimal.ZERO, (x, m) -> x.add(m)))));
        System.out.println(JSON.toJSONString(collect9));

        // 相同key的value相加
        List<Map<String, BigDecimal>> list2 = com.google.common.collect.Lists.newArrayList(collect9, collect9);

        BinaryOperator<Map<String, BigDecimal>> mapBinaryOperator = (x, m) -> {
            x.forEach((key, value) -> m.compute(key, (key2, oldValue) -> { // Map的merge和compute很像，但merge需要传入的value不为空,否则merge会报错; 所以用compute更合适。
                if (oldValue == null) return value;
                if (value == null) return oldValue;
                return value.add(oldValue);
            }));
            return m;  // 返回最全的map
        };
        Map<String, BigDecimal> collect15 = list2.stream().reduce(mapBinaryOperator).orElse(null);
        System.out.println(JSON.toJSONString(collect15));

        // 先RealName分组 -> 再计数
        Map<String, Long> collect8 = list.stream().collect(Collectors.groupingBy(Person::getRealName, Collectors.counting()));
        System.out.println(JSON.toJSONString(collect8));

        // 先RealName分组 -> 再取最大/最小的time
        Map<String, Person> collect3 = list.stream().collect(Collectors.groupingBy(Person::getRealName,
                Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(Person::getTime)), Optional::get)));
        System.out.println(JSON.toJSONString(collect3));

        // RealName分组 -> TaskType分组 -> 按time排序
        Map<String, Map<String, TreeSet<Person>>> collect4 = list.stream().collect(Collectors.groupingBy(Person::getRealName,
                Collectors.groupingBy(Person::getTaskType,
                        Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<Person>(Comparator.comparing(Person::getTime)
                        )), Function.identity()))));
        System.out.println(JSON.toJSONString(collect4));

    }

}
