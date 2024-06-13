package com.noob.util;

import com.ecc.emp.core.EMPException;
import com.google.common.base.Strings;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.poifs.storage.HeaderBlockConstants;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LocaleUtil;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.apache.poi.poifs.common.POIFSConstants.OOXML_FILE_HEADER;
import static org.apache.poi.poifs.common.POIFSConstants.RAW_XML_FILE_HEADER;

public class PdfUtil {
    final static String FONT_PATH = "simsun.ttf";


    // 合并成1份pdf
    public static byte[] mergeToPdf(String title, String author, String theme, List<byte[]> bytesList) {
        if (CollectionUtils.isEmpty(bytesList) || bytesList.stream().allMatch(t -> t == null || t.length == 0)) {
            return null;
        }
        byte[] data = null;
        ByteArrayOutputStream os = null;
        Document document = null;
        List<byte[]> pdfBytes = new ArrayList<>();

        boolean preIsImage = false;
        try {
            for (byte[] bytes : bytesList) {
                FileMagic fileMagic = FileMagic.valueOf(bytes);
                if (fileMagic == FileMagic.PNG || fileMagic == FileMagic.JPEG) {
                    if (preIsImage == false) { // 图片要改成pdf
                        os = new ByteArrayOutputStream();
                        // 创建一个 document 流
                        document = new Document(PageSize.A4);
                        PdfWriter.getInstance(document, os);
                        //打开文档
                        document.open();
                        // 添加PDF文档的某些信息，比如作者，主题等等.必须 open 以后才起作用
                        document.addTitle(title);
                        document.addAuthor(author);
                        document.addSubject(theme);
                        document.addCreator(author);
                    }
                    //获取图片的宽高
                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(bytes);
                    // 也可设置页面尺寸
                    float imageHeight = image.getScaledHeight();
                    float imageWidth = image.getScaledWidth();
                    //统一百分比压缩
                    Integer percent = Math.round(350 / imageWidth * 100);
                    image.scalePercent(percent);
                    if (imageHeight < imageWidth / 2) {
                        image.setRotationDegrees(-90);
                    }
                    //image.scaleToFit(PageSize.A4); // 图片大小缩小在 A4 尺寸以内自适应
                    // 图片大小自适应处理
                    image.scaleToFit(PageSize.EXECUTIVE);
                    //图片居中
                    image.setAlignment(Image.ALIGN_CENTER); // 对齐方式居中
                    image.setCompressionLevel(0); // 压缩 0-9
                    //image.scalePercent(40); // 百分比缩小图片
                    //新建一页添加图片
                    document.newPage();
                    document.add(image);
                    preIsImage = true;
                } else {
                    if (preIsImage) {
                        document.close();
                        pdfBytes.add(os.toByteArray());
                        os.close();
                    }
                    if (FileMagic.valueOf(bytes) == FileMagic.PDF) {
                        pdfBytes.add(bytes);
                    }
                    preIsImage = false;
                }
            }

            if (document != null && document.isOpen()) {
                document.close();
                pdfBytes.add(os.toByteArray());
                os.close();
            }
            if (pdfBytes.size() > 0) {
                os = new ByteArrayOutputStream();
                document = new Document(PageSize.A4);
                PdfCopy copy = new PdfCopy(document, os);
                document.open();
                for (byte[] pdfByte : pdfBytes) {
                    PdfReader reader = new PdfReader(pdfByte);
                    int n = reader.getNumberOfPages();
                    for (int j = 1; j <= n; j++) {
                        document.newPage();
                        PdfImportedPage page = copy.getImportedPage(reader, j);
                        copy.addPage(page);
                    }
                }
                document.close();
                data = os.toByteArray();
                os.close();
            }

        } catch (Exception e) {
            throw new RuntimeException("PdfUtil工具合并文件失败!", e);
        } finally {
            if (document != null) {
                document.close();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return data;
        }
    }

    public enum FileMagic {
        /**
         * OLE2 / BIFF8+ stream used for Office 97 and higher documents
         */
        OLE2(HeaderBlockConstants._signature),
        /**
         * OOXML / ZIP stream
         */
        OOXML(OOXML_FILE_HEADER),
        /**
         * XML file
         */
        XML(RAW_XML_FILE_HEADER),
        /**
         * BIFF2 raw stream - for Excel 2
         */
        BIFF2(new byte[]{
                0x09, 0x00, // sid=0x0009
                0x04, 0x00, // size=0x0004
                0x00, 0x00, // unused
                '?', 0x00  // '?' = multiple values
        }),
        /**
         * BIFF3 raw stream - for Excel 3
         */
        BIFF3(new byte[]{
                0x09, 0x02, // sid=0x0209
                0x06, 0x00, // size=0x0006
                0x00, 0x00, // unused
                '?', 0x00  // '?' = multiple values
        }),
        /**
         * BIFF4 raw stream - for Excel 4
         */
        BIFF4(new byte[]{
                0x09, 0x04, // sid=0x0409
                0x06, 0x00, // size=0x0006
                0x00, 0x00, // unused
                '?', 0x00  // '? = multiple values
        }, new byte[]{
                0x09, 0x04, // sid=0x0409
                0x06, 0x00, // size=0x0006
                0x00, 0x00, // unused
                0x00, 0x01
        }),
        /**
         * Old MS Write raw stream
         */
        MSWRITE(
                new byte[]{0x31, (byte) 0xbe, 0x00, 0x00},
                new byte[]{0x32, (byte) 0xbe, 0x00, 0x00}),
        /**
         * RTF document
         */
        RTF("{\\rtf"),
        /**
         * PDF document
         */
        PDF("%PDF"),
        /**
         * Some different HTML documents
         */
        HTML("<!DOCTYP",
                "<html", "\n\r<html", "\r\n<html", "\r<html", "\n<html",
                "<HTML", "\r\n<HTML", "\n\r<HTML", "\r<HTML", "\n<HTML"),
        WORD2(new byte[]{(byte) 0xdb, (byte) 0xa5, 0x2d, 0x00}),
        /**
         * JPEG image
         */
        JPEG(
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB},
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, '?', '?', 'J', 'F', 'I', 'F', 0x00, 0x01},
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xEE},
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE1, '?', '?', 'E', 'x', 'i', 'f', 0x00, 0x00}),
        /**
         * GIF image
         */
        GIF("GIF87a", "GIF89a"),
        /**
         * PNG Image
         */
        PNG(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}),
        /**
         * TIFF Image
         */
        TIFF("II*\u0000", "MM\u0000*"),
        /**
         * WMF image with a placeable header
         */
        WMF(new byte[]{(byte) 0xD7, (byte) 0xCD, (byte) 0xC6, (byte) 0x9A}),
        /**
         * EMF image
         */
        EMF(new byte[]{
                1, 0, 0, 0,
                '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
                '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?', '?',
                ' ', 'E', 'M', 'F'
        }),
        /**
         * BMP image
         */
        BMP(new byte[]{'B', 'M'}),
        // keep UNKNOWN always as last enum!
        /**
         * UNKNOWN magic
         */
        UNKNOWN(new byte[0]);


        final byte[][] magic;

        FileMagic(long magic) {
            this.magic = new byte[1][8];
            LittleEndian.putLong(this.magic[0], 0, magic);
        }

        FileMagic(byte[]... magic) {
            this.magic = magic;
        }

        FileMagic(String... magic) {
            this.magic = new byte[magic.length][];
            int i = 0;
            for (String s : magic) {
                this.magic[i++] = s.getBytes(LocaleUtil.CHARSET_1252);
            }
        }

        public static FileMagic valueOf(byte[] magic) {
            for (FileMagic fm : values()) {
                for (byte[] ma : fm.magic) {
                    // don't try to match if the given byte-array is too short
                    // for this pattern anyway
                    if (magic.length < ma.length) {
                        continue;
                    }

                    if (findMagic(ma, magic)) {
                        return fm;
                    }
                }
            }
            return UNKNOWN;
        }

        private static boolean findMagic(byte[] expected, byte[] actual) {
            int i = 0;
            for (byte expectedByte : expected) {
                if (actual[i++] != expectedByte && expectedByte != '?') {
                    return false;
                }
            }
            return true;
        }


    }


    /**
     * 对pdf中指定地方进行处理：在原来的值得区域加一层白色涂层，并写入新的值，如果预留宽度不够，会出现字体重叠
     *
     * @param map     <String,String> map key 为pdf中要替换的地方的值,必须保证唯一，value 为pdf中将key
     *                替换后的值
     * @param srcPdf  srcPdf pdf 源文件位置
     * @param destPdf destPdf pdf 存放位置
     * @throws IOException
     * @throws EMPException
     */
    public static void treatmentPdf(Map<String, String> map, String srcPdf, String destPdf) throws EMPException {
        try {
            Map postions = null;
            // 找到所有位置
            postions = getPositionByKeyWord(map, srcPdf);
            // 将需要替换的文字，根据坐标替换
            replaceTextByPositions(srcPdf, destPdf, postions, map);
        } catch (Exception e) {
            throw new EMPException(e);
        }
    }

    private static void replaceTextByPositions(String tempPdf, String destPdf, Map<String, Position> positions,
                                               Map<String, String> map) throws EMPException {
        PdfReader reader = null;
        PdfStamper stamper = null;

        Entry<String, Position> postionEntry = null;
        String key = null;
        Position p = null;
        String text = null;
        try {
            reader = new PdfReader(tempPdf);
            stamper = new PdfStamper(reader, new FileOutputStream(destPdf));
            Iterator<Entry<String, Position>> postionEntrys = positions.entrySet().iterator();
            while (postionEntrys.hasNext()) {
                postionEntry = postionEntrys.next();
                key = postionEntry.getKey();
                p = postionEntry.getValue();
                Object obj = map.get(key);
                if (obj == null) {
                    text = "";
                } else {
                    text = String.valueOf(obj);
                }
                PdfContentByte canvas = stamper.getOverContent(p.pageNum);
                canvas.saveState();
                canvas.setColorFill(BaseColor.WHITE);
                // h稍微大一些，以保证== 能全部被挡住
                canvas.rectangle(p.x, p.y - 1.5, p.w, 12);
                canvas.fill();

                canvas.rectangle(p.x, p.y, p.w, p.h);
                canvas.fill();
                canvas.restoreState();
                // 开始写入文本
                canvas.beginText();
                // 中文字体需要iTextAsian.jar,目前用的是字体文件，宋体(simsum.ttf),小五
                BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                Font font = new Font(bf, 9f);
                canvas.setFontAndSize(font.getBaseFont(), 9f);
                canvas.setTextMatrix((float) p.x, (float) p.y);

                canvas.showText(text);
                canvas.endText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new EMPException(e);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new EMPException(e);
                }
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void hiderAreaByPositions(String srcPdf, String tempPdf, Map<String, Position> map)
            throws EMPException {
        PdfReader reader = null;
        PdfStamper stamper = null;
        try {
            reader = new PdfReader(srcPdf);
            stamper = new PdfStamper(reader, new FileOutputStream(tempPdf));
            PdfContentByte canvas = null;

            Collection<Position> positions = map.values();

            for (Position p : positions) {
                canvas = stamper.getOverContent(p.pageNum);
                canvas.saveState();
                canvas.setColorFill(BaseColor.WHITE);
                // h稍微大一些，以保证{} 能全部被挡住
                canvas.rectangle(p.x, p.y - 3, p.w, 12);
                canvas.fill();
                canvas.restoreState();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new EMPException(e);
        } finally {
            if (stamper != null) {
                try {
                    stamper.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new EMPException(e);
                }
            }
            if (reader != null) {
                reader.close();
            }

        }

    }

    public static Map<String, Position> getPositionByKeyWord(Map<String, String> map, String srcPdf) throws IOException {
        Map<String, Position> positions = new HashMap<String, Position>();
        PdfReader pdfReader = null;
        try {

            pdfReader = new PdfReader(srcPdf);
            int pageNum = pdfReader.getNumberOfPages();
            PdfReaderContentParser pdfReaderContentParser = new PdfReaderContentParser(pdfReader);
            RenderListenerImpl render = null;
            Iterator<String> keyWords = map.keySet().iterator();
            while (keyWords.hasNext()) {
                String keyWord = keyWords.next();
                // 每一页
                for (int i = 1; i <= pageNum; i++) {
                    render = new RenderListenerImpl(keyWord, i);
                    pdfReaderContentParser.processContent(i, render);

                    if (render.isMatching) {
                        positions.put(keyWord, render.position2);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }
        }
        return positions;
    }

    private static class RenderListenerImpl implements RenderListener {
        String keyWord = "";
        Position position;
        Position position2;
        int pageNum;
        boolean isMatching = false;
        int index;

        RenderListenerImpl(String keyWord, int pageNum) {
            this.keyWord = keyWord;
            this.pageNum = pageNum;
            this.position = new Position();
            position2 = null;
        }

        @Override
        public void renderText(TextRenderInfo textRenderInfo) {
            String text = textRenderInfo.getText();
            char ch = keyWord.charAt(index);
            char textChar = text.charAt(0);
            if (ch == textChar) {
                if (index == 0) {
                    Rectangle2D.Float boundingRectange = textRenderInfo.getBaseline().getBoundingRectange();
                    position.pageNum = pageNum;
                    position.x = (float) boundingRectange.getX();
                    position.y = (float) boundingRectange.getY();
                    position.w = (float) boundingRectange.getWidth();
                    position.h = (float) boundingRectange.getHeight();
                }

                index++;
                if (text.length() > 1) {
                    for (int i = 1; i < text.length() && index < keyWord.length(); i++) {
                        if (text.charAt(i) == keyWord.charAt(index)) {
                            index++;
                        } else {
                            position = new Position();
                            index = 0;
                            break;
                        }
                    }
                }
                if (index == keyWord.length()) {
                    float x = (float) textRenderInfo.getBaseline().getBoundingRectange().getX();
                    //	position.w =(position.w+1.2f)* (keyWord.length() + 1);
                    position.w = x - position.x + position.w;

                    this.position2 = position;
                    position = new Position();
                    index = 0;
                    isMatching = true;
                }
            } else {
                position = new Position();
                index = 0;
            }
        }

        @Override
        public void beginTextBlock() {

        }

        @Override
        public void endTextBlock() {

        }

        @Override
        public void renderImage(ImageRenderInfo renderInfo) {

        }

    }

    private static class Position {
        float x;
        float y;
        float w;
        float h;
        int pageNum;

        @Override
        public String toString() {
            return "Position [x=" + x + ", y=" + y + ", w=" + w + ", h=" + h + ", pageNum=" + pageNum + "]";
        }

    }


    /**
     * 读取一个pdf 文件，判断关键字所在页数,如果未找到或者 参数不正确，返回-1
     *
     * @param srcFile    源文件
     * @param key        关键字
     * @param returnLast 是否直接返回最后一页的页码
     */
    public static int getKeyAtPageNum(String srcFile, String key, boolean returnLast) throws IOException {
        int pageNum = -1;
        PdfReader pdfReader = null;
        try {
            if (Strings.isNullOrEmpty(srcFile)) {
                return pageNum;
            }
            if (returnLast) {
                pdfReader = new PdfReader(srcFile);
                pageNum = pdfReader.getNumberOfPages();
            } else {
                if (Strings.isNullOrEmpty(key)) {
                    return pageNum;
                }
                Map<String, String> map = new HashMap<>();
                map.put(key, "");
                Map<String, Position> resultMap = PdfUtil.getPositionByKeyWord(map, srcFile);
                if (resultMap != null) {
                    Position position = resultMap.get(key);
                    if (position != null) {
                        pageNum = position.pageNum;
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }
        }
        return pageNum;
    }


}