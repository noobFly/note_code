package com.noob.dataCheck;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.WorkbookUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.noob.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


// 数据稽查
@RestController
@Slf4j
@RequestMapping("/business/dataCheck")
public class DataCheckController {

    @Resource
    DataCheckConfigCreator dataCheckConfigCreator;

    private final int LIMIT = 5000;
    private final String SYMBOL = " &^& ";

    /**
     * 上传后即刻开始比对
     */
    @PostMapping(value = "/check")
    public Map<Integer, Map<String, Object>> check(@RequestParam(value = "topic", required = false) String topic, @RequestParam(value = "clear", required = false) boolean clear, @RequestPart("file") MultipartFile file) throws Exception {
        Instant instant = Instant.now();
        Map<Integer, Map<String, Object>> result = new HashMap<>();

        DataCheckConfig sheetConfig = getDataCheckConfig(topic, clear);// 表、字段映射关系;

        Workbook workbook = WorkbookUtil.createBook(file.getInputStream());

        String date = TimeUtil.dateTime(new Date());

        List<DataCheckTableMapping> tableMappings = sheetConfig.getTableMappings();
        tableMappings.forEach(tableMapping -> {

            String logMsg;
            String sheetName = tableMapping.getSheetName();
            /**
             * 正常情况下，只有1个head行：
             *  ExcelReader reader = new ExcelReader(workbook, sheetName);
             *  List<Map<String, Object>> uploadInfoList = reader.read(tableMapping.getHeadIndex(), tableMapping.getDataStartIndex(), LIMIT); //TODO 实际的行号其实就是list内数据的顺序; 或者扩展CellHandler来额外保存<index,value>值
             */

            // 支持多行head
            Sheet sheet = workbook.getSheet(sheetName);
            MapSheetMergeHeadReader reader = new MapSheetMergeHeadReader(tableMapping.getHeadIndexList(), tableMapping.getDataStartIndex(), LIMIT);
            List<Map<String, Object>> uploadInfoList = reader.read(sheet);


            Map<String, Object> checkItemMap = new HashMap<>();

            if (CollectionUtils.isEmpty(uploadInfoList)) {
                logMsg = "excel表内无数据";
            } else {
                CheckResult checkResults = null;
                List dbList = null;
                if (tableMapping.getTopic().equals(DataCheckConfigCreator.DataCheckTopic.PROJECT.getTopic())) {
                    dbList = getDbResource();
                }
                checkResults = check(uploadInfoList, dbList, tableMapping);


                checkResults.setDate(date);
                checkResults.setSheetName(sheetName);

                checkItemMap.put("diff", checkResults);
                logMsg = checkResults.getDesc();
            }
            log.info("------ {} 数据稽查完成.----- \n {}", sheetName, logMsg);

            checkItemMap.put("msg", logMsg);
            result.put(tableMapping.getTopic(), checkItemMap);

        });

        log.info("数据稽查完成. cost:{}ms.", Duration.between(instant, Instant.now()));
        return result;
    }

    private <T> CheckResult check(List<Map<String, Object>> outerDataList, List<T> innerDataList, DataCheckTableMapping tableMapping) {
        CheckResult finalResult = new CheckResult();
        finalResult.setUploadCount(size(outerDataList));
        finalResult.setDbCount(size(innerDataList));


        List<DataCheckColumnMapping> primaryKeyColumns = tableMapping.getPrimaryKeyColumns();
        List<DataCheckColumnMapping> columnMappings = tableMapping.getColumnMappings();

        Map<String, T> innerDataMap = innerDataList.stream().
                collect(Collectors.toMap(data -> getPrimaryKey(data, primaryKeyColumns), Function.identity(), (k1, k2) -> k1));
        List<CheckResult.CheckDetail> outerErrorList = Lists.newArrayList(); // 外部无效
        List<CheckResult.CheckDetail> outerOverList = Lists.newArrayList(); // 外部超出的
        List<CheckResult.CheckDetail> matchFailList = Lists.newArrayList(); // 数据比对失败的
        List<CheckResult.CheckDetail> dbOverList = Lists.newArrayList(); // 数据比对失败的
        List<CheckResult.CheckDetail> allList = Lists.newArrayList();

        int successCount = 0;

        // 先以外侧数据为源
        Iterator<Map<String, Object>> outerIterator = outerDataList.iterator();
        while (outerIterator.hasNext()) {
            Map<String, Object> outerData = outerIterator.next();
            String outKey = getPrimaryKey(outerData, primaryKeyColumns);
            if (Strings.isNullOrEmpty(outKey)) {
                outerErrorList.add(CheckResult.failDetail(CheckResult.FailType.OUT_UNVALID, null, String.format("Excel数据无主键")));
            } else {
                T innerData = innerDataMap.get(outKey);
                if (innerData == null) {
                    outerOverList.add(CheckResult.failDetail(CheckResult.FailType.OUT_OVER, outKey, "本地表内无该数据"));
                } else {
                    // 比对数据内容， 无论结果都从inner结果集里删除
                    CheckResult.CheckDetail result = compare(outKey, outerData, innerData, columnMappings);
                    if (!result.isSuccess()) {
                        matchFailList.add(result);
                    } else {
                        successCount++;
                    }

                    innerDataMap.remove(outKey);
                }
            }
            outerIterator.remove();
        }

        // 本地数据溢出
        if (innerDataMap.size() > 0) {
            dbOverList = innerDataMap.entrySet().stream().map(entry -> CheckResult.failDetail(CheckResult.FailType.DB_OVER, entry.getKey(), "Excel表内无该数据")).collect(Collectors.toList());
        }

        finalResult.setSuccessCount(successCount);
        finalResult.setOuterErrorCount(size(outerErrorList));
        finalResult.setMatchFaiLCount(size(matchFailList));
        finalResult.setInnerOverCount(size(dbOverList));
        finalResult.setOuterOverCount(size(outerOverList));
        allList.addAll(matchFailList);
        allList.addAll(dbOverList);
        allList.addAll(outerOverList);
        finalResult.setFailList(allList);

        return finalResult;
    }

