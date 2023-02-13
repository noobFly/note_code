package com.noob.easyexcel.demo;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.noob.easyexcel.demo.entity.BaseEntity;
import com.noob.easyexcel.demo.entity.CreditBank;
import com.noob.easyexcel.demo.entity.ProjectCooperation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class TestEasyExcel {
    public static final String FILE = "bankCooperation.xlsx"; //要用英文名,否则可能无法读取到路径文件。最好放到启动jar模块的resources文件下，需要用Class#getResourceAsStream方式读
    final String TEMP = "/template/" + FILE;


    // 上传
    public void upload(String path) throws Exception {
        File file = new File(path);

        CustomizeEventListener creditGrantedEachBankHandler =
                new CustomizeEventListener(Type.CREDIT_GRANTED_BANK.getMsg(), "2022-04");
        CustomizeEventListener projectCooperationHandler =
                new CustomizeEventListener(Type.COOPERATION.getMsg(), "2022-04");

        ExcelReader excelReader = EasyExcel.read(new FileInputStream(file)).build();
        try {
            // 这里选择按指定sheet_index读入， 指定标题行号
            excelReader.read(
                    EasyExcel.readSheet(0).head(CreditBank.class).headRowNumber(5).registerReadListener(creditGrantedEachBankHandler).build(),
                    EasyExcel.readSheet(1).head(ProjectCooperation.class).headRowNumber(4).registerReadListener(projectCooperationHandler).build());
        } finally {
            // 这里千万别忘记关闭，读的时候会创建临时文件
            excelReader.finish();
        }

        System.out.println(creditGrantedEachBankHandler.getDataList().size());
        System.out.println(projectCooperationHandler.getDataList().size());
    }

    public static void main(String[] args) throws Exception {
        //  new TestEasyExcel().upload("C:\\Users\\xiongwenjun\\Desktop\\bankCooperation.xlsx");
        new TestEasyExcel().download();
    }


    private void download() throws FileNotFoundException {

        List<CreditBank> list = Lists.newArrayList(buildCredit("1"), buildCredit("1"), buildCredit("2"), buildCredit("2"));
        downLoadExcel(TEMP, new FileOutputStream(new File("C:\\Users\\xiongwenjun\\Desktop\\bankCooperation.xlsx")), excelWriter -> {
            Map<String, Object> extraMap = Maps.newHashMap();
            extraMap.put("totalLoan", BigDecimal.ZERO);
            extraMap.put("year", "2023");

            //需要合并的列 --- 按实际模板情况而定
            int[] mergeColumIndex = {1};
            // --- 按实际模板情况而定： 从第6行开始合并, 第一条真实数据的rowIndex
            int mergeRowIndex = 5;

            // 可以用sheetName或sheetIndex来约定写入的sheet
            WriteSheet writeSheet = EasyExcel.writerSheet(0).head(CreditBank.class).registerWriteHandler(new ExcelMergeHandler(mergeRowIndex, mergeColumIndex)).build();
            FillConfig fillConfig = FillConfig.builder().direction(WriteDirectionEnum.VERTICAL).forceNewRow(Boolean.TRUE).build();
            excelWriter.fill(new FillWrapper("list", list), fillConfig, writeSheet);
            excelWriter.fill(new FillWrapper("list2", list), fillConfig, writeSheet); // 不同区域的数据分成不同的warpper插入。
            if (MapUtils.isNotEmpty(extraMap)) {
                excelWriter.fill(extraMap, writeSheet);
            }
        });

    }


    /**
     * 下载: 按指定模板导出
     */
    public void download(HttpServletResponse response) throws Exception {

        downLoadExcel(FILE, TEMP, response, excelWriter -> {

            Map<String, Object> extraMap = Maps.newHashMap();
            extraMap.put("totalLoan", BigDecimal.ZERO);
            // 处理2个不同的sheet
            fillSheet(resetSerialNo(getAllCreditGrantedEachBank()), Type.CREDIT_GRANTED_BANK, CreditBank.class, excelWriter);
            fillSheet(Lists.newArrayList(new FillWrapper("list", (resetSerialNo(getAllProjectCooperation())))), extraMap, Type.COOPERATION, ProjectCooperation.class, excelWriter);
        });

    }

    private List<CreditBank> getAllCreditGrantedEachBank() {
        return Lists.newArrayList(); //TODO 真实数据
    }

    private List<ProjectCooperation> getAllProjectCooperation() {
        return Lists.newArrayList();//TODO 真实数据

    }

    public <T> void fillSheet(List<T> list, Type type, Class cls, ExcelWriter excelWriter) {
        fillSheet(Lists.newArrayList(new FillWrapper("list", list)), null, type, cls, excelWriter);
    }


    // 支持同个excel多个sheet的写入，按sheetName匹配； 支持写入多份数据。
    public <T> void fillSheet(List<FillWrapper> dataWrapperList, Map extraMap, Type type, Class cls, ExcelWriter excelWriter) {
        // 表头可以以类对象属性上的@ExcelProperty定义； 也可以动态处理。
        WriteSheet writeSheet = EasyExcel.writerSheet(type.getMsg()).head(cls).build();
        // 填写配置，forceNewRow true表示自动创建一行，后面的数据后移
        FillConfig fillConfig = FillConfig.builder().direction(WriteDirectionEnum.VERTICAL).forceNewRow(Boolean.TRUE).build();
        dataWrapperList.stream().forEach(t -> excelWriter.fill(t, fillConfig, writeSheet));
        if (MapUtils.isNotEmpty(extraMap)) {
            excelWriter.fill(extraMap, writeSheet); // 最后写入extraMap!
        }
    }

    public <T extends BaseEntity> List<T> resetSerialNo(List<T> list) {
        if (!CollectionUtils.isEmpty(list)) {
            for (int index = 0; index < list.size(); index++) {
                list.get(index).setSerialNumber(String.valueOf(index + 1));
            }
        }
        return list;
    }

    //按模板定义导出
    public void downLoadExcel(String fileName, String temp, HttpServletResponse response, Consumer<ExcelWriter> executeConsumer) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment; filename=" + URLEncoder.encode(fileName, "utf-8"));
        try {
            downLoadExcel(temp, response.getOutputStream(), executeConsumer);
        } finally {
            response.flushBuffer();
        }

    }

    //按模板定义导出
    public void downLoadExcel(String temp, OutputStream response, Consumer<ExcelWriter> executeConsumer) {
        BufferedOutputStream bos = new BufferedOutputStream(response);
        InputStream stream = this.getClass().getResourceAsStream(temp);
        ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(stream).build();
        try {
            executeConsumer.accept(excelWriter);
        } finally {
            if (excelWriter != null) excelWriter.finish(); // 一定要finish
        }

    }

    private CreditBank buildCredit(String serialNo) {
        CreditBank creditBank = new CreditBank();
        creditBank.setSerialNumber(serialNo);
        creditBank.setCorporateName("CorporateName");

        return creditBank;
    }

    // 一定要和excel里的sheet_table名一样！ easyexcel使用了按名称匹配sheet_table！
    @AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    @Getter
    public enum Type {
        CREDIT_GRANTED_BANK(1, "各银行给予的授信情况"),
        COOPERATION(3, "项目合作情况");
        private int code;
        private String msg;

    }
}