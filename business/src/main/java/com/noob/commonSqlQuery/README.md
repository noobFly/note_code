
/**
 * 因为有sql注入风险所以在入参DTO的属性上都加了sql防御的校验器。
 * 本意是想支持通道式的简单查询, 部分支持简单的函数！支持去重取值！
 * <p>
 *   缺点:
 *    1、多表联合查询等比较复杂的操作无法支持！
 *    2、前端得注意属性值的写法。
 *    3、有sql注入的风险
 *    4、因为ibatis的默认实现：在PreparedStatementHandler#execute 执行完sql后 在DefaultResultSetHandler#handleResultSets-> #applyAutomaticMappings 根据字段属性映射给返回对象实例的属性setter值时，如果为null不操作
 *       所以当用Map(默认HashMap)做返回对象类型时，会出现: 如果值为空，map里会没该key!
 *
 * </p>
 *
 {
    "type": 1,
    "startPage": false,
    "sortConditionList": [
        {
            "order": "fund_project_name",
            "sort": "desc"
        },
        {
            "order": "fund_project_name",
            "sort": "asc"
        }
    ],
    "selectColumnList": [
        "substr(fund_project_name,7) as fund_project_name"
    ],
    "filterConditionList": [
        {
            "column": "fund_project_name",
            "value": "'中山贝森医疗产业投资企业（有限合伙）'",
            "type": "7"
        }
    ]
}
     
     
     // 模糊匹配取唯一值
     {
         "type": 1,
         "selectColumnList": [
             "distinct fund_project_name"
         ],
         "filterConditionList": [
             {
                 "column": "fund_project_name",
                 "type": 6,
                 "value": "科技"
             }
         ]
     }
     // 也可以用group
     
     {
         "type": 1,
         "selectColumnList": [
             "fund_project_name"
         ],
         "grpupColumnList": [
             "fund_project_name"
         ],
         "filterConditionList": [
             {
                 "column": "fund_project_name",
                 "type": 6,
                 "value": "科技"
             }
         ]
     }
     
 * </>
 */