package com.codex.ytdl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codex.ytdl.utils.ExternalProcessStarter;
import com.codex.ytdl.utils.GuiLogWriter;

public class Controller {

	@FXML
	public TextArea logScreen;
	@FXML
	public Button youTubDlLocationBtn;
	@FXML
	public Button downloadLocationBtn;
	@FXML
	public Button startDownloadBtn;
	@FXML
	public Button startUpdatingBtn;
	@FXML
	public Button startProcessBtn;
	@FXML
	public TextField youTubeDLLocationField;
	@FXML
	public TextField urlField;
	@FXML
	public TextField downloadLocationField;
	@FXML
	public TextField fileFormatField;

	Logger log = LogManager.getLogger(Controller.class.getName());
	Process process = null;

	Properties prop = null;
	String youTubeDLExeURI = "youTubeDLExeURI";
	String lastDownloadedURL = "lastDownloadedURL";
	String lastDownloadURI = "lastDownloadURI";
	String songNameTemplate = "songNameTemplate";

	//Get ExecutorService from Executors utility class, thread pool size is 10
    ExecutorService executorService;
    
	public void initialize() {
		//executorService = Executors.newFixedThreadPool(5);
		executorService = Executors.newFixedThreadPool(4,new ThreadFactory() {
											            public Thread newThread(Runnable r) {
											                Thread t = Executors.defaultThreadFactory().newThread(r);
											                t.setDaemon(true);
											                return t;
											            }
											        });
									        
		GuiLogWriter guiLogWriter = new GuiLogWriter();
		guiLogWriter.setupTextArea(logScreen);
		guiLogWriter.setExecutorService(executorService);
		guiLogWriter.startLogFileTailer(new File(YTDLConstants.YTDL_TEMP_FOLDER + "/" + YTDLConstants.LOG_FILE));
		
		log.info("========================================================================================================");
		log.info("Starting application...");

		try {
			prop = new Properties();
			File initFile = null;
			InputStream input = null;

			//logScreen.appendText("Initialization of application started... \n");
			log.info("Initialization of application started...");

			try {
				initFile = new File(YTDLConstants.YTDL_TEMP_FOLDER, YTDLConstants.INIT_FILE);
				input = new FileInputStream(initFile);

			}catch (FileNotFoundException e){

				//logScreen.appendText("No init file! Creating one: " + initFile.getAbsolutePath()+"\n");
				log.warn("No init file! Creating one: " + initFile.getAbsolutePath());

				initFile.getParentFile().mkdirs();
				initFile.createNewFile();
				input = new FileInputStream(initFile);
			}

			prop.load(input);
			youTubeDLLocationField.setText(getPropertyIfExists(youTubeDLExeURI));//"D:\\Music\\youTube dl\\youtube-dl.exe");
			urlField.setText(getPropertyIfExists(lastDownloadedURL));//https://www.youtube.com/watch?v=KsDZix4ZSlUasda;
			downloadLocationField.setText(getPropertyIfExists(lastDownloadURI));//C:\\Users\\Tomislav\\Desktop\\New folder100;

			fileFormatField.setText(getPropertyIfExists(songNameTemplate));//%(title)s.%(ext)s
			if(fileFormatField == null || fileFormatField.getText() == null || (fileFormatField.getText()).equals("")) {
				fileFormatField.setText("%(title)s.%(ext)s");
			}

			//logScreen.setText("Initialization of application finished!\n");
			log.info("Initialization of application finished!");

			updateYTDL();

		}catch (IOException e) {
			//logScreen.appendText(YTDLConstants.FATAL_ERROR_MESSAGE);
			log.error("Unable to load last settings!", e);
			e.printStackTrace();
		}
	}

