package com.noob.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * word转pdf也可以通过aspose：
 * <dependency>
 * <groupId>com.aspose</groupId>
 * <artifactId>words</artifactId>
 * <version>18.6</version>
 * </dependency>
 */

public class DocToPdf {
    public static void doc2pdf(String inPath, String outPath) throws Exception {
        if (!getLicense()) { // 验证License 若不验证则转化出的pdf文档有水印
            throw new Exception("com.aspose.words lic ERROR!");
        }
        FileOutputStream os = null;
        try {
            File file = new File(outPath);
            os = new FileOutputStream(file);
            com.aspose.words.Document doc = new com.aspose.words.Document(inPath); // word文档
            // 支持RTF HTML,OpenDocument, PDF,EPUB, XPS转换
            doc.save(os, com.aspose.words.SaveFormat.PDF);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (os != null) os.close();
        }
    }

    public static boolean getLicense() throws Exception {
        boolean result;
        try {
            InputStream is = com.aspose.words.Document.class.getResourceAsStream("/com.aspose.words.lic_2999.xml");
            com.aspose.words.License aposeLic = new com.aspose.words.License();
            aposeLic.setLicense(is);
            result = true;
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return result;
    }

    public static void main(String args[]) throws Exception {
        FileInputStream fi = new FileInputStream(new File("C:\\Users\\xiongwenjun\\Desktop\\工作\\外网可用的redis集群信息.doc"));
        System.out.println(fi.available()); // )获取 文件的总大小
        doc2pdf("C:\\Users\\xiongwenjun\\Desktop\\工作\\外网可用的redis集群信息.doc", "C:\\Users\\xiongwenjun\\Desktop\\工作\\外网可用的redis集群信息.pdf");
    }
}

