package com.noob.util;

import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.poi.ss.usermodel.CellType.BLANK;

/**
 * sheet从源excle(Workbook)赋值到新的excel中
 */
public final class CopySheetUtil {

    private CopySheetUtil() {
    }

    /**
     * 将来源文件的所有sheet页复制到目标文件中
     *
     * @param srcWb     被合并的文件
     * @param toWriteWb 合并后的文件
     * @throws Exception
     */
    public static void copySheets(SXSSFWorkbook srcWb, SXSSFWorkbook toWriteWb) throws Exception {
        int numberOfSheets = toWriteWb.getNumberOfSheets();

        List<String> sheetNames = Lists.newArrayList();
        for (int i = 0; i < numberOfSheets; i++) {
            //初始化被合并文件sheet names
            sheetNames.add(toWriteWb.getSheetName(i));
        }
        //被合并文件

        int numberOfToBeMergeSheets = srcWb.getNumberOfSheets();
        for (int i = 0; i < numberOfToBeMergeSheets; i++) {
            SXSSFSheet sheet = srcWb.getSheetAt(i);
            String sheetName = sheet.getSheetName();
            sheetName = sheetNameCanUse(sheetName, sheetNames);
            SXSSFSheet newSheet = toWriteWb.createSheet(sheetName);
            copySheetsData(newSheet, sheet);
            if (SheetVisibility.HIDDEN == srcWb.getSheetVisibility(i)) {
                //sheet 隐藏
                toWriteWb.setSheetHidden(toWriteWb.getSheetIndex(sheetName), true);
            }
        }
    }

    /**
     * 将来源文件的指定数量sheet页复制到目标文件中
     *
     * @param srcWb
     * @param toWriteWb
     * @param sheetNameArr   sheet名称
     * @throws Exception
     */
    public static void copySheets(SXSSFWorkbook srcWb, SXSSFWorkbook toWriteWb, String... sheetNameArr) throws Exception {
        //初始化被合并文件sheet names
        List<String> sheetNames = Arrays.asList(sheetNameArr);

        //被合并文件
        int numberOfToBeMergeSheets = srcWb.getNumberOfSheets();
        for (int i = 0; i < numberOfToBeMergeSheets; i++) {
            SXSSFSheet sheet = srcWb.getSheetAt(i);
            String sheetName = sheetNames.get(i);
            SXSSFSheet newSheet = toWriteWb.createSheet(sheetName);
            copySheetsData(newSheet, sheet);
            if (SheetVisibility.HIDDEN == srcWb.getSheetVisibility(i)) {
                //sheet 隐藏
                toWriteWb.setSheetHidden(toWriteWb.getSheetIndex(sheetName), true);
            }
        }
    }

    public static void copySheetsData(SXSSFSheet newSheet, SXSSFSheet sheet) {
        copySheetsStyle(newSheet, sheet);
        copySheetsData(newSheet, sheet, true);
    }

    //sheet 样式拷贝
    private static void copySheetsStyle(SXSSFSheet newSheet, SXSSFSheet sheet) {
        //公式计算
        newSheet.setForceFormulaRecalculation(true);
        //sheet tab颜色
        XSSFColor tabColor = sheet.getTabColor();
        if (Objects.nonNull(tabColor)) {
            newSheet.setTabColor(sheet.getTabColor());
        }
        //条件表达式
        SheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
        SheetConditionalFormatting toSheetConditionFormat = newSheet.getSheetConditionalFormatting();
        for (int i = 0; i < scf.getNumConditionalFormattings(); i++) {
            toSheetConditionFormat.addConditionalFormatting(scf.getConditionalFormattingAt(i));
        }
        //冻结行列信息
        PaneInformation paneInformation = sheet.getPaneInformation();
        if (Objects.nonNull(paneInformation)) {
            newSheet.createFreezePane(paneInformation.getHorizontalSplitPosition(), paneInformation.getVerticalSplitPosition()
                    , paneInformation.getHorizontalSplitTopRow(), paneInformation.getVerticalSplitLeftColumn());
        }
    }

