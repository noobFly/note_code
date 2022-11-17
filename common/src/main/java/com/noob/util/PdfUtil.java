package com.noob.util;

import com.ecc.emp.core.EMPException;
import com.google.common.base.Strings;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class PdfUtil {
    final static String FONT_PATH = "simsun.ttf";

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