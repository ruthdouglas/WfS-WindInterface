package openei.windinterface;

import java.awt.AWTException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Mark McKay, Justin Leis, Ian Mason
 * @version 2.3.2 OpenEI
 */

public class windinterface2_openei {
	public static void main(String[] args) throws AWTException, IOException {
		new windinterface2_openei(args);
	}
	// Conversion constants.....
	final double windConvert = 7.2D;
	final double speedConvert = 1.125D;
	final double powerConvert = 0.12D;
	final double voltsConvert = 0.03571428571428571D;

	final double dayEnergyConvert = 0.2D;
	// End constants....
	int avgCount = 0; // Used when printing avgs, stores # of avgs collected
							int maxAvgCount = 20; // Used when printing avgs (Total possible avgs, 10min
	// * 2 points/min)
	boolean averagesReadyToPrint = false; // Used when printing avgs
	String myDBURL;
	String myMySQLURL;
	String myMySQLUser;
	String myMySQLPass;
	Double myGMT_Offset;
	String myPath = "resources/";
	String windowSystemName;
	File settings;
	WindTurbine[] Turbines;
	WindTimerTask TimerTask;
	Timer timer;
	int NumTurbines;
	boolean debug = false;
	String errorLog;
	String debugLog;
	FileWriter errorFileWriter;
	FileWriter debugFileWriter;
	PrintWriter errorStream;
	PrintWriter debugStream;
	String WIVersion = "2.3 Release 3 Multi-Turbine";

	String WIName = "OpenEI Wind Interface";

