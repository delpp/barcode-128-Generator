package barcode128Generator.model;

import java.awt.FontMetrics;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class Barcode128Generator  {
	
	private int width; // ticket width in mm
	private int height; // ticket height in mm
	private float widthPX; // ticket width in pixels
	private float heightPX; // ticket height  in pixels

	private static int matrixWidth;
	private static int matrixHeight;

	private static PDFont fontPDF;
	private static FontMetrics fm;
	private static double temp;
	
	private BitMatrix bitMatrix;
	private ArrayList<ArrayList<String>> textsToTicket;
	private ArrayList<Integer> barcodeProductCode;
	private ArrayList<Integer> barcodeDED;
	
	private Code128Writer code128Writer;
	private Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap;
	
	private File[] childrenXLSFiles;
	private String searchFolder;
	private String targetFolder;
	private String fontFolder;
	
	private String client;
	
	public String getSearchFolder() {
		return searchFolder;
	}

	public String getTargetFolder() {
		return targetFolder;
	}

	
	public void init(){	
		System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
		
		String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);

		if (OS.indexOf("mac") >= 0) {		
			//folders for macOS
			searchFolder = "/Applications/barcodeGenerator/IN/";
			targetFolder = "/Applications/barcodeGenerator/OUT/";
			fontFolder = "/Applications/barcodeGenerator/open-sans/OpenSans-Regular.ttf";
			//fontFolder = "/Applications/barcodeGenerator/carbon font/carbon bl.ttf";
		}
		else {
			//folders for Windows and others systems	
			searchFolder = "C:\\barcodeGenerator\\IN\\";
			targetFolder = "C:\\barcodeGenerator\\OUT\\"; 
			fontFolder = "C:\\barcodeGenerator\\open-sans\\OpenSans-Regular.ttf";
			//fontFolder = "C:\\barcodeGenerator\\carbon font\\carbon bl.ttf";
		}
			
		//fontFolder = "/Users/Marcin/Library/Fonts/0759EU27.ttf";
		
		width = 50;
		height = 30;
		
		/*width = 105;
		height = 70;*/	
			
		//temp = width*72/25.4;
        widthPX = (float) (width*72/25.4);
        
        //temp = height*72/25.4;
        heightPX = (float) (height*72/25.4);
        
		//fontSize = 16;
		
		//System.out.println("wysokość etykiety: " + heightPX + " px");
		
		textsToTicket = new ArrayList<>();
		barcodeProductCode = new ArrayList<Integer>();
		barcodeDED = new ArrayList<Integer>();
		
		//barcode
		code128Writer = new Code128Writer();	
		hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
	}
	
	public int setFontSizeForPDF(int startFontSize, String name, PDFont font, PDPageContentStream content) throws Exception{
		boolean fontSizeOK = false;
		int fontSize = startFontSize;
		do {
			content.setFont(font, fontSize);
			
			if (font.getStringWidth(name)/1000 * fontSize > widthPX-5) // "-5" bo uwzględnia marginesy wewnętrzne
				fontSize--;
			else fontSizeOK = true;
			
			if (fontSize < 7) fontSizeOK = true;
		}
		while (!fontSizeOK);
		
		return fontSize;
	}
	
	
	public int setNameWidth(String name){
		return fm.stringWidth(name);
	}	
		
	public ArrayList<Integer> prepareBarcodeToPDF(BitMatrix bitMatrix, Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap) throws Exception{

		matrixWidth = bitMatrix.getWidth();
		matrixHeight = bitMatrix.getHeight();
				
		boolean number = false;
		int counter = 0;
		ArrayList<Integer> kreski = new ArrayList<Integer>();
		
		// odczytaj jedną poziomą linię obrazu - oczytaj binarnie kod 
		for (int i = 0; i < matrixWidth; i++) {			
			if (bitMatrix.get(i, 1)) {
				if (number == true) counter++;
				else{
					kreski.add(counter);
					counter = 1;
					number = true;
				}				
			}
				else {
					if (number == false) counter++;
						else {
							kreski.add(counter);
							counter = 1;
							number = false;
						}
				}			
		}
		kreski.add(counter);
		return kreski;
	}
	
	
	public void insertTextsIntoPDF(PDPageContentStream content, PDFont font, ArrayList<String> textsToTicket, int shiftFromBottom) throws Exception{	
		float size;
		int fontSizeBottomText = 1;
		int fontSize;
		int fontSizeForKompania;
        
        content.beginText();

		// ustawia font
        fontSize = setFontSizeForPDF(14, textsToTicket.get(0), font, content);        
        content.setFont(font, fontSize);
        
        fontSizeForKompania = fontSize;
       
        // wstaw nazwe na górze
        size = fontSize * font.getStringWidth(textsToTicket.get(0)) / 1000;                            
        content.newLineAtOffset((widthPX-size)/2, heightPX-17-shiftFromBottom);
        content.showText(textsToTicket.get(0));

        
        
		// ustawia font
        fontSize = setFontSizeForPDF(fontSize, textsToTicket.get(1), font, content);        
        content.setFont(font, fontSize);
        
		// wstaw liczbe sztuk na górze
        if (fontSize > 10)
        	content.newLineAtOffset(-(widthPX-size)/2, -14);
        else
        	content.newLineAtOffset(-(widthPX-size)/2, -12);
		
		size = fontSize * font.getStringWidth(textsToTicket.get(1)) / 1000;			
		content.newLineAtOffset((widthPX-size)/2, 0);
        content.showText(textsToTicket.get(1));
 
        
        // wstaw tekst - numer kodu pod barcodem    
        if (client == "kompania") fontSizeBottomText = (int) Math.round(fontSizeForKompania*1.5);
        //if (client == "kompania") fontSizeBottomText = 14; //21; // "21" to jest to samo co: = (int) Math.round(14*1.5);
        	else fontSizeBottomText = fontSize;
        
        if (fontSizeBottomText > 10)
        	content.newLineAtOffset(-(widthPX-size)/2, -heightPX+34);
        else
        	content.newLineAtOffset(-(widthPX-size)/2, -heightPX+38);
        
        content.setFont(font, fontSizeBottomText);
        
        size = (fontSizeBottomText * font.getStringWidth(textsToTicket.get(2)) / 1000);
        content.newLineAtOffset((widthPX-size)/2, 0);
        content.showText(textsToTicket.get(2));
        
        
        if (client == "lorealDED") {
        	fontSizeBottomText = fontSize;
        	if (fontSizeBottomText > 10)
            	content.newLineAtOffset(-(widthPX-size)/2, -54);
            else
            	content.newLineAtOffset(-(widthPX-size)/2, -55);
            
            content.setFont(font, fontSizeBottomText);
            
            size = (fontSizeBottomText * font.getStringWidth(textsToTicket.get(3)) / 1000);
            content.newLineAtOffset((widthPX-size)/2, 0);
            content.showText(textsToTicket.get(3));
        	
        }
        content.endText();       
	}
	
	
	public void insertBarcodeIntoPDF(PDPageContentStream content, ArrayList<Integer> barcode) throws Exception{
		int shift;
		int moveCodeFromBottomLine = 0;

		if (client == "kompania") moveCodeFromBottomLine = 21;
				else moveCodeFromBottomLine = 17;
		
		shift = barcode.get(0);		
		for (int i = 1; i < barcode.size(); i+=2){		
			content.addRect(shift, moveCodeFromBottomLine, barcode.get(i), matrixHeight);		
			shift = shift + barcode.get(i) + barcode.get(i+1);
		}
        content.fill();       
	}
	
	public void insertWithDEDAsBarcodeIntoPDF(PDPageContentStream content, ArrayList<Integer> barcode, int moveCodeFromBottomLine, double reductionRatio) throws Exception{
		double shift;
		double tempWidth = 0;
		
		for (int i = 0; i < barcode.size(); i+=1){			
			tempWidth = tempWidth + barcode.get(i) * reductionRatio;
		}	
		
		shift = barcode.get(0) * reductionRatio + (widthPX - tempWidth) / 2;
			
		for (int i = 1; i < barcode.size(); i+=2){		
			content.addRect( (float) shift, moveCodeFromBottomLine, (float) (barcode.get(i) * reductionRatio), matrixHeight);		
			shift = shift + barcode.get(i) * reductionRatio + barcode.get(i+1) * reductionRatio;
		}
        content.fill();       
	}
	

	public void writeWithDEDNumberPDF(ArrayList<String> textsToTicket, ArrayList<Integer> barcode, ArrayList<Integer> barcodeDED, double reductionRatioCode, double reductionRatioDED){
		PDDocument doc = null;
        PDPage page = null;    
        
        int heightDocumentForTwoBarcodes = 50; // mm
        
        temp = heightDocumentForTwoBarcodes*72/25.4;

        
        PDRectangle format = new PDRectangle(widthPX, (float) temp);
        try{
           doc = new PDDocument();
           page = new PDPage();
           doc.addPage(page);
           
           page.setMediaBox(format);
           
           fontPDF = PDType0Font     		   
        		   .load(doc, new File(fontFolder));	
             
           PDPageContentStream content = new PDPageContentStream(doc, page);
           
           insertTextsIntoPDF(content, fontPDF, textsToTicket, -55);      
           
           
           insertWithDEDAsBarcodeIntoPDF(content, barcode, 72, reductionRatioCode); // 72 -- przesunięcie od dołu
           
           insertWithDEDAsBarcodeIntoPDF(content, barcodeDED, 17, reductionRatioDED); // 17 -- przesunięcie od dołu
           
           content.close();                     
          
          doc.save(targetFolder + textsToTicket.get(2) + "_" + textsToTicket.get(1) + ".pdf");
          doc.close();
    } catch (Exception e){
        System.out.println(e);
    }
	}
	
	public void writePDF(ArrayList<String> textsToTicket, ArrayList<Integer> barcode){
		PDDocument doc = null;
        PDPage page = null;        
        PDRectangle format = new PDRectangle(widthPX, heightPX);
        try{
           doc = new PDDocument();
           page = new PDPage();
           doc.addPage(page);
           
           page.setMediaBox(format);
           
           fontPDF = PDType0Font     		   
        		   .load(doc, new File(fontFolder));	
             
           PDPageContentStream content = new PDPageContentStream(doc, page);
           
           insertTextsIntoPDF(content, fontPDF, textsToTicket, 0);          
           insertBarcodeIntoPDF(content, barcode);
           
           content.close();                     
          
          doc.save(targetFolder + textsToTicket.get(2) + "_" + textsToTicket.get(1) + ".pdf");
          doc.close();
    } catch (Exception e){
        System.out.println(e);
    }
	}
	
	
	public ArrayList<ArrayList<String>> readXLS(File childrenXLSFile){
		return xlsFile.getTextsFromXLSFile(childrenXLSFile);
	}
	
	public  File[] searchFolder(String sourcePath){
		return xlsFile.findXLSFiles(sourcePath);
	}
	
	public void generateBarcodeFromXLS() throws Exception{
				
		childrenXLSFiles = searchFolder(searchFolder);
		
		for (File childrenXLSFile : childrenXLSFiles){

			textsToTicket = readXLS(childrenXLSFile);

			for (int i =  0; i< textsToTicket.size(); i++){
				if (textsToTicket.get(i).get(2).length() > 0){
								
					bitMatrix = code128Writer.encode(textsToTicket.get(i).get(2), BarcodeFormat.CODE_128, (int) widthPX, (int) heightPX-50, hintMap);				
					barcodeProductCode = prepareBarcodeToPDF(bitMatrix, hintMap);
					
					bitMatrix = code128Writer.encode(textsToTicket.get(i).get(3), BarcodeFormat.CODE_128, (int) widthPX, (int) heightPX-50, hintMap);				
					barcodeDED = prepareBarcodeToPDF(bitMatrix, hintMap);
					
					
					writePDF(textsToTicket.get(i), barcodeProductCode);
				}
				
			}
		}
	}
	
	public void generateBarcodeFromTextFields(
			String nameOfProduct, 
			String code, 
			String count, 
			String DEDnumber, 
			String clientName) 
					throws Exception{
		
		int heightOfBarcode = 1;
		int widthOfBarcode;
		ArrayList<String> texts = new ArrayList<String>();		
		texts.add(nameOfProduct);
		texts.add(count);
		texts.add(code);
		texts.add(DEDnumber);
		
		widthOfBarcode = (int) widthPX;
		
		if (clientName.equals("loreal")) {
			client = "loreal";
			heightOfBarcode = (int) heightPX-50;
			bitMatrix = code128Writer.encode(code, BarcodeFormat.CODE_128, widthOfBarcode, heightOfBarcode, hintMap);
			barcodeProductCode = prepareBarcodeToPDF(bitMatrix, hintMap);
			writePDF(texts, barcodeProductCode);
		}
		
		
		if ((clientName.equals("lorealDED")) && (!DEDnumber.isEmpty())){
			client = "lorealDED";
			heightOfBarcode = (int) heightPX-50;
			
			double reductionRatioCode;
			double reductionRatioDED;
			
			
			bitMatrix = code128Writer.encode(code, BarcodeFormat.CODE_128, widthOfBarcode, heightOfBarcode, hintMap);			
			barcodeProductCode = prepareBarcodeToPDF(bitMatrix, hintMap);
			reductionRatioCode = (widthPX - 10) / bitMatrix.width; // odejmuje 10, aby kod nie był rozciągnięty na całą etykietę 
			
			bitMatrix = code128Writer.encode(DEDnumber, BarcodeFormat.CODE_128, widthOfBarcode, heightOfBarcode, hintMap);		
			barcodeDED = prepareBarcodeToPDF(bitMatrix, hintMap);
			
			reductionRatioDED = (widthPX - 10) / bitMatrix.width; // odejmuje 10, aby kod nie był rozciągnięty na całą etykietę – żeby miał marginesy
			
			writeWithDEDNumberPDF(texts, barcodeProductCode, barcodeDED, reductionRatioCode, reductionRatioDED);
		}
		
		
		if (clientName.equals("kompania")) {
			client = "kompania";
			heightOfBarcode = (int) heightPX-55;
			bitMatrix = code128Writer.encode(code, BarcodeFormat.CODE_128, widthOfBarcode, heightOfBarcode, hintMap);
			barcodeProductCode = prepareBarcodeToPDF(bitMatrix, hintMap);
			writePDF(texts, barcodeProductCode);
		}
		
		
	}
		
	public static void main (String[] args) throws Exception{
		Barcode128Generator barcode128Generator = new Barcode128Generator();
		barcode128Generator.init();
		barcode128Generator.generateBarcodeFromXLS();	
	}
}
