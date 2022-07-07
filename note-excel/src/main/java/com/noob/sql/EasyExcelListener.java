package com.noob.sql;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EasyExcelListener<T> extends AnalysisEventListener<T> {
    public int[] headRow; // 指定excel里标题行的index
    public boolean loadSheetName = true; // 读sheet名
    @Getter
    public String sheetName;//表单名称
    @Getter
    public Map<Integer, String> headMap; // 表头title

    public List<Map<Integer, String>> headComposableList; // 用来整合多行表头

    public static List importList = new ArrayList();
    public static final ThreadLocal<String> RESP = new ThreadLocal();

    public EasyExcelListener(int[] headRow) {
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

        if (headMapComposableList.size() != 1) {
            Map<Integer, String> salve = headMapComposableList.get(1);

            List<Map.Entry<Integer, String>> list = salve.entrySet().stream().collect(Collectors.toList());
            String markValue = null;
            for (int i = 0; i < list.size(); i++) {
                Map.Entry<Integer, String> cur = list.get(i);
                if (Strings.isNullOrEmpty(cur.getValue())) {
                    markValue = null; // salve为空则清空mark
                    continue;
                }

                String mainVal = main.get(cur.getKey());

                /**
                 * 11100100
                 * 00111111
                 */
                if (Strings.isNullOrEmpty(markValue) || !Strings.isNullOrEmpty(mainVal)) { // markValue为空标识刚开始, mainVal不为空标识mark切换
                    markValue = main.get(cur.getKey());
                }
                final String markValue2 = markValue;
                main.compute(cur.getKey(), (k, v) -> markValue2.trim() + "_" + cur.getValue()); // 整合mainValue
            }

        }
        this.headMap = main;

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
