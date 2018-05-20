package transform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
	private static final DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy_MM_dd_HHmm");
	private File fc;
	private String Path;
	private boolean appendToFile;	
	
	public Logger(String rootpath) {
		appendToFile = true;
		this.Path = rootpath+"\\log\\log_"+getDateLogFile()+".txt";
		loggerSetup();
	}

	private void loggerSetup() {
		fc = new File(Path);
		try {
			fc.getParentFile().mkdirs();
			fc.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (setpermission(fc)) {
			l("LOGGING [STARTED] - Log file - "+fc);
		} else {
			String emsg = "[ERROR] LOGGING NOT STARTED Log file -" + fc;
			l(emsg);
		}
	}

	private boolean setpermission(File f) {
		boolean b = false;
		if (f.exists()) {
			f.setExecutable(true);
			f.setReadable(true);
			f.setWritable(true);
		} else {
			try {
				b = f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				
			}
		}
		if (f.canExecute() && f.canRead() && f.canWrite()) {
			b = true;
		}
		return b;
	}

	//short version of Write to logs files
	public void l(String msg) {
		log(msg);
	}

	// Writes to Log Files
	public void log(String msg) {
		String m = logMsg(msg);
		writelog(m);
	}
	
	//returns Date for Log file creation
	private String getDateLogFile() {
		String res = "";
		LocalDateTime now = LocalDateTime.now();
		res = dtf2.format(now);
		return res;
	}
	//gets Date of log Event
	private String getDateLogEvent() {
		String res = "";
		LocalDateTime now = LocalDateTime.now();
		res = dtf.format(now);
		return res;
	}
	// Adds Date to log Msg String
	private String logMsg(String text) {
		String res="";
		if(text!=null&&text!=""&&text!=" ") {
			res = "[" + getDateLogEvent() + "] " + text;
		}
		return res;
	}

	// WRITES TO LOG FILE
	private void writelog(String msg) {
		String filename = Path;
		if (Path != null && Path != "" && (msg!=null && msg!="" && msg!=" ")) {
			String linesToWrite = msg;
			PrintWriter pw = null;
			if (appendToFile) {
				try {
					pw = new PrintWriter(new FileWriter(filename, true));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				try {
					pw = new PrintWriter(new FileWriter(filename));
				} catch (IOException e) {
					e.printStackTrace();
				}
				// if overwrite file then pw = new PrintWriter(new FileWriter(filename, false));
			}
			pw.println(linesToWrite);
			System.out.println("\r"+linesToWrite);
			pw.flush();
			pw.close();
		}
	}
	
}