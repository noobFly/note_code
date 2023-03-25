package com.noob.easyexcel.demo;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * 比对cellData数据是否前后一致，合并单元格
 */
public class ExcelMergeHandler implements CellWriteHandler {
    private int[] mergeColumnIndex; // 哪些下标列需要合并 --- 按实际模板情况而定
    private int mergeRowIndex; // 从哪个下标行开始进入合并逻辑 --- 按实际模板情况而定


    public ExcelMergeHandler(int mergeRowIndex, int[] mergeColumnIndex) {
        this.mergeRowIndex = mergeRowIndex;
        this.mergeColumnIndex = mergeColumnIndex;
    }


    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        //当前行
        int curRowIndex = cell.getRowIndex();
        //当前列
        int curColIndex = cell.getColumnIndex();
        // 指定的行列才执行merge方法
        if (curRowIndex > mergeRowIndex) {
            for (int i = 0; i < mergeColumnIndex.length; i++) {
                if (curColIndex == mergeColumnIndex[i]) {
                    mergeWithPrevRow(writeSheetHolder, cell, curRowIndex, curColIndex);
                    break;
                }
            }
        }
    }


    /**
     * 当前单元格向上合并
     *
     * @param writeSheetHolder
     * @param cell             当前单元格
     * @param curRowIndex      当前行
     * @param curColIndex      当前列
     */
    private void mergeWithPrevRow(WriteSheetHolder writeSheetHolder, Cell cell, int curRowIndex, int curColIndex) {
        Object curData = cell.getCellTypeEnum() == CellType.STRING ? cell.getStringCellValue() : cell.getNumericCellValue();
        Cell preCell = cell.getSheet().getRow(curRowIndex - 1).getCell(curColIndex);
        Object preData = preCell == null ? null : (preCell.getCellTypeEnum() == CellType.STRING ? preCell.getStringCellValue() : preCell.getNumericCellValue());

        if (curData == null || preData == null) {
            return; // 为空不合并
        }

        // 将当前单元格数据与上一个单元格数据比较
        Boolean dataBool = preData.equals(curData);
        String s1 = cell.getRow().getCell(curColIndex).getStringCellValue();
        String s2 = cell.getSheet().getRow(curRowIndex - 1).getCell(curColIndex).getStringCellValue();
        /*BigDecimal d1 = new BigDecimal(cell.getRow().getCell(0).getNumericCellValue());
          BigDecimal d2 = new BigDecimal(cell.getSheet().getRow(curRowIndex - 1).getCell(0).getNumericCellValue());*/
        Boolean bool = s1.compareTo(s2) == 0 ? true : false;
        if (dataBool && bool) {
            Sheet sheet = writeSheetHolder.getSheet();
            List<CellRangeAddress> mergeRegions = sheet.getMergedRegions();
            boolean isMerged = false;
            for (int i = 0; i < mergeRegions.size() && !isMerged; i++) {
                CellRangeAddress cellRangeAddr = mergeRegions.get(i);
                // 若上一个单元格已经被合并，则先移出原有的合并单元，再重新添加合并单元
                if (cellRangeAddr.isInRange(curRowIndex - 1, curColIndex)) {
                    sheet.removeMergedRegion(i);
                    cellRangeAddr.setLastRow(curRowIndex);
                    sheet.addMergedRegion(cellRangeAddr);
                    isMerged = true;
                }
            }
            // 若上一个单元格未被合并，则新增合并单元
            if (!isMerged) {
                CellRangeAddress cellRangeAddress = new CellRangeAddress(curRowIndex - 1, curRowIndex, curColIndex, curColIndex);
                sheet.addMergedRegion(cellRangeAddress);
            }
        }
    }
}