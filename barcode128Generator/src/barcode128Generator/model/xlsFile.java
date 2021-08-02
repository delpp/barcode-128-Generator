package barcode128Generator.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class xlsFile {
		
	public static File[] findXLSFiles(String sourcePath){
		File[] xlsFiles = null;
		int size = 0;
		int count = 0;
		File directory = new File(sourcePath);
		File[] childrenFilesXLSX = directory.listFiles(
        		(dir, name) -> {
        				return name.toLowerCase().endsWith(".xlsx");
        		}
        		);		
		
		if (childrenFilesXLSX != null){
			for (File file : childrenFilesXLSX){
				if (!file.isHidden()) size++;
			}
			
			xlsFiles = new File[size];
			
			for (int i = 0; i < childrenFilesXLSX.length; i++){
				if (!childrenFilesXLSX[i].isHidden()) {
					xlsFiles[count] = childrenFilesXLSX[i];
					count++;
				}
			}			
		}		
		
		return xlsFiles;
	}
	
	public static int[] initRowCells(){
		int[] cellRowNumber = new int[5];
		cellRowNumber[0] = 23; // row of barcodecode and nameOfProduct
		cellRowNumber[1] = 30;
		cellRowNumber[2] = 31;
		cellRowNumber[3] = 33;
		cellRowNumber[4] = 19; // row of DED number
		return cellRowNumber;
	}
	
	public static String setSheet(){
		return "WZ - awizacja dostawy";
	}
	
	@SuppressWarnings("deprecation")
	public static ArrayList<ArrayList<String>> getTextsFromXLSFile(File childrenXLSFile) {	
						
		ArrayList<ArrayList<String>> tickets = new ArrayList<ArrayList<String>>();
		
		int[] cellRowNumber = initRowCells();
		int codeColumn = 2;
		int nameOfProductColumn = 5;
		int nameOfDEDColumn = 2;
		String sheetName = setSheet();
		
		Cell cell;
		int tempInt = 0;
		String tempString;
		
		try{
			FileInputStream file = new FileInputStream(childrenXLSFile);
			
			System.out.println("ścieżka i nazwa pliku: " + childrenXLSFile);
			
			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheet(sheetName);	
		
			String code = sheet.getRow(cellRowNumber[0]).getCell(codeColumn).getStringCellValue();
			String nameOfProduct = sheet.getRow(cellRowNumber[0]).getCell(nameOfProductColumn).getStringCellValue();
			
			String DEDnumber = sheet.getRow(cellRowNumber[4]).getCell(nameOfDEDColumn).getStringCellValue();
			
			if (code != null){			
				code = Text.removeFreeSpaces(code);
				DEDnumber = Text.removeFreeSpaces(DEDnumber);
				
				if (nameOfProduct != null) nameOfProduct = Text.removeFreeSpaces(nameOfProduct);
				
				
				for (int i = 1; i < 4 ; i++){				
					
					cell = sheet.getRow(cellRowNumber[i]).getCell(2);
					
					System.out.println(cell + ", typ komórki:" + cell.getCellType());
					


					
					if (cell.getCellType() == 0){
						tempInt = (int) sheet.getRow(cellRowNumber[i]).getCell(2).getNumericCellValue();
						tempString = tempInt + "";
					}
					
					else
						tempString = sheet.getRow(cellRowNumber[i]).getCell(2).getStringCellValue();
					
					
					if ((tempInt > 0) || (tempString != null)){
						ArrayList<String> texts = new ArrayList<String>();
						texts.add(nameOfProduct);
						texts.add(tempString);
						texts.add(code);	
						texts.add(DEDnumber);
						tickets.add(texts);		
						tempInt = 0;
						tempString = null;
					}			
				}			
			}			
			workbook.close();
			file.close();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}		
		
		
		System.out.println("tickets:" + tickets);
		
		
		return tickets;		
	}
}