    private int size(List list) {
        return list == null ? 0 : list.size();
    }


    private <T> CheckResult.CheckDetail compare(String key, Map<String, Object> outerDataMap, T innerDataElement, List<DataCheckColumnMapping> columnMappings) {
        List<CheckResult.Diff> diffList = Lists.newArrayList();
        List<String> failMsgList = Lists.newArrayList();
        columnMappings.stream().forEach(column -> {
            String title = column.getTitle();
            Object outerData = outerDataMap.get(title);
            Object innerData = getVal(innerDataElement, column);

            DataCheckConfig.DataTypeEnum dataTypeEnum = DataCheckConfig.DataTypeEnum.valueOf(column.getDataType());
            Map<String, String> extraPropertiesMap = column.getProperties();
            String outerStr = dataTypeEnum.clear(outerData, extraPropertiesMap);
            String innerStr = dataTypeEnum.clear(innerData, extraPropertiesMap);

            boolean outerNull = Strings.isNullOrEmpty(outerStr);
            boolean innerNull = Strings.isNullOrEmpty(innerStr);
            if (!(outerNull && innerNull)) {
                String msg = null;
                if (outerNull) {
                    msg = String.format("字段[%s]Excel数据为空, 表数据值: %s", title, innerData);
                } else if (innerNull) {
                    msg = String.format("字段[%s]表数据为空, excel数据值: %s", title, outerData);
                } else {
                    if (!dataTypeEnum.compare(outerStr, innerStr, extraPropertiesMap)) {
                        msg = String.format("字段[%s]：excel数据值：%s, 表数据值：%s", title, outerData, innerData);
                    }

                }
                if (!Strings.isNullOrEmpty(msg)) {
                    failMsgList.add(msg);
                    diffList.add(new CheckResult.Diff(column.getColumnName(), outerData, innerData));
                }
            }
        });

        return failMsgList.size() > 0 ? CheckResult.failDetailWithData(CheckResult.FailType.NOT_MATCH, key, diffList, failMsgList.stream().collect(Collectors.joining(";"))) : CheckResult.Success(key);

    }


    private <T> Object getVal(T innerDataElement, DataCheckColumnMapping column) {
        Method method = column.getGetter();
        try {
            return method.invoke(innerDataElement);
        } catch (Exception e) {
            throw new RuntimeException(String.format("方法%s反射获取对象属性值异常", method.getName()), e);
        }
    }

    // 主键一定是字符类型
    private String getPrimaryKey(Map<String, Object> data, List<DataCheckColumnMapping> primaryKeyColumns) {
        return getPrimaryKey(columnMapping -> data.get(columnMapping.getTitle()), primaryKeyColumns);
    }

    private <T> String getPrimaryKey(T data, List<DataCheckColumnMapping> primaryKeyColumns) {
        return getPrimaryKey(columnMapping -> getVal(data, columnMapping), primaryKeyColumns);
    }

    private <T> String getPrimaryKey(Function<DataCheckColumnMapping, Object> valFunc, List<DataCheckColumnMapping> primaryKeyColumns) {
        return primaryKeyColumns.stream().map(columnMapping ->
                DataCheckConfig.DataTypeEnum.valueOf(columnMapping.getDataType()).clear(valFunc.apply(columnMapping), columnMapping.getProperties())).filter(t -> !Strings.isNullOrEmpty(t)).collect(Collectors.joining(SYMBOL));
    }


    private DataCheckConfig getDataCheckConfig(String topic, boolean clearCache) {
        DataCheckConfig config = dataCheckConfigCreator.create(topic, clearCache);
        if (config == null || CollectionUtils.isEmpty(config.getTableMappings())) {
            throw new RuntimeException(topic + "数据稽查的配置为空, 请配置！");
        }
        return config;
    }


    //TODO 拿数据库表数据
    private List<DataEntity> getDbResource() {
        return Lists.newArrayList();
    }
}