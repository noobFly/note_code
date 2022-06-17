package com.noob.util.sql;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.util.IoUtils;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.noob.util.JacksonUtil;
import com.noob.util.security.MD5;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 从excel的列转成create table
public class ExcelTableCreator {

    // 百度翻译API
    public static String appId = "20220616001249570";
    public static String key = "CbihSFfS5zdNDpNeLWCV";
    public static String remote = "http://api.fanyi.baidu.com/api/trans/vip/translate?from=zh&to=en&q=%s&appid=%s&salt=%s&sign=%s";

    public static int sheetNum = 0; // 指定sheet
    public static int[] headRow = new int[]{4, 5}; // 指定excel里标题行的index
    public static String path = "C:\\Users\\xiongwenjun\\Desktop\\有息负债明细表模板.xls(1).xlsx"; // 模板文件

    public static Map<Integer, String> ENtransferMap = Maps.newHashMap(); // 需要转英文的中文名称

    // 默认的sql字段类型
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum SpecialType {
        DEFAULT(0, "varchar(100)"),
        RATE(4, "decimal(12,6)"),
        MONEY(3, "decimal(20,4)"), DATE(2, "date"), enumList(1, "tinyint");

        private int code;
        @Getter
        private String sqlType;
    }

    public static String convert(String en) {
        en = en.replaceAll("or |of |at |the |by |and |in | line", ""); //替换介词、 动词等 TODO 最好只保留名词
        en = en.replaceAll("  ", " ");
        en = StringUtils.trim(en);
        en = en.replaceAll("\\s", "_");
        en = en.replaceAll("single_credit_", "");
        en = en.replaceAll("__", "_").toLowerCase();
        return en;
    }


    public static void main(String[] args) throws Exception {
        File file = new File(path);
        EasyExcelListener readListener = new EasyExcelListener(headRow);
        // 默认只读第一个sheet
        EasyExcel.read(file, readListener).extraRead(CellExtraTypeEnum.MERGE).autoTrim(true).sheet(sheetNum).headRowNumber(headRow[headRow.length - 1] + 1).doRead();
        Table table = new Table();
        table.setSheetName(readListener.getSheetName());
        Map<Integer, String> headMap = readListener.getHeadMap(); // 表头原数
        table.setColumnList(headMap.entrySet().stream().map(t -> {
            Column column = new Column();
            String val = t.getValue().replace("\n", "");
            column.setType(specialType(val));
            column.setComment(t.getKey(), val);
            return column;
        }).collect(Collectors.toList()));


        List<Pre> result = getEnglish(ExcelTableCreator.ENtransferMap.values().stream().collect(Collectors.joining("\n")));

        table.getColumnList().forEach(t -> {
            Pre pre = result.stream().filter(a -> a.getSrc().equals(t.getComment())).findAny().orElse(null);
            if (pre != null && !Strings.isNullOrEmpty(pre.getDst())) {

                t.setColumnName(convert(pre.getDst()));
            }

        });
        table.setTableName(convert(result.stream().filter(a -> a.getSrc().equals(table.getSheetName())).findAny().orElse(null).getDst()));
        System.out.println(JacksonUtil.toJson(table));

        StringBuilder sb = new StringBuilder("create table ");
        sb.append(table.getTableName()).append("(").append("\n")
                .append("id bigint(20)  not null  auto_increment comment '主键' ,").append("\n");
        sb.append(table.getColumnList().stream().map(t -> {
            String column = t.getColumnName();
            return String.join(" ", column, t.getType().getSqlType(), "comment", "'" + t.getComment() + "'");
        }).collect(Collectors.joining(", \n"))).append(",\n");
        sb.append("upload_month date not null  comment '数据月份'").append(",\n")
                .append("create_time datetime null default current_timestamp comment '创建时间'").append(",\n")
                .append("update_time datetime null default current_timestamp on update current_timestamp comment '更新时间'").append(",\n")
                .append("primary key (`id`) )").append("\n").append("COMMENT='").append(table.getSheetName()).append("' AUTO_INCREMENT=1000;");
        System.out.println(sb.toString());

    }

    // 0 默认字符  1 enum 下拉  2  date 日期   3  带2位小数的钱  4  带%4位小数
    private static SpecialType specialType(String v) {
        if (v.contains("下拉选")) {
            return SpecialType.enumList;
        } else if (StringUtils.containsAny(v, "日期", "有效期")) {
            return SpecialType.DATE;
        } else if (StringUtils.containsAny(v, "（人民币）", "(原币)", "（本币）", "额度")) {
            return SpecialType.MONEY;
        } else if (v.contains("利率")) {
            return SpecialType.RATE;
        } else {
            return SpecialType.DEFAULT;
        }
    }


