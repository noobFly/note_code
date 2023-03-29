package com.noob.merge;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.RowUtil;
import cn.hutool.poi.excel.reader.AbstractSheetReader;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.noob.json.JSON;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 扩写cn.hutool.poi.excel.reader.MapSheetReader 支持多行作为表头head
 */
public class HutoolMapSheetMergeHeaderReader extends AbstractSheetReader<List<Map<String, Object>>> {
    private final List<Integer> headerRowIndexs;
    @Getter
    private List<String> headerList;

    /**
     * 构造
     *
     * @param headerRowIndex 标题所在行组合
     * @param startRowIndex  起始行（包含，从0开始计数）
     * @param endRowIndex    结束行（包含，从0开始计数）
     */
    public HutoolMapSheetMergeHeaderReader(List<String> headerRowIndex, int startRowIndex, int endRowIndex) {
        super(startRowIndex, endRowIndex);
        this.headerRowIndexs = headerRowIndex.stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> read(Sheet sheet) {
        if (sheet == null) {
            throw new RuntimeException("sheet not exists! check sheet name is necessary");
        }
        // 边界判断
        final int firstRowNum = sheet.getFirstRowNum();
        final int lastRowNum = sheet.getLastRowNum();
        if (lastRowNum < 0) {
            return ListUtil.empty();
        }

        if (headerRowIndexs.stream().anyMatch(t -> t < firstRowNum)) {
            throw new IndexOutOfBoundsException(StrUtil.format("Header row index {} is lower than first row index {}.", JSON.toJSONString(headerRowIndexs), firstRowNum));
        } else if (headerRowIndexs.stream().anyMatch(t -> t > lastRowNum)) {
            throw new IndexOutOfBoundsException(StrUtil.format("Header row index {} is greater than last row index {}.", JSON.toJSONString(headerRowIndexs), firstRowNum));
        }
        final int startRowIndex = Math.max(this.startRowIndex, firstRowNum);// 读取起始行（包含）
        final int endRowIndex = Math.min(this.endRowIndex, lastRowNum);// 读取结束行（包含）

        // 读取header
        headerList = aliasHeader(readRow(sheet, headerRowIndexs));

        final List<Map<String, Object>> result = new ArrayList<>(endRowIndex - startRowIndex + 1);
        List<Object> rowList;
        for (int i = startRowIndex; i <= endRowIndex; i++) {
            // 跳过标题行
            final int tmp = i;
            if (headerRowIndexs.stream().noneMatch(t -> t == tmp)) {
                rowList = readRow(sheet, i);
                if (CollUtil.isNotEmpty(rowList) || false == ignoreEmptyRow) {
                    result.add(IterUtil.toMap(headerList, rowList, true));
                }
            }
        }
        return result;
    }

    protected List<Object> readRow(Sheet sheet, List<Integer> headerRowIndexList) {
        List<List<Object>> headRowList =
                headerRowIndexList.stream().map(rowIndex -> RowUtil.readRow(sheet.getRow(rowIndex), this.cellEditor)).collect(Collectors.toList()); //按顺序排列的名称
        return headRowList.size() == 1 ? clear(headRowList.get(0)) : merge(headRowList);

    }

    private List<Object> merge(List<List<Object>> headRowList) {
        int size = headRowList.stream().map(List::size).max(Comparator.comparing(Integer::intValue)).get();
        List<Object> result = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            String val = "";
            List<String> checkList = Lists.newArrayList(); // 缓存已经比对过的值。 要求不能有重复的
            for (List<Object> title : headRowList) {
                if (title.size() < i + 1) {
                    continue;
                }

                String tmp = title.get(i) == null ? StringUtils.EMPTY : title.get(i).toString();

                if (Strings.isNullOrEmpty(val)) {
                    val = tmp;
                } else if (checkList.stream().noneMatch(t -> Strings.nullToEmpty(tmp).equals(t))) {
                    val = val + "_" + tmp; // 不重复才拼接
                }
                checkList.add(Strings.nullToEmpty(tmp));


            }
            result.add(val);

        }
        System.out.println(JSON.toJSONString(result));
        return clear(result);

    }

    private List<Object> clear(List<Object> result) {
        if (CollectionUtils.isEmpty(result)) {
            return null;
        } else {
            // n 回车(u000a) t 水平制表符(u0009) s 空格(u0008) r 换行(u000d)*/
            return result.stream().map(t -> t == null ? StringUtils.EMPTY : String.valueOf(t).replaceAll("\\s*|t|r|n", "")).collect(Collectors.toList());
        }
    }


/**
 * "序号",
 * "统计对象",
 * "认缴规模（亿元）_总额",
 * "认缴规模（亿元）_其中：粤财系",
 * "认缴规模（亿元）_其中：非粤财系_总额",
 * "认缴规模（亿元）_其中：非粤财系_其中：受托管理的财政资金",
 * "认缴规模（亿元）_其中：非粤财系_其中：国有",
 * "认缴规模（亿元）_其中：非粤财系_其中：民营",
 * "实缴规模（亿元）_总额","实缴规模（亿元）_其中：粤财系",
 * "实缴规模（亿元）_其中：非粤财系_总额","实缴规模（亿元）_其中：非粤财系_其中：受托管理的财政资金",
 * "实缴规模（亿元）_其中：非粤财系_其中：国有","实缴规模（亿元）_其中：非粤财系_其中：民营"
 */
}
