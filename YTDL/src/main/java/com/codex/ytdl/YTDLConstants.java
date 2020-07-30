package com.codex.ytdl;

public class YTDLConstants {

	//static final String  YTDL_TEMP_FOLDER = "C:/Users/" + System.getProperty("user.name") + "/YTDLTemp/";
	static final String  YTDL_TEMP_FOLDER = "C:/Users/Public/YTDLTemp/";
	static final String INIT_FILE = "initialize.txt";
	static final String LOG_FILE = "log.txt";
	static final String LOG_MESSAGE_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} |%level| %C{1}: %m%n";
	
	static final String FATAL_ERROR_MESSAGE = "Something went worng! Please check log file " + LOG_FILE + " in " + YTDL_TEMP_FOLDER + "\n";

}
