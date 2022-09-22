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
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class Test {
    public static final String FILE = "bankCooperation.xlsx"; //要用英文名,否则可能无法读取到路径文件 。 最好放到启动jar模块的resources文件下。否则需要用getResourceAsStream方式读
    final String TEMP = "/template/" + FILE;

    // 一定要和excel里的sheet名一样！ easyexcel使用了按名称匹配sheet！
    @AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    @Getter
    public enum Type {
        CREDIT_GRANTED_BANK(1, "各银行给予的授信情况"),
        COOPERATION(3, "项目合作情况");
        private int code;
        private String msg;

    }

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
        new Test().upload("C:\\Users\\xiongwenjun\\Desktop\\bankCooperation.xlsx");
    }

    ;


    /**
     * 上传指定月份 覆盖原有数据原有数据
     */
    public void download(HttpServletResponse response) throws Exception {


        downLoadExcel(FILE, TEMP, response, excelWriter -> {


            Map<String, BigDecimal> extraMap = Maps.newHashMap();
            extraMap.put("totalLoan", BigDecimal.ZERO);
            fillSheet(resetSerialNo(getAllCreditGrantedEachBank()), Type.CREDIT_GRANTED_BANK, CreditBank.class, excelWriter);
            fillSheet(resetSerialNo(getAllprojectCooperation()), extraMap, Type.COOPERATION, ProjectCooperation.class, excelWriter);
        });

    }

    private List<CreditBank> getAllCreditGrantedEachBank() {
        return Lists.newArrayList();
    }

    private List<ProjectCooperation> getAllprojectCooperation() {
        return Lists.newArrayList();

    }

    public <T> void fillSheet(List<T> list, Type type, Class cls, ExcelWriter excelWriter) {
        fillSheet(list, null, type, cls, excelWriter);
    }


    // 支持同个excel多个sheet的写入
    public <T> void fillSheet(List<T> list, Map extraMap, Type type, Class cls, ExcelWriter excelWriter) {
        WriteSheet writeSheet = EasyExcel.writerSheet(type.getMsg()).head(cls).build();
        FillConfig fillConfig = FillConfig.builder().direction(WriteDirectionEnum.VERTICAL).forceNewRow(Boolean.TRUE).build();
        excelWriter.fill(new FillWrapper("list", list), fillConfig, writeSheet);
        if (MapUtils.isNotEmpty(extraMap)) {
            excelWriter.fill(extraMap, writeSheet);
        }
    }

    public <T extends BaseEntity> List<T> resetSerialNo(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            for (int index = 0; index < list.size(); index++) {
                list.get(index).setSerialNumber(String.valueOf(index + 1));
            }
        }
        return list;
    }

    public void downLoadExcel(String fileName, String temp, HttpServletResponse response, Consumer<ExcelWriter> executeConsumer) throws Exception {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        fileName = URLEncoder.encode(fileName, "utf-8");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);

        BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        InputStream stream = this.getClass().getResourceAsStream(temp);
        ExcelWriter excelWriter = EasyExcel.write(bos).withTemplate(stream).build();
        try {
            executeConsumer.accept(excelWriter);
        } finally {
            if (excelWriter != null) excelWriter.finish();
            response.flushBuffer();
        }

    }
}