    private static void copySheetsData(SXSSFSheet newSheet, SXSSFSheet sheet,
                                       boolean copyStyle) {
        int maxColumnNum = 0;
        Map<Integer, CellStyle> styleMap = (copyStyle) ? new HashMap<Integer, CellStyle>()
                : null;
        Set<CellRangeAddressWrapper> mergedRegions = new TreeSet<CellRangeAddressWrapper>();
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            SXSSFRow srcRow = sheet.getRow(i);
            SXSSFRow destRow = newSheet.createRow(i);

            if (srcRow != null) {
                //行隐藏
                destRow.setZeroHeight(srcRow.getZeroHeight());

                copyRow(sheet, newSheet, srcRow, destRow,
                        styleMap, mergedRegions);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {    //设置列宽
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
            //列隐藏
            newSheet.setColumnHidden(i, sheet.isColumnHidden(i));
        }
    }

    /**
     * 复制并合并单元格
     */
    private static void copyRow(SXSSFSheet srcSheet, SXSSFSheet destSheet,
                                SXSSFRow srcRow, SXSSFRow destRow,
                                Map<Integer, CellStyle> styleMap,
                                Set<CellRangeAddressWrapper> mergedRegions) {

        destRow.setHeight(srcRow.getHeight());
        //如果copy到另一个sheet的起始行数不同
        int deltaRows = destRow.getRowNum() - srcRow.getRowNum();
        int jn = srcRow.getFirstCellNum() < 0 ? 0 : srcRow.getFirstCellNum();
        for (int j = jn; j <= srcRow.getLastCellNum(); j++) {
            SXSSFCell oldCell = srcRow.getCell(j);
            SXSSFCell newCell = destRow.getCell(j);
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                copyCell(oldCell, newCell, styleMap);
                CellRangeAddress mergedRegion = getMergedRegion(srcSheet,
                        srcRow.getRowNum(), (short) oldCell.getColumnIndex());
                if (mergedRegion != null) {
                    CellRangeAddress newMergedRegion = new CellRangeAddress(
                            mergedRegion.getFirstRow() + deltaRows,
                            mergedRegion.getLastRow() + deltaRows, mergedRegion
                            .getFirstColumn(), mergedRegion
                            .getLastColumn());
                    CellRangeAddressWrapper wrapper = new CellRangeAddressWrapper(
                            newMergedRegion);
                    if (isNewMergedRegion(wrapper, mergedRegions)) {
                        mergedRegions.add(wrapper);
                        destSheet.addMergedRegion(wrapper.range);
                    }
                }
            }
        }
    }

    /**
     * 把原来的Sheet中cell（列）的样式和数据类型复制到新的sheet的cell（列）中
     *
     * @param oldCell  oldCell
     * @param newCell  newCell
     * @param styleMap styleMap
     */
    private static void copyCell(SXSSFCell oldCell, SXSSFCell newCell,
                                 Map<Integer, CellStyle> styleMap) {
        if (styleMap != null) {
            int stHashCode = oldCell.getCellStyle().hashCode();
            CellStyle newCellStyle = styleMap.get(stHashCode);
            if (newCellStyle == null) {
                newCellStyle = newCell.getSheet().getWorkbook()
                        .createCellStyle();
                newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                styleMap.put(stHashCode, newCellStyle);
            }
            newCell.setCellStyle(newCellStyle);
        }
        switch (oldCell.getCellType()) {
            case STRING:
                newCell.setCellValue(oldCell.getStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BLANK:
                newCell.setCellType(BLANK);
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        }

    }

    // 获取merge对象
    private static CellRangeAddress getMergedRegion(SXSSFSheet sheet, int rowNum,
                                                    short cellNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);
            if (merged.isInRange(rowNum, cellNum)) {
                return merged;
            }
        }
        return null;
    }

    private static boolean isNewMergedRegion(
            CellRangeAddressWrapper newMergedRegion,
            Set<CellRangeAddressWrapper> mergedRegions) {
        boolean bool = mergedRegions.contains(newMergedRegion);
        return !bool;
    }


    private static String sheetNameCanUse(String sheetName, List<String> sheetNames) {
        int suffix = 0;
        String nameCanUse = sheetName;
        while (sheetNames.contains(nameCanUse)) {
            nameCanUse = sheetName + "(" + suffix + ")";
            suffix += 1;
        }
        sheetNames.add(nameCanUse);
        return nameCanUse;
    }


    public static class CellRangeAddressWrapper implements Comparable<CellRangeAddressWrapper> {
        public CellRangeAddress range;

        public CellRangeAddressWrapper(CellRangeAddress theRange) {
            this.range = theRange;
        }

        @Override
        public int compareTo(CellRangeAddressWrapper craw) {
            if (range.getFirstColumn() < craw.range.getFirstColumn()
                    || range.getFirstRow() < craw.range.getFirstRow()) {
                return -1;
            } else if (range.getFirstColumn() == craw.range.getFirstColumn()
                    && range.getFirstRow() == craw.range.getFirstRow()) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
