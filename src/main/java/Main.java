/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Inaqui Insaurralde
 */
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Carga el archivo visual generado por Scene Builder
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ComisionesView.fxml"));
        
        Scene scene = new Scene(loader.load(), 400, 300);
        stage.setTitle("Gestor de Comisiones");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
