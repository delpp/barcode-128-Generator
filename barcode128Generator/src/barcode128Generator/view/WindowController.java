package barcode128Generator.view;

import barcode128Generator.model.Barcode128Generator;
import barcode128Generator.model.Text;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

public class WindowController {
	private Barcode128Generator barcode128Generator;
	//@FXML private Button generuj;
	@FXML private Label folderIN;
	@FXML private Label folderOUT1;
	@FXML private Label folderOUT2;
	@FXML private TextField nameOfProduct;
	@FXML private TextField code;
	@FXML private TextField count;
	@FXML private TextField DEDNumber;
	@FXML private RadioButton lorealRadioBtn;
	@FXML private RadioButton lorealDEDRadioBtn;
	@FXML private RadioButton kompaniaRadioBtn;
	
	@FXML private ToggleGroup client;
	
	@FXML private void initialize(){
		barcode128Generator = new Barcode128Generator();
		barcode128Generator.init();
		folderIN.setText(barcode128Generator.getSearchFolder()); 
		folderOUT1.setText(barcode128Generator.getTargetFolder()); 
		folderOUT2.setText(barcode128Generator.getTargetFolder());
	}
	
	@FXML private void generateFromXLS() throws Exception{
		
		barcode128Generator.generateBarcodeFromXLS();

/*		Platform.runLater(() ->
			{
				try {
					barcode128Generator.generateBarcode();
					generuj.setDisable(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});*/
	}
	
	@FXML private void generateFromTextFields() throws Exception{
		String clientName = "";
		
		if (client.getSelectedToggle().equals(lorealRadioBtn)) clientName = "loreal";
		if (client.getSelectedToggle().equals(lorealDEDRadioBtn)) clientName = "lorealDED";	
		if (client.getSelectedToggle().equals(kompaniaRadioBtn)) clientName = "kompania";		
		
		if ((!nameOfProduct.getText().equals("")) 
				&& (!code.getText().equals("")) 
				&& (!count.getText().equals(""))){
					nameOfProduct.setText(Text.removeFreeSpaces(nameOfProduct.getText()));
					code.setText(Text.removeFreeSpaces(code.getText()));
					count.setText(Text.removeFreeSpaces(count.getText()));			
					barcode128Generator.generateBarcodeFromTextFields(nameOfProduct.getText(), code.getText(), count.getText(),	DEDNumber.getText(), clientName);
		}
			
	}
}
