package com.codex.ytdl.utils;

import java.io.File;
import java.util.concurrent.ExecutorService;

import com.codex.ytdl.interfaces.LogFileTailerListener;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class GuiLogWriter implements LogFileTailerListener {
	TextArea logTextArea;
	ExecutorService executorService;
	
	@Override
	public void newLogFileLine(String line) {
		//without runLater overfloods JavaFX thread (at least I think so)
		Platform.runLater(() -> logTextArea.appendText(line + "\n"));
	}
	
	public void setupTextArea(TextArea textArea) {
		logTextArea = textArea;
	}
	
	public void startLogFileTailer(File fileToTail) {
		LogFileTailer logFileTailer = new LogFileTailer(fileToTail, 1000, false);
		logFileTailer.addLogFileTailerListener(this);
		if(executorService != null) {
			executorService.submit(logFileTailer);
		}
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

}
