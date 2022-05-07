package com.noob.sign.handler;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 先word填充-> 再word转pdf -> 再pdf加签
 * 用LibreOffice将Office文档转换PDF：  https://segmentfault.com/a/1190000015129654?utm_source=channel-hottest
 */
@Slf4j
public class LibreOfficePdfConverter {
    private String linuxCommandLineTemplate = "libreoffice6.4 --invisible --headless --convert-to pdf:writer_pdf_Export --outdir %s %s";

    private String windowsCommandLineTemplate = "C:\\\\Progra~1\\\\LibreOffice\\\\program\\\\soffice.exe --invisible --headless --convert-to pdf:writer_pdf_Export --outdir %s %s";

    private static boolean windowsOs = System.getProperty("os.name").contains("Windows");
    private static Configure configure;

    static {
        ConfigureBuilder builder = Configure.newBuilder();
        builder.buildGramer("${", "}"); //模板文件的占位符${xxx}
        builder.setElMode(Configure.ELMode.SPEL_MODE);
        configure = builder.build();
    }

    // office填充数据
    public void convertDoc(Map<String, Object> params, Map<String, Map<String, String>> dicts) {
        XWPFTemplate xwpftemplate = null;
        try {
            setDefaultParam(params);
            handleDictValues(params, dicts);
            xwpftemplate = XWPFTemplate.compile(FileUtils.openInputStream(new File("doc模板文件")), configure)
                    .render(params);

            String filePath = UUID.randomUUID().toString().replace("-", "") + ".docx";
            xwpftemplate.writeToFile(filePath);

        } catch (Exception e) {
            log.error(" office填充数据", e);
        } finally {
            try {
                if (xwpftemplate != null) {
                    xwpftemplate.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 默认的占位符数值
    private void setDefaultParam(Map<String, Object> params) {
        if (params != null) {
            params.put("_now", new Date());
        }
    }

    /**
     * 字典值中文转换
     *
     * @param params
     * @param dicts
     */
    private void handleDictValues(Map<String, Object> params, Map<String, Map<String, String>> dicts) {
        if (MapUtils.isEmpty(params) || MapUtils.isEmpty(dicts)) {
            return;
        }

        Set<String> paramKeys = params.keySet();
        Set<String> dictMapKeys = dicts.keySet();

        for (String paramKey : paramKeys) {
            for (String dictMapKey : dictMapKeys) {
                if (Objects.equals(paramKey, dictMapKey)) {
                    Object paramValue = params.get(paramKey);

                    if (paramValue instanceof String) {
                        String paramValueStr = (String) paramValue;
                        Map<String, String> dict = dicts.get(dictMapKey);
                        Set<String> dictKeys = dict.keySet();

                        for (String dictKey : dictKeys) {
                            if (Objects.equals(paramValueStr, dictKey)) {
                                String dictValue = dict.get(dictKey);
                                params.put(paramKey, dictValue);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * LibreOffice转换doc为pdf文件
     * 单例，只能同步访问， 可多开容器
     */
    public synchronized String convert(String docFilePath) {
        File docFile = new File(docFilePath);
        String parent = docFile.getParent();

        String commandLine = String.format(getCommandLineTemplate(), parent, docFilePath);
        try {
            boolean success = executeCommand(commandLine);
            if (!success) {
                throw new RuntimeException("LibreOffice转换doc为pdf文件失败");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("LibreOffice转换doc为pdf文件异常", e);
        }
        return docFilePath.substring(0, docFilePath.lastIndexOf(".") + 1) + "pdf";
    }

    private boolean executeCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            String lines = getLines(process.getInputStream());
            return lines.contains("writer_pdf_Export");
        } else {
            log.error("执行脚本{}出错：{}", command, getLines(process.getErrorStream()));
            return false;
        }
    }

    private String getLines(InputStream inputStream) throws IOException {
        byte[] byteArray = IOUtils.toByteArray(inputStream);
        if (byteArray == null) {
            return "";
        }

        return new String(byteArray, windowsOs ? "GBK" : "UTF-8");
    }

    private String getCommandLineTemplate() {
        if (windowsOs) {
            return windowsCommandLineTemplate;
        }
        return linuxCommandLineTemplate;
    }
}
