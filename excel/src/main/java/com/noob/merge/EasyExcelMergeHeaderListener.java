package com.noob.merge;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.noob.json.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// 整合多表头的列标题
@Slf4j
public class EasyExcelMergeHeaderListener<T> extends AnalysisEventListener<T> {
    public int[] headRow; // 指定excel里标题行的index
    public boolean loadSheetName = true; // 读sheet名
    @Getter
    public String sheetName;//表单名称
    @Getter
    public Map<Integer, String> headMap; // 表头title

    public List<Map<Integer, String>> headComposableList; // 用来整合多行表头, 原始的head行数据

    public static List importList = new ArrayList();
    public static final ThreadLocal<String> RESP = new ThreadLocal();

    public EasyExcelMergeHeaderListener(int[] headRow) {
        this.headRow = headRow;
        headComposableList = Lists.newArrayListWithCapacity(headRow.length);
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        log.info("解析到的一条数据: excelRow = {}", data);
        sheetName = context.readSheetHolder().getSheetName();
        importList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 解析完所有excel行, 保存到数据库或进行业务处理
        log.info("解析的所有数据 list = {}", importList);
        RESP.set("String");
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (loadSheetName) {
            sheetName = context.readSheetHolder().getSheetName();
            loadSheetName = false;
        }
        if (headRow.length == 1) {
            if (context.readRowHolder().getRowIndex().equals(headRow[0])) {
                this.headMap = filter(headMap);
                log.info("表头数据 excelHead= {}", headMap);

            }
        } else {
            int rowIndex = context.readRowHolder().getRowIndex();
            if (in(rowIndex)) {
                headComposableList.add(filter(headMap));
            }
            if (rowIndex == headRow[headRow.length - 1]) {
                mergeTitle(headComposableList);
                log.info("表头数据 excelHead= {}", headMap);
            }

        }

    }


    private void mergeTitle(List<Map<Integer, String>> headMapComposableList) {
        Map<Integer, String> main = headMapComposableList.get(0);
        int size = headMapComposableList.size();
        if (size > 1) {
            for (int m = 1; m < headMapComposableList.size(); m++) {
                Map<Integer, String> salve = headMapComposableList.get(m);

                List<Map.Entry<Integer, String>> list = salve.entrySet().stream().collect(Collectors.toList());
                String markValue = null; // 留存上一个需要沿用的主title
                for (int i = 0; i < list.size(); i++) {
                    Map.Entry<Integer, String> cur = list.get(i);
                    String cellVal = cur.getValue();
                    int cellIndex = cur.getKey();
                    boolean isLast = m == size - 1;
                    boolean isEmpty = Strings.isNullOrEmpty(cur.getValue());

                    if (isEmpty) {
                        if (isLast) {
                            markValue = null; // 当最后1行时， 列值为空则清空mark
                            continue;
                        } else {
                            markValue = main.get(cellIndex - 1); // 当非最后行时，则应该取当前合并进程中已经合并过的上一个值
                        }
                    }

                    String mainVal = main.get(cellIndex);
                    // markValue为空标识刚开始, mainVal不为空标识mark切换
                    if (Strings.isNullOrEmpty(markValue) || !Strings.isNullOrEmpty(mainVal)) {
                        markValue = mainVal;
                    }
                    if (!Strings.isNullOrEmpty(markValue)) {
                        final String markValue2 = Strings.nullToEmpty(markValue);
                        main.compute(cur.getKey(), (k, v) -> Strings.isNullOrEmpty(cellVal) ? markValue2.trim() : markValue2.trim() + "_" + cur.getValue());
                    }

                }
            }

        }
        this.headMap = main;
        System.out.println(JSON.toJSONString(headMap));

    }


    // 清除开始为空的字段
    private Map<Integer, String> filter(Map<Integer, String> map) {
        int i = 0;
        while (true) {
            if (Strings.isNullOrEmpty(map.get(i))) {
                map.remove(i);
            } else {
                break;
            }
            i++;
        }
        return map;
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        log.info("读取到了一条额外信息:{}", extra);
    }

    boolean in(int index) {
        for (int i : headRow) {
            if (index == i) {
                return true;
            }
        }
        return false;
    }
}
