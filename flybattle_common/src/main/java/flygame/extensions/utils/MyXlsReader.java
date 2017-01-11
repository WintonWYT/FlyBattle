package flygame.extensions.utils;

import flygame.common.ApplicationLocal;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MyXlsReader {
    private Workbook workbook;

    public MyXlsReader(String fileName) {
        File file = new File(fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            workbook = WorkbookFactory.create(fis);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Open %s error!", fileName), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Sheet getSheet(String sheetName) {
        return workbook.getSheet(sheetName);
    }

    public Sheet getSheet(int index) {
        return workbook.getSheetAt(index);
    }

    /**
     * 按行读取
     *
     * @param <T>
     * @param sheetName
     * @param rowIndex  需要读取的行，从0开始计数，如第一行是rowIndex = 0。
     *                  并且不能小于1，因为默认第一行是字段名
     * @param builder
     * @return
     */
    public <T> T getScalarRowData(String sheetName, int rowIndex, ExcelObjectBuilder<T> builder) {
        return getScalarRowData(getSheet(sheetName), rowIndex, builder);
    }

    public <T> T getScalarRowData(int sheetIndex, int rowIndex, ExcelObjectBuilder<T> builder) {
        return getScalarRowData(getSheet(sheetIndex), rowIndex, builder);
    }

    private <T> T getScalarRowData(Sheet sheet, int rowIndex, ExcelObjectBuilder<T> builder) {
        if (rowIndex < 0)
            throw new IllegalArgumentException("rowIndex cannot less than 1.");
        if (sheet == null) {
            return null;
        }
        Row row0 = sheet.getRow(sheet.getFirstRowNum());
        if(row0 == null) {
            throw new IllegalArgumentException("sheet is empty!!!");
        }
        int lastCols = row0.getLastCellNum();
        ExcelResultSet ers = new ExcelResultSet(1, lastCols);
        ers.next();
        for (int col = 0; col < lastCols; col++) {
            Cell cell = sheet.getRow(rowIndex).getCell(col);
            ers.updateCellValue(col, cell);
        }
        if (!ers.first())
            return null;
        return builder.build(ers);
    }

    /**
     * 按行读取多行
     *
     * @param <T>
     * @param sheetName
     * @param startRowIndex 从哪一行开始读，从0开始计数，如第一行是rowIndex = 0。
     *                      并且不能小于1，因为默认第一行是字段名
     * @param builder
     * @return
     */
    public <T> List<T> getRowDataList(String sheetName, int startRowIndex, ExcelObjectBuilder<T> builder) {
        return getRowDataList(getSheet(sheetName), startRowIndex, builder);
    }

    public <T> List<T> getRowDataList(int sheetIndex, int startRowIndex, ExcelObjectBuilder<T> builder) {
        return getRowDataList(getSheet(sheetIndex), startRowIndex, builder);
    }

    private <T> ArrayList<T> getRowDataList(Sheet sheet, int startRowIndex, ExcelObjectBuilder<T> builder) {
        if (startRowIndex < 0)
            throw new IllegalArgumentException("startRowIndex cannot less than 1.");
        if (sheet == null) {
            throw new IllegalArgumentException("sheet is null!!!");
        }
        Row row0 = sheet.getRow(sheet.getFirstRowNum());
        if(row0 == null) {
            throw new IllegalArgumentException("sheet is empty!!!");
        }
        int lastRows = sheet.getLastRowNum() + 1;  //返回时下标值
        int lastCols = row0.getLastCellNum();      //返回的是个数
        int rows = lastRows - startRowIndex;
        if(rows <= 0) {
            return new ArrayList<>(0);
        }
        ArrayList<T> resultList = new ArrayList<>(rows);
        ExcelResultSet ers = new ExcelResultSet(rows, lastCols);
        for (int row = startRowIndex; row < lastRows; row++) {
            if (!ers.next())
                break;
            for (int col = 0; col < lastCols; col++) {
                Cell cell = sheet.getRow(row).getCell(col);
                ers.updateCellValue(col, cell);
            }
        }
        ers.beforeFirst();
        while (ers.next()) {
            resultList.add(builder.build(ers));
        }
        return resultList;
    }

    /**
     * Remember to close me!!!
     */
    public void close() {
        try {
            workbook.close();
        } catch (IOException e) {
            ApplicationLocal.instance().error("", e);
        }
    }

}
