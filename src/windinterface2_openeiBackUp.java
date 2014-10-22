import java.awt.AWTException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class windinterface2_openeiBackUp extends HttpServlet {
	//Conversion constants.....
	final double windConvert = 7.2D;
	final double speedConvert = 1.125D;
	final double powerConvert = 0.12D;
	final double voltsConvert = 0.03571428571428571D;
	final double dayEnergyConvert = 0.2D;
	//End constants....

	int avgCount = 0; //Used when printing avgs
	int maxAvgCount = 20;  //Used when printing avgs
	boolean averagesReadyToPrint = false;  //Used when printing avgs
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
	boolean debug = true;
	String errorLog;
	FileWriter errorFileWriter;
	PrintWriter errorStream;
	//TODO: Should GMT offset be negative?
	public static void main(String[] args) throws AWTException, IOException {
		new windinterface2_openeiBackUp(args);
	}
	public windinterface2_openeiBackUp(final String[] args) {
		System.out.println("Initialing....");
		try {
			settings = new File(myPath + "windinterfacepref.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(settings);
			doc.getDocumentElement().normalize();
			NodeList list = doc.getElementsByTagName("setup");
			if (list.item(0).getNodeType() == 1) {
				Element element = (Element)list.item(0);
				NumTurbines = Integer.parseInt(element.getElementsByTagName("num_turbines").item(0).getFirstChild().getNodeValue());
				Turbines = new WindTurbine[NumTurbines];
			}
			list = doc.getElementsByTagName("system");
			if (list.item(0).getNodeType() == 1) {
				Element element = (Element)list.item(0);
				myDBURL = element.getElementsByTagName("dbURL").item(0).getFirstChild().getNodeValue();
				if (myDBURL == null | myDBURL == "") { myDBURL = "none"; }
				myMySQLURL = element.getElementsByTagName("mysqlURL").item(0).getFirstChild().getNodeValue();
				if (myMySQLURL == null | myMySQLURL == "") { myMySQLURL = "none"; }
				myMySQLUser = element.getElementsByTagName("mysqlUser").item(0).getFirstChild().getNodeValue();
				if (myMySQLUser == null | myMySQLUser == "") { myMySQLUser = "none"; }
				myMySQLPass = element.getElementsByTagName("mysqlPass").item(0).getFirstChild().getNodeValue();
				if (myMySQLPass == null | myMySQLPass == "") { myMySQLPass = "none"; }
				myGMT_Offset = Double.parseDouble(element.getElementsByTagName("gmt_offset").item(0).getFirstChild().getNodeValue());
			}
			errorLog = "errorlog.txt";
			errorFileWriter = new FileWriter(errorLog,true);
			errorStream = new PrintWriter(errorFileWriter);
		}
		catch (Exception e) {
			errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			e.printStackTrace();
		}
		windowSystemName = "Open EI Wind Interface";
		System.out.println("windinterface version openei 2.1 using:");
		System.out.println("dbURL: " + myDBURL + ", mySQLURL: " + myMySQLURL + ", mySQLUser: " + myMySQLUser + ", GMT+ " + myGMT_Offset);
		System.out.println("Initialization complete.");
		System.out.println("Loading Turbines.....");
		loadTurbines();
	}

	void loadTurbines() {
		try {
			File file = new File(myPath + "windinterfacepref.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();
			String SYSTitle, SYSID, SerialNum, SYSName, APIKey;
			SYSTitle=SYSID=SerialNum=SYSName=APIKey = "none";
			Double PWROffset = 0.0;
			for (int n = 0; n < NumTurbines; n++) {
				NodeList list = doc.getElementsByTagName(String.valueOf((char)(n + 65)));
				if (list.item(0).getNodeType() == 1) {
					Element element = (Element)list.item(0);
					SYSTitle = element.getElementsByTagName("sys_title").item(0).getFirstChild().getNodeValue();
					SYSID = element.getElementsByTagName("sys_id").item(0).getFirstChild().getNodeValue().replaceAll("\\W", "");
					SerialNum = element.getElementsByTagName("serial_num").item(0).getFirstChild().getNodeValue();
					SYSName = element.getElementsByTagName("sys_name").item(0).getFirstChild().getNodeValue();
					APIKey = element.getElementsByTagName("api_key").item(0).getFirstChild().getNodeValue();
					PWROffset = Double.parseDouble(element.getElementsByTagName("pwr_offset").item(0).getFirstChild().getNodeValue());
				}
				System.out.println(SYSTitle + ", " + SYSName + ", " + SYSID + ", " + SerialNum + ", " + APIKey + ", " + PWROffset);
				Turbines[n] = new WindTurbine(this, SYSTitle, SYSName, SYSID, SerialNum, APIKey, PWROffset);
				TimerTask = new WindTimerTask();
				TimerTask.init(Turbines);
				timer = new Timer();
				timer.schedule(TimerTask, 0L, 30000L); //Run for 30s with 0s delay....
			} //end turbine loop
		}
		catch (NullPointerException e) {
			System.out.println("Error reading from config file. Check turbine count?");
			errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e) {
			errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("Loaded Turbines.");
	}
	public String getDBURL () {
		return myDBURL;
	}
	public String getMySQLURL () {
		return myMySQLURL;
	}
	public String getPath () {
		return myPath;
	}
	public Double getGMTOffset () {
		return myGMT_Offset;
	}
	public String getMySQLUser () {
		return myMySQLUser;
	}
	protected String getMySQLPass () {
		return myMySQLPass;
	}
	public boolean isDebug() {
		return debug;
	}
	public void errorLog(String s) {
		errorStream = new PrintWriter(errorFileWriter);
		errorStream.append(s);
		errorStream.close();
	}
	public String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
}