package com.codex.ytdl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application{

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		System.setProperty("log4j.configurationFile", "com/codex/ytdl/log4j2.xml");
		System.setProperty("YTDLTempPath", YTDLConstants.YTDL_TEMP_FOLDER);
		System.setProperty("logFileName", YTDLConstants.LOG_FILE);
		System.setProperty("logMessagePattern", YTDLConstants.LOG_MESSAGE_PATTERN);
        
		//closes all threads on exiting application
		Platform.setImplicitExit(true);
		primaryStage.setOnCloseRequest((ae) -> {
									            Platform.exit();
									            System.exit(0);
									        });
        
		Parent root = FXMLLoader.load(getClass().getResource("YTDLWindow.fxml"));
		primaryStage.setTitle("YouTube downloader");
		primaryStage.setScene(new Scene(root));
		primaryStage.setResizable(false);
		primaryStage.show();
	}
}
