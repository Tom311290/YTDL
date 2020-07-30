package com.codex.ytdl.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;

/**
 * Starts external process in new thread, and reads it's output to log
 * @author Tomislav
 *
 */
public class ExternalProcessStarter implements Callable<Object> {
	
	private ProcessBuilder processBuilder;
	private Process process;
	private Logger log;
	private Button startDownloadBtn;
	private String startProcessMessage = "";
	private String endProcessMessage = "";
	
	@Override
	public Object call() {
		try {
			
			disableStartButton();
			
			process = processBuilder.start();
			log.info(startProcessMessage);
			
			BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	
			String s = null;
			while ((s = lineReader.readLine()) != null) {
				log.info(s);
			}
	
			while ((s = errorReader.readLine()) != null) {
				log.info(s);
			}
			return true;
			
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return false;
			
		} finally {
			process.destroy();
			log.info(endProcessMessage);
			enableStartButton();
		}
	}
	
	public ProcessBuilder getProcessBuilder() {
		return processBuilder;
	}

	public void setProcessBuilder(ProcessBuilder processBuilder) {
		this.processBuilder = processBuilder;
	}

	public Logger getLog() {
		return log;
	}

	public void setLogger(Logger log) {
		this.log = log;
	}
	
	public void stop() {
		process.destroy();
	}

	public Button getStartDownloadBtn() {
		return startDownloadBtn;
	}

	public void setStartDownloadBtn(Button startDownloadBtn) {
		this.startDownloadBtn = startDownloadBtn;
	}
	
	private void disableStartButton() {
		if(startDownloadBtn != null) {
			startDownloadBtn.setDisable(true);
		}
	}
	
	private void enableStartButton() {
		if(startDownloadBtn != null) {
			startDownloadBtn.setDisable(false);
		}
	}

	public String getStartProcessMessage() {
		return startProcessMessage;
	}

	public void setStartProcessMessage(String startProcessMessage) {
		this.startProcessMessage = startProcessMessage;
	}

	public String getEndProcessMessage() {
		return endProcessMessage;
	}

	public void setEndProcessMessage(String endProcessMessage) {
		this.endProcessMessage = endProcessMessage;
	}
}