	@FXML
	public void startDownload() {
		try {
			String message = "";
			message = checkField(youTubeDLLocationField);
			message = message + checkField(downloadLocationField);
			message = message + checkField(fileFormatField);
			message = message + checkField(urlField);

			if(!message.equals("")){
				Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
				log.info("---------------------------------------------------------------------");
				log.info("Please take care of errors:" + message);
				log.info("---------------------------------------------------------------------");
				alert.showAndWait();
				return;
			}

			log.info("Download started! Please wait...");
			String downloadFilePatt = "\"" +downloadLocationField.getText() + "\\" + fileFormatField.getText() + "\"";

			log.info("Download to:" + downloadFilePatt);
			ProcessBuilder processBuilder = new ProcessBuilder(youTubeDLLocationField.getText(), 
					"--extract-audio", 
					"--audio-format", 
					"mp3", 
					"-o", 
					downloadFilePatt, 
					urlField.getText());

			startProcess(processBuilder, "Download started...", "Download finished!");

			log.info("Saving last setup...");

			if(youTubeDLLocationField.getText() != null && youTubeDLLocationField.getText() != null && !youTubeDLLocationField.getText().equals("")) {
				prop.setProperty(youTubeDLExeURI, youTubeDLLocationField.getText());
			}

			if(urlField.getText() != null && urlField.getText() != null && !urlField.getText().equals("")) {
				prop.setProperty(lastDownloadedURL, urlField.getText());
			}

			if(downloadLocationField.getText() != null && downloadLocationField.getText()!= null && !downloadLocationField.getText().equals("")) {
				prop.setProperty(lastDownloadURI, downloadLocationField.getText());
			}

			if(fileFormatField.getText() != null && fileFormatField.getText() != null && !fileFormatField.getText().equals("")) {
				prop.setProperty(songNameTemplate, fileFormatField.getText());
			}

			prop.store(new FileOutputStream(YTDLConstants.YTDL_TEMP_FOLDER + YTDLConstants.INIT_FILE), null);

			log.info("Saving finished!");

		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@FXML
	public void updateYTDL() {
		log.info("Checking for updates of youtube-dl application! Please wait...");

		if(youTubeDLLocationField != null && youTubeDLLocationField.getText() != null && !youTubeDLLocationField.getText().equals("")) {
			ProcessBuilder processBuilder = new ProcessBuilder(youTubeDLLocationField.getText(), "-U", "--verbose");
			startProcess(processBuilder, "Updating started...", "Updating finished!");
		}else {
			log.warn("Youtube-dl location not set! Unable to update application!");
		}

	}

	private void startProcess(ProcessBuilder processBuilder, String startProcMessage, String endProcMessage) {
		ExternalProcessStarter extProcStarter = new ExternalProcessStarter();
		extProcStarter.setLogger(log);
		extProcStarter.setProcessBuilder(processBuilder);
		extProcStarter.setStartDownloadBtn(startDownloadBtn);
		extProcStarter.setStartProcessMessage(startProcMessage);
		extProcStarter.setEndProcessMessage(endProcMessage);
		executorService.submit(extProcStarter);
	}

	@FXML
	public void chooseYTDLLocation(ActionEvent ae) {
		final File youTubeDLLocation = fileChooser(ae, "YTDL exe file", "youtube-dl.exe");
		youTubeDLLocationField.setText((youTubeDLLocation != null) ? youTubeDLLocation.getAbsolutePath() : youTubeDLLocationField.getText());
		checkField(youTubeDLLocationField);
	}

	@FXML
	public void chooseDownloadLocation(ActionEvent ae) {
		File youTubeDLLocation = directoryChooser(ae, downloadLocationField.getText());

		downloadLocationField.setText((youTubeDLLocation != null) ? youTubeDLLocation.getAbsolutePath() : downloadLocationField.getText());
		checkField(downloadLocationField);
	}

	private File fileChooser(ActionEvent ae, String... params){
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select path");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(params[0], params[1]));

		File file = fileChooser.showOpenDialog(getStage(ae));		

		return file;
	}

	private File directoryChooser(ActionEvent ae,  String startDirPath){
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select directory");

		if(startDirPath != null && !startDirPath.equals("")) {
			File startDir = new File(startDirPath);
			if (startDir != null && !startDir.exists()){
				//logScreen.appendText("Creating directory: " + startDir.getAbsolutePath());
				log.info("Creating directory: " + startDir.getAbsolutePath());
				startDir.mkdirs();
			}

			chooser.setInitialDirectory(startDir);
		}

		File selectedDirectory = chooser.showDialog(getStage(ae));

		return selectedDirectory;
	}

	private String checkField(TextField field){

		String message = "";

		if (field != null && field.getText() != null && !field.getText().equals("")){
			field.setStyle("");

		}else{
			field.setStyle("-fx-effect: innershadow( three-pass-box, rgba( 255, 0, 0, 0.5 ), 10, 0, 0, 0 );");

			final String fieldId = field.getId();

			switch(fieldId) {
			case "youTubeDLLocationField":
				message = "\n1. youtube-dl.exe location missing!";
				break;
			case "downloadLocationField":
				message = "\n2. Please choose download folder location!";
				break;
			case "fileFormatField":
				message = "\n3. Name pattern of downloaded file is missing!";
				break;
			case "urlField":
				message = "\n4. YouTube video or playlist URL missing!";
				break;
			default:
				break;
			}
		}
		return message;
	}

	private Stage getStage(ActionEvent ae){
		Node source = (Node) ae.getSource();
		Stage theStage = (Stage) source.getScene().getWindow();

		return theStage;
	}

	private String getPropertyIfExists(String propertyName) {
		String propertyValue = prop.getProperty(propertyName);
		if(propertyValue != null && !propertyValue.equals("")) {
			return propertyValue;
		}
		return null;
	}
}
