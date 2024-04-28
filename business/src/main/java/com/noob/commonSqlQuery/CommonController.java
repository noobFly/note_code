package com.noob.commonSqlQuery;

import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.google.common.collect.Lists;
import com.noob.json.JSON;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通用请求处理
 */
@RestController
@Slf4j
public class CommonController {


    @Autowired
    CommonQueryHandler commonService;

    private final int EXCEL_MAX_SIZE = 2000;
    private final int BATCH_SIZE = 2000;


    /**
     * 通用的查询功能
     *
     * @return
     */
    @PostMapping("/common/query")
    @ApiOperation(value = "通用的查询功能")
    public Object query(@Validated @RequestBody CommonQueryDTO param) {

        List<Map<String, Object>> list = commonService.query(param);
        return param.isStartPage() ? toPage(list) : list;
    }

    private Object toPage(List<Map<String, Object>> list) {
        return null; //转换分页
    }


    /**
     * 通用的查询导出功能
     *
     * @return
     */
    @PostMapping("/common/download")
    public void downloadAdm(@Validated @RequestBody CommonQueryDTO param, String fileName, HttpServletResponse response) throws Exception {
        List<Map<String, Object>> dataList = commonService.query(param);
        if (CollectionUtils.isEmpty(dataList)) {
            throw new RuntimeException("无可导出数据");
        }

        List<SysDictData> dictList = getDict().stream().sorted(Comparator.comparing(SysDictData::getDictSort)).collect(Collectors.toList());

        List<String> selectColumnList = param.getSelectColumnList();
        List<SysDictData> rowList = CollectionUtils.isEmpty(selectColumnList) ? dictList : dictList.stream().filter
                (t -> selectColumnList.stream().anyMatch(a -> a.equals(t.getDictValue()))).collect(Collectors.toList()); // 需要的字段

        List<Map<String, Object>> finalDataList = dataList.stream().map(originMap -> {
            Map<String, Object> map = new LinkedHashMap<>();
            rowList.forEach(dict -> map.put(dict.getDictLabel(), originMap.get(dict.getDictValue())));
            return map;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        ExcelUtil.getWriter().write(finalDataList).autoSizeColumnAll().flush(response.getOutputStream()).close(); // cn.hutool.poi.excel 直接写.

    }


    //TODO 第二种写法 com.alibaba.excel
    public void download2(CommonQueryDTO param) {
        param.setStartPage(true);
        long total = commonService.count(param);
        if (total == 0) {
            throw new RuntimeException("当前查询条件下无可导出数据");
        }

        // 排序标题
        List<SysDictData> dictList = getDict().stream().sorted(Comparator.comparing(SysDictData::getDictSort)).collect(Collectors.toList());

        List<String> selectColumnList = param.getSelectColumnList();
        List<SysDictData> rowList = CollectionUtils.isEmpty(selectColumnList) ? dictList : dictList.stream().filter
                (t -> selectColumnList.stream().anyMatch(a -> a.equals(t.getDictValue()))).collect(Collectors.toList()); // 需要的字段

        List<List<String>> head = Lists.newArrayList();
        rowList.stream().map(SysDictData::getDictLabel).forEach(t -> head.add(Lists.newArrayList(t))); // 这种格式是为了多行标题head
        Instant now = Instant.now();
        ExcelWriter excelWriter = null;
        try {
            long sheetCount = total / EXCEL_MAX_SIZE + (total % EXCEL_MAX_SIZE == 0 ? 0 : 1); // 分多少个sheet
            long pageNumMax = total / BATCH_SIZE + (total % BATCH_SIZE == 0 ? 0 : 1); //查询sql分页

            String localFileName = null;//TODO 本地文件的绝对地址
            excelWriter = EasyExcel.write(localFileName).registerWriteHandler(new SimpleColumnWidthStyleStrategy(25)).build(); // LongestMatchColumnWidthStyleStrategy

            int pageNum = 1;
            // 按固定条数分sheet保存。 但当前测试1个sheet保存10W+条数据是可以的。
            for (int sheetNum = 0; sheetNum < sheetCount; sheetNum++) {
                WriteSheet sheet = EasyExcel.writerSheet(sheetNum).head(head).needHead(true).build(); //无模板自定义标题
                do {
                    final int pageNum2 = pageNum;
                    List<List<Object>> finalDataList = commonService.query(param, () -> startPage(pageNum2, BATCH_SIZE)).stream().map(originMap -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        rowList.forEach(dict -> map.put(dict.getDictLabel(), originMap.get(dict.getDictValue())));
                        return map.values().stream().collect(Collectors.toList());
                    }).collect(Collectors.toList());
                    pageNum++;
                    excelWriter.write(finalDataList, sheet);
                }
                while (pageNum <= pageNumMax && (pageNum * BATCH_SIZE / EXCEL_MAX_SIZE < sheetNum + 1 || pageNum * BATCH_SIZE % EXCEL_MAX_SIZE == 0));
            }

        } catch (Exception e) {
            log.info("导出数据异常 {}", JSON.toJSONString(param), e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
            log.info("导出数据 耗时:{}ms", Duration.between(now, Instant.now()).toMillis());
        }

    }

    // 用ThreadLocal方式开启当前线程内对下一个sql执行的分页处理. 处理完成后需要重置分页状态
    private void startPage(int pageNum, int pageSize) {
        // TODO
    }

    private List<SysDictData> getDict() {
        // 从配置里拿到需要的表字段
        return null; // TODO
    }


}
