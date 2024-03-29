package com.noob.dataCheck;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class CheckResult {
    // 数据比对日期
    private String date;
    //比对的sheet名
    private String sheetName;
    // 数据表数据总量
    private int dbCount;
    // 上传总量
    private int uploadCount;
    // 比对成功总量
    private int successCount;
    // 外部无效数据总量
    private int outerErrorCount;
    // 数据表无法匹配主键数据总量
    private int outerOverCount;
    // Excel无法匹配主键数据总量
    private int innerOverCount;
    // 匹配失败数据总量
    private int matchFaiLCount;
    private List<CheckDetail> failList;

    /**
     * 只是为了做数据传输载体
     */
    private DataCheckTableMapping tableMapping;
    private List<String> headList;


    public String getDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append(
                String.format("----【%s】核对统计 ---- \n Excel上传数据: %s, 其中无效：%s; 本地表数据：%s, 成功匹配：%s, excel溢出：%s, 本地表溢出:%s, 匹配失败：%s \n",
                        this.getSheetName(), this.getUploadCount(), this.getOuterErrorCount(), this.getDbCount(), this.getSuccessCount(), this.getOuterOverCount(),
                        this.getInnerOverCount(), this.getMatchFaiLCount()));
        sb.append("【excel溢出】: \n").append(failList.stream().filter(t -> t.getFailType() == FailType.OUT_OVER).map(CheckDetail::getKey).collect(Collectors.joining("\n")));
        sb.append("\n【本地表溢出】: \n").append(failList.stream().filter(t -> t.getFailType() == FailType.DB_OVER).map(CheckDetail::getKey).collect(Collectors.joining("\n")));

        return sb.toString();
    }


    @Data
    public static class CheckDetail {
        /**
         * 结果
         */
        private boolean success;
        /**
         * 描述
         */
        private String msg;
        /**
         * 数据主键
         */
        private String key;

        /**
         * 1 数据表无该主键 (外多内少) 2 Excel无该主键(外少内多) 3 数据不一致
         */
        private int failType;
        private List<Diff> diffList;

        // 可能是Excel数据 也可能是db多的数据
        private Map<String, Object> originData;


    }

    @Data
    @AllArgsConstructor
    public static class Diff {
        /**
         * 字段
         */
        private String column;
        // 对应到的sheet的列描述
        private String title;
        /**
         * excel值
         */
        private Object excelVal;
        /**
         * 数据库值
         */
        private Object dbVal;
        // 错误报告
        private String reportMsg;

    }

    public interface FailType {
        int OUT_OVER = 1;
        int DB_OVER = 2;
        int NOT_MATCH = 3;
        int OUT_UNVALID = 4;
    }

    public static CheckResult.CheckDetail failDetailWithData(int failType, String key, List<Diff> diffList, Map<String, Object> data) {
        CheckDetail detail = fail(failType, key, null);
        detail.setDiffList(diffList);
        detail.setOriginData(data);
        return detail;
    }

    public static CheckDetail fail(int failType, String key, String msg) {
        CheckDetail detail = new CheckDetail();
        detail.setSuccess(false);
        detail.setFailType(failType);
        detail.setMsg(msg);
        detail.setKey(key);
        return detail;
    }

    public static CheckDetail fail(int failType, String key, String msg, Map<String, Object> data) {
        CheckDetail detail = fail(failType, key, null);
        detail.setOriginData(data);
        return detail;
    }

    public static CheckDetail Success(String key) {
        CheckResult.CheckDetail detail = new CheckResult.CheckDetail();
        detail.setSuccess(true);
        detail.setKey(key);
        return detail;
    }

}
