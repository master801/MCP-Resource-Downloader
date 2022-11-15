package org.slave.mcprd.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;

public final class AppMCPRD extends Application {

    @Override
    public void start(final Stage primaryStage) throws Exception {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("mcprd");
        URL url = AppMCPRD.class.getResource("/mcprd.fxml");
        if (url == null) throw new FileNotFoundException("Could not find file \"mcprd.fxml\"!");
        FXMLLoader fxmlLoader = new FXMLLoader(url, resourceBundle);

        Parent root = fxmlLoader.load();
        MCPRDController fxmlController = fxmlLoader.getController();

        Scene scene = new Scene(root);

        primaryStage.setTitle("MCP Resource Downloader");
        primaryStage.setResizable(false);
        fxmlController.init(primaryStage, resourceBundle);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
