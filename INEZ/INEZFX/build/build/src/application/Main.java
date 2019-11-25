package application;
	
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.*;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
	private Controller control;
    @Override
    public void start(Stage stage) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
    	Parent root = loader.load();
        control = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
       
    }
    
    @Override
    /*
     * saves the user product list, before application gets closed
     */
    public void stop(){
        System.out.println("Stage is closing");
        control.saveUserList();
        
    }

    public static void main(String[] args) {
        launch();
    }
    
    
}
