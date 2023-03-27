package com.noob.dataCheck;

import cn.hutool.core.date.StopWatch;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.WorkbookUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.noob.commonSqlQuery.CommonQueryDTO;
import com.noob.commonSqlQuery.CommonQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    @Resource
    CommonQueryHandler commonQueryHandler;

    private final int LIMIT = 5000;
    private final String SYMBOL = " &^& ";
    private final String COMMA = ",";


    /**
     * 上传后即刻开始比对
     */
    @GetMapping(value = "/check")
    public void check(@RequestParam(value = "topic") @NotBlank String topics, @RequestPart("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("开启{}的数据比对", topics);

        StopWatch stopWatch = new StopWatch();

        Map<Sheet, CheckResult> dataMap = Maps.newHashMap();
        try (Workbook workbook = WorkbookUtil.createBook(file.getInputStream())) {
            for (String param : topics.split(COMMA)) {
                int topic = Integer.parseInt(param);
                stopWatch.start(CommonQueryHandler.TableEnum.findTable(topic).name());

                DataCheckTableMapping tableMapping = dataCheckConfigCreator.create(topic);// 表、字段映射关系;

                String logMsg;
                String sheetName = tableMapping.getSheetName();
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new RuntimeException(String.format("[%s]在excel文件中不存在！请修改稽查配置后重试", sheetName));
                }
                int dataStartIndex = tableMapping.getDataStartIndex();

                MapSheetMergeHeadReader reader = new MapSheetMergeHeadReader(tableMapping.getHeadIndexList(), dataStartIndex, LIMIT);
                List<Map<String, Object>> uploadInfoList = reader.read(sheet);


                CheckResult checkResults = null;

                if (CollectionUtils.isEmpty(uploadInfoList)) {
                    logMsg = "excel内无数据";
                } else {
                    List<String> headList = reader.getHeaderList();

                    validateColumnConfig(tableMapping, sheetName, headList);

                    checkResults = check(uploadInfoList, queryDbData(topic), tableMapping);
                    checkResults.setSheetName(sheetName);
                    checkResults.setTableMapping(tableMapping);
                    checkResults.setHeadList(headList);
                    removeDataRow(dataStartIndex, sheet);

                    logMsg = checkResults.getDesc();

                }
                dataMap.put(sheet, checkResults);

                stopWatch.stop();
                log.info("------ {}数据稽查完成. cost:{}ms ----- \n {}", sheetName, stopWatch.getLastTaskTimeMillis(), logMsg);
            }

            try {
                writeFailReport(workbook, stopWatch, dataMap);
                downloadInit(request, response, "稽查报告文件.xls");
                workbook.write(response.getOutputStream());
            } catch (Exception e) {
                throw new RuntimeException("稽查报告文件生成异常", e);
            }
        }


        log.info("数据稽查全过程完成. cost:{}ms. \n {}", stopWatch.getTotalTimeMillis(), stopWatch.prettyPrint());
    }

    private void writeFailReport(Workbook workbook, StopWatch stopWatch, Map<Sheet, CheckResult> dataMap) {
        List<Map.Entry<Sheet, CheckResult>> dataEntryList = dataMap.entrySet().stream().filter(t -> t.getValue() != null && CollectionUtils.isNotEmpty(t.getValue().getFailList())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(dataEntryList)) {
            stopWatch.start("回写错误报告数据");
            log.info("=======回写错误报告数据 开始=======");

            ExcelWriter writer = new ExcelWriter(workbook, workbook.getSheetName(0));
            Font dataFont = workbook.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 20);
            writer.getCellStyle().setFont(dataFont);

            dataEntryList.forEach(t -> {
                writer.setSheet(t.getKey());
                List<String> headList = t.getValue().getHeadList();
                List<CheckResult.CheckDetail> failList = t.getValue().getFailList();
                List<DataCheckColumnMapping> columnMappings = t.getValue().getTableMapping().getPrimaryKeyColumns();
                List<String> primaryKeyColumns = columnMappings.stream().map(DataCheckColumnMapping::getTitle).collect(Collectors.toList());
                List<Map<String, Object>> reportDataList = failList.stream().filter(failRecord -> failRecord.getFailType() == CheckResult.FailType.NOT_MATCH).map(failRecord -> {

                    Map<String, Object> originData = failRecord.getOriginData();
                    List<CheckResult.Diff> diffList = failRecord.getDiffList();
                    originData.forEach((column, value) -> {
                        if (primaryKeyColumns.stream().noneMatch(key -> key.equals(column))) {
                            //非主键才处理, 无异常的数据清空，有异常的数据reportMsg覆盖
                            CheckResult.Diff diff = diffList.stream().filter(c -> c.getTitle().equals(column)).findAny().orElse(null);
                            if (diff == null) {
                                originData.put(column, null);
                            } else {
                                originData.put(column, diff.getReportMsg());
                            }
                        }
                    });

                    return originData;
                }).collect(Collectors.toList());

                reportDataList.add(new HashMap<>()); //在Excel里的换行

                failList.stream().filter(failRecord -> failRecord.getFailType() == CheckResult.FailType.OUT_OVER).forEach(failRecord -> {
                    Map<String, Object> originData = failRecord.getOriginData();
                    originData.forEach((column, value) -> {
                        if (primaryKeyColumns.stream().anyMatch(key -> key.equals(column))) {
                            //主键才补
                            originData.computeIfPresent(column, (key, oldValue) -> oldValue + "(系统无该数据)");
                        } else {
                            originData.put(column, null);
                        }
                    });
                    reportDataList.add(originData);
                });
                reportDataList.add(new HashMap<>()); //在Excel里的换行

                List<CheckResult.CheckDetail> dbOverList = failList.stream().filter(failRecord -> failRecord.getFailType() == CheckResult.FailType.DB_OVER).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(dbOverList)) {

                    failList.stream().filter(failRecord -> failRecord.getFailType() == CheckResult.FailType.DB_OVER).forEach(failRecord -> {
                        Map<String, Object> originData = failRecord.getOriginData();
                        // 内部数据结构转换成外部数据结构, 按主键在excel表内顺序构建
                        Map<String, Object> convertMap = new LinkedHashMap<>();
                        headList.forEach(head -> {
                                    DataCheckColumnMapping columnMapping;
                                    convertMap.put(head, (columnMapping = columnMappings.stream().filter(mapping -> mapping.getTitle().equals(head)).findAny().orElse(null)) != null ? originData.get(columnMapping.getColumnName()) + "(母表无该数据)" : null);
                                }
                        );
                        reportDataList.add(convertMap);
                    });
                }

                writer.setCurrentRow(t.getValue().getTableMapping().getDataStartIndex());
                writer.write(reportDataList, false).autoSizeColumnAll();

            });
            stopWatch.stop();
            log.info("[回写错误报告数据]结束. cost:{}ms.", stopWatch.getLastTaskTimeMillis());

        }
    }

    private void removeDataRow(Integer dataStartIndex, Sheet sheet) {
        // 清空数据行
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            if (i >= dataStartIndex) {
                Row row = sheet.getRow(i);
                if (row != null)
                    sheet.removeRow(sheet.getRow(i));
            }
        }
    }

    private List<Map<String, Object>> queryDbData(int topic) {
        CommonQueryDTO queryDTO = new CommonQueryDTO();
        queryDTO.setType(topic);
        queryDTO.setMergeDefaultFilterCondition(true);
        return commonQueryHandler.queryAdm(queryDTO);
    }

    // 比较标头和稽查字段的配置是否一致
    private void validateColumnConfig(DataCheckTableMapping tableMapping, String sheetName, List<String> headerList) {
        DataCheckColumnMapping notExistsPk;
        DataCheckColumnMapping notExistsColumn = null;
        if ((notExistsPk = tableMapping.getPrimaryKeyColumns().stream().filter(t -> !headerList.contains(t.getTitle())).findAny().orElse(null)) != null ||
                (notExistsColumn = tableMapping.getColumnMappings().stream().filter(t -> !headerList.contains(t.getTitle())).findAny().orElse(null)) != null) {
            throw new RuntimeException(String.format("稽查配置的字段[%s]在[%s]表内的标题列不存在", notExistsPk != null ? notExistsPk.getTitle() : notExistsColumn.getTitle(), sheetName));
        }
    }

    private CheckResult
    check(List<Map<String, Object>> outerDataList, List<Map<String, Object>> innerDataList, DataCheckTableMapping tableMapping) {
        CheckResult finalResult = new CheckResult();
        finalResult.setUploadCount(size(outerDataList));
        finalResult.setDbCount(size(innerDataList));


        List<DataCheckColumnMapping> primaryKeyColumns = tableMapping.getPrimaryKeyColumns();
        List<DataCheckColumnMapping> columnMappings = tableMapping.getColumnMappings();

        Map<String, Map<String, Object>> innerDataMap = innerDataList.stream().
                collect(Collectors.toMap(data -> getPrimaryKeyForInner(data, primaryKeyColumns), Function.identity(), (k1, k2) -> k1));
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
            String outKey = getPrimaryKeyForOuter(outerData, primaryKeyColumns);
            if (Strings.isNullOrEmpty(outKey)) {
                outerErrorList.add(CheckResult.fail(CheckResult.FailType.OUT_UNVALID, null, String.format("Excel数据无主键")));
            } else {
                Map<String, Object> innerData = innerDataMap.get(outKey);
                if (innerData == null) {
                    outerOverList.add(CheckResult.fail(CheckResult.FailType.OUT_OVER, outKey, "本地表内无该数据", outerData));
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
            dbOverList = innerDataMap.entrySet().stream().map(entry -> CheckResult.fail(CheckResult.FailType.DB_OVER, entry.getKey(), "Excel表内无该数据", entry.getValue())).collect(Collectors.toList());
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


    private CheckResult.CheckDetail compare(String key, Map<String, Object> outerDataMap, Map<String, Object> innerDataElement, List<DataCheckColumnMapping> columnMappings) {
        List<CheckResult.Diff> diffList = Lists.newArrayList();
        columnMappings.stream().forEach(column -> {
            String title = column.getTitle();
            Object outerData = outerDataMap.get(title);
            Object innerData = getValForInner(innerDataElement, column);

            DataTypeEnum dataTypeEnum = DataTypeEnum.valueOf(column.getDataType());
            Map<String, String> extraPropertiesMap = column.getProperties();
            String outerStr = dataTypeEnum.clear(outerData, extraPropertiesMap);
            String innerStr = dataTypeEnum.clear(innerData, extraPropertiesMap);

            boolean outerNull = Strings.isNullOrEmpty(outerStr);
            boolean innerNull = Strings.isNullOrEmpty(innerStr);
            if (!(outerNull && innerNull)) {
                String msg = null;
                if (outerNull) {
                    msg = String.format("母表值为空, 系统值: %s", innerData); // 母表就是Excel
                } else if (innerNull) {
                    msg = String.format("母表值: %s, 系统值为空", outerData);
                } else {
                    if (!dataTypeEnum.compare(outerStr, innerStr, extraPropertiesMap)) {
                        msg = String.format("母表值：%s, 系统值：%s", outerData, innerData);
                    }

                }
                if (!Strings.isNullOrEmpty(msg)) {
                    diffList.add(new CheckResult.Diff(column.getColumnName(), column.getTitle(), outerData, innerData, msg));
                }
            }
        });

        return diffList.size() > 0 ? CheckResult.failDetailWithData(CheckResult.FailType.NOT_MATCH, key, diffList, outerDataMap) : CheckResult.Success(key);

    }


    private Object getValForInner(Map<String, Object> innerDataElement, DataCheckColumnMapping column) {
        return innerDataElement.get(column.getColumnName());

    }

    // 主键一定是字符类型
    private String getPrimaryKeyForOuter(Map<String, Object> data, List<DataCheckColumnMapping> primaryKeyColumns) {
        return getPrimaryKey(columnMapping -> data.get(columnMapping.getTitle()), primaryKeyColumns);
    }

    private String getPrimaryKeyForInner(Map<String, Object> data, List<DataCheckColumnMapping> primaryKeyColumns) {
        return getPrimaryKey(columnMapping -> getValForInner(data, columnMapping), primaryKeyColumns);
    }

    private String getPrimaryKey(Function<DataCheckColumnMapping, Object> valFunc, List<DataCheckColumnMapping> primaryKeyColumns) {
        return primaryKeyColumns.stream().map(columnMapping ->
                DataTypeEnum.valueOf(columnMapping.getDataType()).clear(valFunc.apply(columnMapping), columnMapping.getProperties())).filter(t -> !Strings.isNullOrEmpty(t)).collect(Collectors.joining(SYMBOL));
    }


    /**
     * 初始化下载，将文件根据不同的浏览器进行编码并设置响应头
     *
     * @param request
     * @param response
     * @param fileName 下载的文件名
     * @throws UnsupportedEncodingException
     */
    public void downloadInit(HttpServletRequest request,
                             HttpServletResponse response, String fileName)
            throws UnsupportedEncodingException {
        response.setContentType("APPLICATION/OCTET-STREAM");
        if (StringUtils.isNotEmpty(fileName)) {
            String fileNameLower = fileName.toLowerCase();
            if (fileNameLower.endsWith(".apk")) {
                response.setContentType("application/vnd.android.package-archive");
            } else if (fileNameLower.endsWith(".pdf")) {
                response.setContentType("application/pdf");
            }
        }
        String filedisplay = URLEncoder.encode(fileName, "UTF-8");
        String agent = request.getHeader("USER-AGENT");
        if (agent != null && agent.indexOf("MSIE") == -1
                && !agent.contains("Trident") && !agent.contains("Edge") && !agent.contains("Mac")) {
            String enableFileName = "=?UTF-8?B?"
                    + new String(Base64.encodeBase64(fileName.getBytes("UTF-8"))) + "?=";

            response.setHeader("Content-Disposition", String.format("attachment; filename=%s;filename*=utf-8''%s"
                    , enableFileName, filedisplay));
        } else {
            response.addHeader("Content-Disposition", String.format("attachment;filename=%s;filename*=utf-8''%s", filedisplay, filedisplay));
        }
    }


}