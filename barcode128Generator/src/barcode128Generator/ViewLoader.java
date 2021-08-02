package barcode128Generator;

import java.io.IOException;

import javafx.fxml.FXMLLoader;


public class ViewLoader<T,U> {
	
	/** The View loader. */
	private T ViewLoader = null;
	
	/** The fxml controller. */
	private U fxmlController = null;
	
	/**
	 * Instantiates a new view loader.
	 *
	 * @param fxml the fxml
	 */
	public ViewLoader(String fxml){
		try{
			FXMLLoader fxmlLoader = new
					FXMLLoader(Main.class.getResource(fxml));
			
			ViewLoader = fxmlLoader.load();
			fxmlController = fxmlLoader.getController();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the layout.
	 *
	 * @return the layout
	 */
	public T getLayout(){ return ViewLoader;}
	
	/**
	 * Gets the controller.
	 *
	 * @return the controller
	 */
	public U getController() { return fxmlController;}
}

