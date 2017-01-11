package flygame.extensions.utils;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 写入xls
 */
public class MyXlsWriter {

    private FileOutputStream out;
    private Workbook workbook;
    private Sheet sheet;

    public MyXlsWriter(String fileName) throws IOException {
        out = new FileOutputStream(fileName);
        if(fileName.endsWith(".xls")) {
            workbook = new HSSFWorkbook();
        } else {
            workbook = new XSSFWorkbook();
        }
    }

    /**
     * 切换sheet
     * 如果不存在会新加一个
     * @param sheetName
     */
    public void changeSheet(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if(sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        this.sheet = sheet;
    }

    /**
     * 往sheet设置值
     * @param row
     * @param col
     * @param value
     */
    public void addCellValue(int row, int col, Object value) {
        Row rowData = this.sheet.getRow(row);
        if(rowData == null) {
            rowData = this.sheet.createRow(row);
        }
        Cell cell = rowData.getCell(col);
        if(cell == null) {
            cell = rowData.createCell(col);
        }
        this.setCellValue(cell, value);
    }

    private void setCellValue(Cell cell, Object value) {
        if(value instanceof Number) {
            cell.setCellValue(Double.parseDouble(String.valueOf(value)));
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        } else if(value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            cell.setCellType(Cell.CELL_TYPE_BOOLEAN);
        } else {
            cell.setCellValue((String) value);
            cell.setCellType(Cell.CELL_TYPE_STRING);
        }
    }

    /**写入并且关闭流*/
    public void close() throws IOException {
        this.workbook.write(out);
        this.out.close();
        this.workbook.close();
    }

}