    @Data
    public static class Table {
        private String sheetName;
        private String tableName;
        private List<Column> columnList;

        public void setSheetName(String sheetName) {
            this.sheetName = StringUtils.trim(sheetName);
            ExcelTableCreator.ENtransferMap.put(9999, this.sheetName);
        }
    }

    @Data
    public static class Column {
        private Integer sort;
        private String comment;
        private SpecialType type;         // 0 默认字符  1 enum 下拉  2  date 日期   3  带2位小数的钱  4  带%4位小数
        private String columnName;

        public void setComment(Integer sort, String comment) {
            this.sort = sort;
            this.comment = StringUtils.trim(comment);
            try {
                ExcelTableCreator.ENtransferMap.put(this.sort, this.comment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static List<Pre> getEnglish(String chinese) throws Exception {
        int salt = RandomUtils.nextInt(100, 300);
        String sign = MD5.md5_32_1(appId + chinese + salt + key);
        String path = String.format(remote, URLEncoder.encode(chinese), appId, salt, sign);
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.connect();
        connection.getOutputStream().flush();
        InputStream stream = connection.getInputStream();
        String msg = StringEscapeUtils.unescapeJava(new String(IoUtils.toByteArray(stream)));

        System.out.println(msg);
        Result result = JacksonUtil.jsonToObject(msg, Result.class);

        return result.getTrans_result();

    }

    @Data
    public static class Result {
        private List<Pre> trans_result;
    }

    @Data
    public static class Pre {
        private String src;
        private String dst;
    }

    /**
     *
     *
     create table credit_granted_each_bank(
     id bigint(20)  not null  auto_increment comment '主键' ,
     serial_number varchar(100) comment '序号',
     corporate_name varchar(100) comment '公司名称',
     credit_institutions varchar(100) comment '授信机构',
     handling_agency varchar(100) comment '经办机构',
     credit decimal(20,4) comment '授信额度',
     credit_type_liquid_loan decimal(20,4) comment '授信种类及单项授信额度_流贷',
     credit_type_investment_debt_financing_instruments decimal(20,4) comment '授信种类及单项授信额度_债务融资工具投资',
     credit_type_corporate_overdraft decimal(20,4) comment '授信种类及单项授信额度_法人透支',
     credit_type_interbank_borrowing decimal(20,4) comment '授信种类及单项授信额度_同业拆借',
     credit_type_factoring decimal(20,4) comment '授信种类及单项授信额度_保理',
     credit_type_equity_investment decimal(20,4) comment '授信种类及单项授信额度_股权投资',
     credit_type_guarantee_guarantee decimal(20,4) comment '授信种类及单项授信额度_担保或保函',
     credit_type_other_credit_facilities decimal(20,4) comment '授信种类及单项授信额度_其他授信',
     credit_validity date comment '授信有效期',
     used_credit decimal(20,4) comment '已用授信额度',
     type_credit_used_liquid_loan decimal(20,4) comment '已用授信种类及单项授信额度_流贷',
     type_credit_used_investment_debt_financing_instruments decimal(20,4) comment '已用授信种类及单项授信额度_债务融资工具投资',
     type_credit_used_corporate_overdraft decimal(20,4) comment '已用授信种类及单项授信额度_法人透支',
     type_credit_used_interbank_borrowing decimal(20,4) comment '已用授信种类及单项授信额度_同业拆借',
     type_credit_used_factoring decimal(20,4) comment '已用授信种类及单项授信额度_保理',
     type_credit_used_equity_investment decimal(20,4) comment '已用授信种类及单项授信额度_股权投资',
     type_credit_used_guarantee_guarantee decimal(20,4) comment '已用授信种类及单项授信额度_担保或保函',
     type_credit_used_other_credit_facilities decimal(20,4) comment '已用授信种类及单项授信额度_其他授信',
     remaining_credit decimal(20,4) comment '剩余授信额度',
     remarks varchar(100) comment '备注说明',
     upload_month date not null  comment '数据月份',
     create_time datetime null default current_timestamp comment '创建时间',
     update_time datetime null default current_timestamp on update current_timestamp comment '更新时间',
     primary key (`id`) )
     COMMENT='各银行给予的授信情况' AUTO_INCREMENT=1000;
     */
}
