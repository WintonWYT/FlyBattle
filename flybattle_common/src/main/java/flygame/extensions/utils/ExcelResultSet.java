package flygame.extensions.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.util.ArrayList;

public class ExcelResultSet {
	private static class CellValue{
		final Cell cell;

		CellValue(Cell cell, int columnIndex){
			this.cell = cell;
		}
	}
	
	private static class RowValue{
		private final CellValue [] columnArray;
		
		RowValue(int colLength){
			columnArray = new CellValue [colLength];
		}
		
		void setCellValue(Cell cell, int columnIndex){
			CellValue cellValue = new CellValue(cell, columnIndex);
			columnArray[columnIndex] = cellValue;
		}
		
		CellValue getCellValue(int columnIndex){
			return columnArray[columnIndex];
		}

	}
	
	private ArrayList<RowValue> rowList;
	private int curIndex;
	private int colLength;
	
	public ExcelResultSet(int rowLength, int colLength){
		this.colLength = colLength;
		rowList = new ArrayList<RowValue>(rowLength);
		for(int index = 0; index < rowLength; index ++){
			rowList.add(new RowValue(colLength));
		}
		curIndex = -1;
	}

	private String getCellValue(Cell cell) {
		if(cell == null) {
			return "";
		}
		DataFormatter df = new DataFormatter();
		String str;
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_FORMULA:
				try {
					str = String.valueOf(cell.getNumericCellValue());
				} catch (IllegalStateException e) {
					str = String.valueOf(cell.getRichStringCellValue());
				}
				break;
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_BOOLEAN:
			case Cell.CELL_TYPE_NUMERIC:
			case Cell.CELL_TYPE_STRING:
                str = df.formatCellValue(cell);
				break;
			default:
				str = null;
				break;
		}
		return str;
	}
	
	public int getColLength() {
		return colLength;
	}
	
	protected void beforeFirst(){
		curIndex = -1;
	}
	
	protected boolean first(){
		if(rowList.size() == 0){
			curIndex = -1;
			return false;
		}
		curIndex = 0;
		return true;
	}
	
	protected boolean next(){
		if(curIndex == rowList.size() -1){
			curIndex ++;
			return false;
		}
		if(curIndex >= rowList.size()){
			return false;
		}
		curIndex ++;
		return true;
	}
	
	protected void updateCellValue(int columnIndex, Cell cell){
		RowValue rowValue = rowList.get(curIndex);
		rowValue.setCellValue(cell, columnIndex);
	}
	
	/**
	 * @param columnIndex 从0开始，比如第一列是 columnIndex = 0
	 * @return
	 */
	public boolean getBoolean(int columnIndex){
		RowValue row = rowList.get(curIndex);
		Cell cell = row.getCellValue(columnIndex).cell;
		return parseBoolean(getCellValue(cell));
	}
	
	public int getInt(int columnIndex){
		RowValue row = rowList.get(curIndex);
		Cell cell = row.getCellValue(columnIndex).cell;
		return (int) parseDouble(getCellValue(cell));
	}
	
	public short getShort(int columnIndex)
	{
		RowValue row = rowList.get(curIndex);
		Cell cell = row.getCellValue(columnIndex).cell;
		return parseShort(getCellValue(cell));
	}

	public double getDouble(int columnIndex){
		RowValue row = rowList.get(curIndex);
		Cell cell = row.getCellValue(columnIndex).cell;
		return parseDouble(getCellValue(cell));
	}

	public float getFloat(int columnIndex){
		RowValue row = rowList.get(curIndex);
		Cell cell = row.getCellValue(columnIndex).cell;
		return parseFloat(getCellValue(cell));
	}
	
	public String getString(int columnIndex){
		RowValue row = rowList.get(curIndex);
		Cell cell = row.getCellValue(columnIndex).cell;
		return getCellValue(cell);
	}
	
	private boolean parseBoolean(String content) {
		boolean result = false;
		try {
			result = Boolean.parseBoolean(content);
		} catch (Throwable t) {
			throw new RuntimeException(String.format("[index:%d]", curIndex), t);
		}
		return result;
	}
	
	private int parseInt(String content) {
		int result = -1;
		try {
			result = Integer.parseInt(content);
		} catch (Throwable t) {
			throw new RuntimeException(String.format("[index:%d]", curIndex), t);
		}
		return result;
	}
	
	private short parseShort(String content) {
		short result = -1;
		try {
			result = Short.parseShort(content);
		} catch (Throwable t) {
			throw new RuntimeException(String.format("[index:%d]", curIndex), t);
		}
		return result;
	}
	
	private double parseDouble(String content) {
		double result = 0;
		try {
			result = Double.parseDouble(content);
		} catch (Throwable t) {
			throw new RuntimeException(String.format("[index:%d]", curIndex), t);
		}
		return result;
	}
	
	private float parseFloat(String content) {
		float result = 0;
		try {
			result = Float.parseFloat(content);
		} catch (Throwable t) {
			throw new RuntimeException(String.format("[index:%d]", curIndex), t);
		}
		return result;
	}

}