	/**
	 * @param args
	 *            Any parameters passed to the jar via command line, none
	 *            matter. This method loads the config from XML, and initiates
	 *            the loading of turbine data.
	 */
	public windinterface2_openei(final String[] args) {
		System.out.println("Initialing....");
		errorLog = "errorlog.txt";
		debugLog = "debuglog.txt";
		try {
			settings = new File(myPath + "windinterfacepref.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(settings);
			doc.getDocumentElement().normalize();
			NodeList list = doc.getElementsByTagName("setup");
			if (list.item(0).getNodeType() == 1) {
				Element element = (Element) list.item(0);
				NumTurbines = Integer
						.parseInt(element.getElementsByTagName("num_turbines").item(0).getFirstChild().getNodeValue());
				Turbines = new WindTurbine[NumTurbines];
			}
			list = doc.getElementsByTagName("system");
			if (list.item(0).getNodeType() == 1) {
				Element element = (Element) list.item(0);
				if (element.getElementsByTagName("dbURL").item(0) != null
						&& element.getElementsByTagName("dbURL").item(0).getFirstChild() != null)
					myDBURL = element.getElementsByTagName("dbURL").item(0).getFirstChild().getNodeValue();
				if (myDBURL == null || myDBURL == "")
					myDBURL = "none";

				if (element.getElementsByTagName("mysqlURL").item(0) != null
						&& element.getElementsByTagName("mysqlURL").item(0).getFirstChild() != null)
					myMySQLURL = element.getElementsByTagName("mysqlURL").item(0).getFirstChild().getNodeValue();
				if (myMySQLURL == null || myMySQLURL == "")
					myMySQLURL = "none";

				if (element.getElementsByTagName("mysqlUser").item(0) != null
						&& element.getElementsByTagName("mysqlUser").item(0).getFirstChild() != null)
					myMySQLUser = element.getElementsByTagName("mysqlUser").item(0).getFirstChild().getNodeValue();
				if (myMySQLUser == null || myMySQLUser == "")
					myMySQLUser = "none";

				if (element.getElementsByTagName("mysqlPass").item(0) != null
						&& element.getElementsByTagName("mysqlPass").item(0).getFirstChild() != null)
					myMySQLPass = element.getElementsByTagName("mysqlPass").item(0).getFirstChild().getNodeValue();
				if (myMySQLPass == null || myMySQLPass == "")
					myMySQLPass = "none";

				myGMT_Offset = Double
						.parseDouble(element.getElementsByTagName("gmt_offset").item(0).getFirstChild().getNodeValue());
				NodeList debugNode = element.getElementsByTagName("debug");
				if (element.getElementsByTagName("debug").item(0) != null) {
					debug = Boolean.parseBoolean(debugNode.item(0).getFirstChild().getNodeValue());
					if (debug)
						System.out.println("Debug Mode: ENABLED");
				}
			}
		} catch (NullPointerException e) {
			System.out.println("Error reading from config file. Error in system or setup section.");
			errorLog(now("HH:mm dd MM yyyy") + Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
			return;
		} catch (Exception e) {
			errorLog(now("HH:mm dd MM yyyy") + Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
			return;
		}
		Package p = this.getClass().getPackage();
		if (p.getSpecificationTitle() != null)
			WIName = p.getSpecificationTitle();
		if (p.getSpecificationVersion() != null)
			WIVersion = p.getSpecificationVersion();
		System.out.println(WIName + " Version: " + WIVersion + " using:");
		System.out.println("dbURL: " + myDBURL + ", mySQLURL: " + myMySQLURL + ", mySQLUser: " + myMySQLUser + ", GMT- "
				+ myGMT_Offset);
		System.out.println("Initialization complete.");
		System.out.println("Loading Turbines.....");
		loadTurbines();
	}

	public void debugLog(String s) {
		if (s != null && s != "") {
			try {
				debugFileWriter = new FileWriter(debugLog, true);
				debugStream = new PrintWriter(debugFileWriter);
				debugStream.append("\n" + now("HH:mm:ss dd MM yyyy") + " " + s);
				debugStream.close();
				debugFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(now("HH:mm:ss dd MM yyyy") + " " + s);
		}
	}

	/**
	 * @param s
	 *            String to be saved to the error log. Method for logging errors
	 *            to errorlog.txt
	 */
	public void errorLog(String s) {
		if (s != null && s != "") {
			try {
				errorFileWriter = new FileWriter(errorLog, true);
				errorStream = new PrintWriter(errorFileWriter);
				errorStream.append("\n" + now("HH:mm:ss dd MM yyyy") + " " + s);
				errorStream.close();
				errorFileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (getDebug())
				debugLog(s);
			else
				System.out.println(now("HH:mm:ss dd MM yyyy") + " " + s);
		}
	}

	/**
	 * @return Returns the Database URL. Getter method for the Database URL.
	 */
	public String getDBURL() {
		return myDBURL;
	}

	/**
	 * @return Returns a boolean to determine if Debug mode is enabled. Getter
	 *         method for Debug mode.
	 */
	public boolean getDebug() {
		return debug;
	}

	/**
	 * @return Returns the MySQL Password from XML. Getter method for the MySQL
	 *         Password. This is protected to other programs outside the package
	 *         can't hook in and grab the password
	 */
	protected String getMySQLPass() {
		return myMySQLPass;
	}

	/**
	 * @return Returns the MySQL Database URL. Getter method for the MySQL
	 *         Database URL.
	 */
	public String getMySQLURL() {
		return myMySQLURL;
	}

	/**
	 * @return Returns the MySQL Username from XML. Getter method for the MySQL
	 *         Username.
	 */
	public String getMySQLUser() {
		return myMySQLUser;
	}

	/**
	 * @return Returns the Local Path of the JAR. Getter method for the Local
	 *         Path of the JAR.
	 */
	public String getPath() {
		return myPath;
	}

	/**
	 * This method loads turbine setting data, and passed it to the WindTurbine
	 * class
	 */
	void loadTurbines() {
		try {
			File file = new File(myPath + "windinterfacepref.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			String SYSTitle, SYSID, SerialNum, SYSName, APIKey;
			SYSTitle = SYSID = SerialNum = SYSName = APIKey = "none";
			Double PWROffset = 0.0;
			for (int n = 0; n < NumTurbines; n++) {
				NodeList list = doc.getElementsByTagName(String.valueOf((char) (n + 65)));
				if (list.item(0).getNodeType() == 1) {
					Element element = (Element) list.item(0);
					SYSTitle = element.getElementsByTagName("sys_title").item(0).getFirstChild().getNodeValue();
					SYSID = element.getElementsByTagName("sys_id").item(0).getFirstChild().getNodeValue()
							.replaceAll("\\W", "");
					SerialNum = element.getElementsByTagName("serial_num").item(0).getFirstChild().getNodeValue()
							.replace("-", "");

					if (element.getElementsByTagName("sys_name").item(0) != null
							&& element.getElementsByTagName("sys_name").item(0).getFirstChild() != null)
						SYSName = element.getElementsByTagName("sys_name").item(0).getFirstChild().getNodeValue();
					if (SYSName == null || SYSName == "" || SYSName == "none") {
						if (!myDBURL.equals("none")) {
							System.out.println("ERROR: dbURL specificed but no sys_name set!!!");
							return;
						}
						SYSName = "none";
					}

					APIKey = element.getElementsByTagName("api_key").item(0).getFirstChild().getNodeValue();

					String PWRtemp = "";
					if (element.getElementsByTagName("pwr_offset").item(0) != null
							&& element.getElementsByTagName("pwr_offset").item(0).getFirstChild() != null)
						PWRtemp = element.getElementsByTagName("pwr_offset").item(0).getFirstChild().getNodeValue();
					if (PWRtemp == null || PWRtemp == "") {
						PWROffset = 0D;
					} else {
						PWROffset = Double.parseDouble(PWRtemp);
					}
				}
				if (!SerialNum.substring(0, 2).equalsIgnoreCase("30"))
					SerialNum = SerialNum.substring(2);
				System.out.println(
						SYSTitle + ", " + SYSName + ", " + SYSID + ", " + SerialNum + ", " + APIKey + ", " + PWROffset);
				Turbines[n] = new WindTurbine(this, SYSTitle, SYSName, SYSID, SerialNum, APIKey, PWROffset);
			} // end turbine loop
			TimerTask = new WindTimerTask();
			TimerTask.init(Turbines);
			timer = new Timer();
			timer.scheduleAtFixedRate(TimerTask, 0, 30000); // Run for 30s with
															// 0s delay....
		} catch (NullPointerException e) {
			System.out.println("Error reading from config file. Check turbine count? Check all field have values?");
			errorLog(now("HH:mm dd MM yyyy") + Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
		} catch (Exception e) {
			errorLog(now("HH:mm dd MM yyyy") + Arrays.toString(e.getStackTrace()));
			e.printStackTrace();
		}
		System.out.println("Loaded Turbines.");
	}

	/**
	 * @param dateFormat
	 *            Format for the date.
	 * @return Current date formatted using date parameters. Method for
	 *         requesting portions of the date, or a formatted date.
	 */
	public String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
}