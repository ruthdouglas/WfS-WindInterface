package openei.windinterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

/**
 * Class created to hold properties of a Winnd Turbine. This class holds all things specific to an individual turbine, and
 * methods to help perform tasks specific to that turbine.
 */
public class WindTurbine {
	int avgCount = 0; //Used when printing avgs
	int maxAvgCount = 20;  //Used when printing avgs, 1 reading every 30 sec, 10 min avgs
	boolean averagesReadyToPrint = false;  //Used when printing avgs
	double[][] avgData = new double[maxAvgCount][40];
	double[] tenMinAvgData = new double[40];
	//Vars from config...
	String mySysTitle;
	String mySysID;
	String mySerialNum;
	String mySysName;
	String myApiKey;
	double myPowerOffset;
	//End Vars from config...
	//Vars from turbine...
	double power;
	double wind;
	double speed;
	double volts;
	double RPM;
	double Watts;
	double Wind;
	double GMT;
	int ts = 0000;
	int ss = 0000;
	int gs = 0000;
	double myDailyTotal = 0.0D;
	double totEnergy;
	String CurrentDate = "";
	//End Vards from turbine...
	boolean counted = false;
	int sendToDBError = 1;
	int sendToDBOptError = 1;
	windinterface2_openei parent; //So you can pull needed references from the main class
	/**
	 * @param wioei A reference to the parent that created it, a Wind Interface instance, to use for accessing parameters
	 * @param SystemTitle The actual name of the turbine
	 * @param SystemName Short name for the turbine, AKA password
	 * @param SystemID The zigBee Id for the turbine
	 * @param SerialNum The turbine serial number
	 * @param APIKey The APIKey for OpenEI
	 * @param PowerOffset The Power offset if there was a new inverter installed, otherwise 0
	 * Initialization method for creating a new WindTurbine with all parameters.
	 */
	public WindTurbine(windinterface2_openei wioei, String SystemTitle, String SystemName, String SystemID, String SerialNum, String APIKey, double PowerOffset) {
		parent = wioei;
		mySysTitle = SystemTitle;
		mySysID = SystemID;
		mySerialNum = SerialNum;
		mySysName = SystemName;
		myApiKey = APIKey;
		myPowerOffset = PowerOffset;
		File dataFile3 = new File(mySysTitle + "_tenminaveragewindturbine.csv");
		try {
			dataFile3.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		dataFile3 = new File(mySysTitle + "_mostcurrentwindturbine.csv");
		try {
			dataFile3.createNewFile();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		myDailyTotal = readDailyTot();
		System.out.println("Loaded Wind Turbine: " + SystemTitle);
	}
	/**
	 * @return Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 * Method for sending data to OpenEI.
	 */
	private int sendToOpenEIDataBase() {
		int didWork = 0;
		int TurbineStatus = ts;
		int GridStatus = gs;
		int SystemStatus = ss;

		String[] myTime = CurrentDate.split(" ");
		String[] myDate = myTime[0].split("/");
		String[] myTimeHMS = myTime[1].split(":");

		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
		try {
			String[] keys = {"turbineID","Day&time","PowerW","DailyKW","TotalKW","RPM","Wind","Volts","Tstat","Sstat","Gstat"};

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] data = {mySerialNum,tempString,Double.toString(power),Double.toString(myDailyTotal),Double.toString(Watts),Double.toString(RPM),Double.toString(Wind),Double.toString(volts),String.format("%04d",TurbineStatus),String.format("%04d",SystemStatus),String.format("%04d",GridStatus)};
			if (parent.getDebug()) System.out.println(mySysTitle + " OpenEI Data: " + Arrays.toString(data));
			for (int i=0;i < keys.length;i++) {
				dataMap.put(keys[i], data[i]);
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = "http://en.openei.org/services/api/2/wfs/w/" + mySerialNum + "?api_key=" + myApiKey + "&" + "json_data=" + jsonString;
			if (parent.getDebug()) System.out.println(mySysTitle + "OpenEI URL: " + urlString);
			URL url = new URL(urlString);

			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				check = s;
			}
			if (check != null) {
				if (parent.getDebug()) System.out.println(mySysTitle + "OpenEI return: " + check);
				if (check.substring(0, 6).equals("{\"Item")) {
					didWork = 1;
					System.out.println(mySysTitle + "Data was Successfully sent to OpenEI");
				}
				else {
					didWork = -1;
					System.out.println(mySysTitle + "OpenEI Error! " + s);
				}
			}
			else {
				System.out.println(mySysTitle + "Unable to connect to OpenEI!" + s );
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog(mySysTitle + "URL ERROR (OpenEI): " +  e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Error executing the  statement: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog(mySysTitle + "IOException (OpenEI): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "IO exception: " + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}
	/**
	 * @param baseURL Local database URL.
	 * @param values Data to be sent to database.
	 * @return  Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 * Method to send Turbine data to local database via HTTP.
	 */
	private int sendToLocalDataBase(String baseURL) {
		int didWork = 0;

		String[] myTime = CurrentDate.split(" ");
		String[] myDate = myTime[0].split("/");
		String[] myTimeHMS = myTime[1].split(":");

		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + "%20" + hour + ":" + min + ":" + sec;
		try {
			String[] keys = {"turbineID","GMTtime","Day&time","PowerW","DailyKW","TotalKW","RPM","Wind","Volts","Tstat","Sstat","Gstat"};

			String[] data = {mySerialNum,Double.toString(GMT),tempString,Double.toString(power),Double.toString(myDailyTotal),Double.toString(Watts),Double.toString(RPM),Double.toString(Wind),Double.toString(volts),String.format("%04d",ts),String.format("%04d",ss),String.format("%04d",gs)};
			if (parent.getDebug()) System.out.println(mySysTitle + "LocalDB Data: " + Arrays.toString(data));
			Map<String, String> dataMap = new HashMap<String, String>();

			for (int i=0;i < keys.length;i++) {
				dataMap.put(keys[i], data[i]);
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = baseURL + "?systemname=" + mySysName + "&" + "json_data=" + jsonString;

			if (parent.getDebug()) System.out.println(mySysTitle + "LocalDB URL: " + urlString);
			URL url = new URL(urlString);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				check = s;
			}
			if (parent.getDebug()) System.out.println(mySysTitle + "LocalDB return: " + check);
			if (check.equals("SUCCESS")) {
				didWork = 1;
				System.out.println(mySysTitle + "Data was Successfully sent to Local 30s Database (HTTP)");
			}
			else {
				didWork = -1;
				parent.errorLog(mySysTitle + "Error sending to Local 30s Database (HTTP) " + s);
				System.out.println(mySysTitle + "Error sending to Local 30s Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog(mySysTitle + "URL Error (LocalDB): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") +  " " + mySysTitle + "Error executing the  statement: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog(mySysTitle + "IOException (localDB): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "IO exception: " + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	/**
	 * @param values Data to be sent to database.
	 * @return  Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 * Method for sending Turbine data to local 30s SQL database. 
	 */
	private int sendTo30sMysqlDatabase() {
		int error = 1;

		Connection connection;
		try {
			Class.forName("org.gjt.mm.mysql.Driver");

			String dbURL = parent.getMySQLURL();
			String username = parent.getMySQLUser();
			String password = parent.getMySQLPass();
			connection = DriverManager.getConnection(dbURL, username, password);
		}
		catch (ClassNotFoundException e) {
			parent.errorLog(mySysTitle + "Class Error (30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Database driver not found.");
			return -1;
		}
		catch (SQLException e) {
			parent.errorLog(mySysTitle + "SQL Error on open (30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Local Error opening the db connection: " + e.getMessage());
			return -1;
		}
		try {
			String myQry = "INSERT into windturbine( \tpower       ,\tvolts,\twindspeed,\ttotalpower,\trpm,\tcurrenttime) VALUES (?,?,?,?,?,?) ";
			if (parent.getDebug()) System.out.println(mySysTitle + "SQL 30s DB query: " + myQry);
			PreparedStatement ps = connection.prepareStatement(myQry);
			ps.setDouble(1, power);
			ps.setDouble(2, volts);
			ps.setDouble(3, Wind);
			ps.setDouble(4, Watts);
			ps.setDouble(5, RPM);
			Timestamp sqlTimestamp = new Timestamp((long) ((GMT + (parent.getGMTOffset()) * 3600.0D) * 1000L));
			ps.setTimestamp(6, sqlTimestamp);

			System.out.println(mySysTitle + "Trying Backup MySQL Database (30s)...");
			ps.executeUpdate();
		}
		catch (SQLException e) {
			parent.errorLog(mySysTitle + "SQL Error on send (30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Error executing the SQL statement: " + e.getMessage());
			error = -1;
		}
		try {
			connection.close();
		}
		catch (SQLException e) {
			parent.errorLog(mySysTitle + "SQL Error on close(30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Error closing the db connection: " + e.getMessage());
		}
		if (error == 1) {
			System.out.println(mySysTitle + "Data was Successfully sent to Backup Mysql Database (30s)");
		}
		return error;
	}

	/**
	 * @return  Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 * Method for sending ten min average data to local database via HTTP.
	 */
	private int sendTo10minLocalDatabase() {
		if (parent.getDBURL().equals("none")) {
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Skipped 10min avg DB send because no DBURL set");
			return -1;
		}
		int didWork = 0;
		/*power = tenMinAvgData[13]
		volts = tenMinAvgData[6]
		Watts = tenMinAvgData[4]
		RPM = tenMinAvgData[19]
		Wind = tenMinAvgData[20]*/
		Timestamp sqlTimestamp = new Timestamp((long) ((tenMinAvgData[2] + (parent.getGMTOffset()) * 3600.0D) * 1000L));

		try {
			String[] keys = {"Day&time","PowerW","TotalKW","RPM","Wind","Volts"};
			String[] data = {sqlTimestamp.toString(),String.valueOf(tenMinAvgData[13]),String.valueOf(tenMinAvgData[4]),String.valueOf(tenMinAvgData[19]),String.valueOf(tenMinAvgData[20]),String.valueOf(tenMinAvgData[6])};
			Map<String, String> dataMap = new HashMap<String, String>();

			for (int i=0;i < keys.length;i++) {
				dataMap.put(keys[i], data[i]);
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = parent.getDBURL() + "?10minavg=yes&systemname=" + mySysName + "&" + "json_data=" + jsonString;

			if (parent.getDebug()) System.out.println(mySysTitle + "10min URL: " + urlString);
			URL url = new URL(urlString);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				check = s;
			}
			if (check.equals("SUCCESS")) {
				didWork = 1;
				System.out.println(mySysTitle + "Data was Successfully sent to Local 10minAvg Database (HTTP)");
			}
			else {
				didWork = -1;
				parent.errorLog(mySysTitle + "Error sending to Local 10minAvg Database (HTTP) " + s);
				System.out.println(mySysTitle + "Error sending to Local 10minAvg Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog(mySysTitle + "URL Error (10mDB): "  + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog(mySysTitle + "IO Error (10mSQL): "  + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	/**
	 * @return Daily total read from current turine file.
	 * Method for reading daily power total from turbine file.
	 */
	private double readDailyTot() {
		double dailyTot = 0.0D;
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(mySysTitle + "_mostcurrentwindturbine.csv"));
			String inLine = null;
			String lastLine = null;
			while ((inLine = inStream.readLine()) != null) {
				lastLine = inLine;
			}
			if (lastLine != null) {
				if (parent.getDebug()) System.out.println(mySysTitle + "most current turbine data: " + lastLine);
				String[] d = lastLine.split(",");
				if (d.length > 2) {
					dailyTot = Double.parseDouble(d[5]);
				}
			}
			inStream.close();
		}
		catch (IOException e) {
			parent.errorLog(mySysTitle + "IO Error (ReadDailyTot): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "IOExcepton:");
			e.printStackTrace();
		}
		return dailyTot;
	}
	/**
	 * @return Array of doubles containing all turbine data.
	 * Method to pulling data from Wind Turbine using skzcmd.
	 */
	public double[] getskzcmd() {
		double[] theData = new double[40];
		Arrays.fill(theData, 0.0);
		try {
			double myTime = 0.0D;
			double pwrTot = 0.0D;
			int numberOfChar = 0;
			String OS = System.getProperty("os.name");
			String execPath = parent.getPath() + "skzcmd.exe -z +" + mySysID + " dstat 1 0";
			if (!OS.startsWith("Windows")) {
				execPath = "./" + parent.getPath() + "s2zcmd -z +" + mySysID + " dstat 1 0";
			}
			Process p = Runtime.getRuntime().exec(execPath);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;

			PrintWriter MonthlyData = new PrintWriter(new FileWriter(mySysTitle + "_ss" + now("yyyy_MM") + ".csv", true));
			while ((line = input.readLine()) != null) {
				if (parent.getDebug()) System.out.println(mySysTitle + "From skzcmd: " + line);
				String[] d = line.split(",");
				if (d.length >= 2) {
					if (parent.getDebug()) System.out.println(mySysTitle + "Raw data: " + Arrays.toString(d));
					theData[0] = Double.parseDouble(d[0].replaceAll("\\D", ""));
					String[] tempd1 = d[1].split(" ");
					theData[1] = Double.parseDouble(tempd1[0].replaceAll("\\D", ""));
					theData[2] = Double.parseDouble(tempd1[1]);

					theData[4] = Double.parseDouble(d[2]);

					for (int ii = 3; ii < d.length - 1; ii++) {
						if(ii != 1 && ii != 27 && ii != 28) theData[(ii + 3)] = Double.parseDouble(d[ii]);
					}
					numberOfChar = Array.getLength(theData);
					myTime = theData[2];

					pwrTot = theData[4];

					theData[4] = pwrTot + myPowerOffset;
					CurrentDate = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:z").format(Double.valueOf((myTime + (parent.getGMTOffset()) * 3600.0D) * 1000.0D));
					int tempHrs = Integer.parseInt(now("HH"));
					int tempMin = Integer.parseInt(now("mm"));
					if ((tempHrs == 0) && (tempMin == 0)) {
						myDailyTotal = 0.0D;
					}
					myDailyTotal += (theData[13] * 0.0083333D); //This is 1/120, because there are 120 per hour
					theData[5] = readDailyTot();

					File dataFile = new File(mySysTitle + "_ss" + now("yyyy_MM") + ".csv");
					if (parent.getDebug()) System.out.println(mySysTitle + "Formatted data: " + Arrays.toString(theData));
					if (dataFile.length() > 0) {
						MonthlyData.println(Arrays.toString(theData));
					}
					else {
						MonthlyData.println("Turbine ID,SW Version,Time(sec),Time(MDY:HMS),watt-hours,DailyTot,Voltage In,Voltage DC Bus,Voltage L1,Voltage L2,voltage rise,min v from rpm,Current out,Power out,Power reg,Power max,Line Frequency,Inverter Frequency,Line Resistance,RPM,Windspeed (ref meters/sec),TargetTSR,Ramp RPM,Boost pulswidth,Max BPW,current amplitude, T1,T2,T3,Event count,Last event code,Event status,Event value,Turbine status,Grid status,System status,Slave Status,Access Status,Timer,");
						MonthlyData.println(Arrays.toString(theData));
					}
				}
			}
			input.close();
			MonthlyData.close();
			//avg start
			if (numberOfChar >= 39) {
				if(parent.getDebug()) System.out.println(mySysTitle + "Adding to avgData: " + Arrays.toString(theData));
				avgData[avgCount] = theData;
				avgCount++;
				if (avgCount == maxAvgCount) {
					avgCount = 0;
					counted = true;
				}
				if (counted) {
					for(int col = 0;col < 40; col++) {
						tenMinAvgData[col] = 0;
						for (int row = 0;row < maxAvgCount; row++) {
							tenMinAvgData[col] += avgData[row][col];
						}
						tenMinAvgData[col] = Math.round(((tenMinAvgData[col]/20.00d)*1000))/1000;
					}
				}
				else {
					for(int col = 0;col < 40; col++) {
						tenMinAvgData[col] = 0;
						for (int row = 0;row < maxAvgCount; row++) {
							tenMinAvgData[col] += avgData[row][col];
						}
						tenMinAvgData[col] = Math.round(((tenMinAvgData[col]/(double)avgCount)*1000))/1000;
					}
				}
				if (counted && avgCount == 0) {
					tenMinAvgData[2] = GMT;
					averagesReadyToPrint = true;
				}
				if(parent.getDebug()) {
					System.out.println(mySysTitle + "Avg count: " + avgCount);
					System.out.println(mySysTitle + "Avg data: " + Arrays.toString(tenMinAvgData));
				}
			}
			//avg end
			if (averagesReadyToPrint) {
				PrintWriter TenMinAverage = new PrintWriter(new FileWriter(mySysTitle + "_tenminaveragewindturbine.csv",true));
				TenMinAverage.println(Arrays.toString(tenMinAvgData));
				TenMinAverage.close();

				PrintWriter TenMinAverageMonthly = new PrintWriter(new FileWriter(mySysTitle + "_tenminaverage_ss" + now("yyyy_MM") + ".csv", true));
				File dataFile3 = new File(mySysTitle + "_tenminaverage_ss" + now("yyyy_MM") + ".csv");
				if (dataFile3.length() > 0L) {
					TenMinAverageMonthly.println(CurrentDate + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
				}
				else {
					TenMinAverageMonthly.println("Date, Power(watts), RPM, Wind(meters/sec), Total Energy(Watt-Hrs)");
					TenMinAverageMonthly.println(CurrentDate + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
				}
				TenMinAverageMonthly.close();

				sendToDBOptError = sendTo10minLocalDatabase();
				if (((sendToDBOptError == -1) || (!parent.getDBURL().equals("none"))) && (!parent.getMySQLURL().equals("none"))) {
					Connection connection;
					try {
						Class.forName("org.gjt.mm.mysql.Driver");
						String dbURL = parent.getMySQLURL();
						String username = parent.getMySQLUser();
						String password = parent.getMySQLPass();
						connection = DriverManager.getConnection(dbURL, username, password);
					}
					catch (ClassNotFoundException e) {
						parent.errorLog(mySysTitle + "Class Error (10mDB): " + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Database driver not found.");
						return null;
					}
					catch (SQLException e) {
						parent.errorLog(mySysTitle + "SQL Error (10minDB): " + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Error opening the local db connection: " + e.getMessage());
						return null;
					}
					try {
						String myQry = "INSERT into windturbine10avg( \tpower       ,\tvolts,\twindspeed,\ttotalpower,\trpm,\tcurrenttime) VALUES (?,?,?,?,?,?) ";
						PreparedStatement ps = connection.prepareStatement(myQry);
						ps.setDouble(1, tenMinAvgData[13]);
						ps.setDouble(2, tenMinAvgData[6]);
						ps.setDouble(3, tenMinAvgData[20]);
						ps.setDouble(4, tenMinAvgData[4]);
						ps.setDouble(5, tenMinAvgData[19]);
						Timestamp sqlTimestamp = new Timestamp((long) ((tenMinAvgData[2] + (parent.getGMTOffset()) * 3600.0D) * 1000L));
						ps.setTimestamp(6, sqlTimestamp);

						System.out.println(mySysTitle + "Attempting to send to backup MySql Database (10minAvg)...");
						ps.executeUpdate();
					}
					catch (SQLException e) {
						parent.errorLog(mySysTitle + "Error sending to backup MySQL Database (10minAvg): " + e.getMessage());
						System.out.println(mySysTitle + "Error sending to backup MySQL Database (10minAvg): " + e.getMessage());
					}
					try {
						connection.close();
					}
					catch (SQLException e) {
						parent.errorLog(mySysTitle + "Error closing the db connection(10MinAvg): " + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Error closing the db connection(10MinAvg): " + e.getMessage());
					}
				}
				averagesReadyToPrint = false;
			}
		}
		catch (Exception e) {
			parent.errorLog(mySysTitle + "Unknwon Error (10minAVG): "  + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "Unknown error:");
			e.printStackTrace();
		}
		return theData;
	}
	/**
	 * Method to call from timer when turbine data should be read. Method creates and writes to necessary data files.
	 */
	public void timerrun () {
		int arraysize = maxAvgCount;
		String cpowerstring = "";
		String cRPMstring = "";
		String ctimestring = "";
		double avgpower = 0.0D;
		double KWatts = 0.0D;
		double avgRPM = 0.0D;
		double avgWind = 0.0D;

		double[] values = getskzcmd();
		if (values[0] != 0.0d) {
			wind = values[20];
			power = values[13];
			Watts = values[4];
			RPM = values[19];
			ts = (int)values[33];
			ss = (int)values[35];
			gs = (int)values[34];
			volts = values[6];
			Wind = values[20];
			GMT = values[2];
			KWatts = Watts / 1000.0D;
			totEnergy = (values[4] / 1000.0D);
			volts = values[6];
		}
		try {
			if (tenMinAvgData[15] != 0.0D) {
				avgpower = tenMinAvgData[13];
				avgRPM = tenMinAvgData[19];
				avgWind = tenMinAvgData[20];
			}
			for (int i = 0; i <= arraysize - 1; i++) {
				cpowerstring = cpowerstring + "," + avgData[i][13];
				cRPMstring = cRPMstring + "," + avgData[i][19];
				ctimestring = ctimestring + "," + Double.toString(i);
			}
			if (values[0] != 0.0) {
				sendToDBError = sendToOpenEIDataBase();
				if (!parent.getDBURL().equals("none")) {
					sendToDBOptError = sendToLocalDataBase(parent.getDBURL());
				}
				if (((parent.getDBURL().equals("none")) || (sendToDBOptError == -1)) && (!parent.getMySQLURL().equals("none"))) {
					sendTo30sMysqlDatabase();
				}
				System.out.println(now("HH:mm dd MM yyyy") + "** Current Readings For: " + mySysTitle + "**");
				System.out.println("Status[TSG]:" + String.format("%04d",ts) + "," + String.format("%04d",ss) + "," + String.format("%04d",gs) + ", power:" + power + ", RPM:" + RPM + ", Wind:" + wind + ", " + String.format("%s %.2f %s", new Object[] { "Kwatt-Hrs:", Double.valueOf(KWatts), ""}));
				System.out.println(now("HH:mm dd MM yyyy") + "** 10 min Averages For: " + mySysTitle + "***");
				System.out.println(String.format("%s %.2f %s", new Object[] { "Avg_power:", avgpower, ", " }) + String.format("%s %.2f %s", new Object[] { "Avg_RPM:", avgRPM, ", " }) + String.format("%s %.2f", new Object[] { "Avg_Wind:", avgWind }));
				System.out.println();

				PrintWriter TurbineCurrent = new PrintWriter(new FileWriter(mySysTitle + "_windturbinecurrent.txt"));
				TurbineCurrent.println("   *** " + mySysTitle + " Current Readings ***   " + "\n");
				TurbineCurrent.println("Last update: " + CurrentDate);
				TurbineCurrent.println("Status - Turbine:" + String.format("%04d",ts) + ", System:" + String.format("%04d",ss) + ", Grid:" + String.format("%04d",gs) + "\n");
				TurbineCurrent.println("power:         " + power + " Watts");
				TurbineCurrent.println("Turbine Speed: " + RPM + " RPM");
				TurbineCurrent.println("Wind Speed:    " + wind + " m/s" + "," + NumberFormat.getInstance().format(wind * 2.2369363D) + "mph");
				TurbineCurrent.print("Daily Energy:   ");
				TurbineCurrent.format("%s%.2f%s%n", new Object[] { " ", myDailyTotal, " KWatt-Hrs" });
				TurbineCurrent.print("Total Energy:   ");
				if (values[0] == 103853) {
					TurbineCurrent.format("%s%.2f%s%n%n", new Object[] { " ", KWatts, " KWatt-Hrs (from 8/2/08)" });
				}
				else {
					TurbineCurrent.format("%s%.2f%s%n%n", new Object[] { " ", KWatts, " KWatt-Hrs" });
				}
				TurbineCurrent.println("            *** 10 min Averages ***   \n");
				TurbineCurrent.format("%s %.2f%s%n", new Object[] { "Avg power:     ", avgpower, " Watts" });
				TurbineCurrent.format("%s %.2f%s%n", new Object[] { "Turbine Speed: ", avgRPM, " RPM" });
				TurbineCurrent.format("%s %.2f %s %.2f %s %n", new Object[] { "Wind Speed:    ", avgWind, " m/s", (avgWind * 2.2369363D), "mph" });
				TurbineCurrent.println("\n* windspeed is for reference only");
				TurbineCurrent.close();

				PrintWriter ChartOut = new PrintWriter(new FileWriter(mySysTitle + "_chart.html"));

				ChartOut.println("<HTML><HEAD><TITLE>" + mySysTitle + " Wind Power Generation </TITLE> <meta http-equiv=" + '"' + "refresh" + '"' + " content=" + '"' + "30" + '"' + "></HEAD>");
				ChartOut.println("<BODY bgcolor = \"gray\">");
				ChartOut.println("<a href=\"http://www.inl.gov\">Idaho National Laboratory</a><BR>");
				ChartOut.println("<APPLET CODE=\"Line2D.class\" WIDTH=800 HEIGHT=600>");
				ChartOut.println("<PARAM name=\"title\" value=\"Wind Turbine Generation\">");
				ChartOut.println("<PARAM name=\"show_small_squares\" value=\"6\">");
				ChartOut.println("<PARAM name=\"show_legend_on_right\">");
				ChartOut.println("<PARAM name=\"legend_border_off\">");
				ChartOut.println("<PARAM name=\"show_percents_on_legend\">");
				ChartOut.println("<PARAM name=\"back_grid_color\" value=\"0,100,200\">");
				ChartOut.println("<PARAM name=\"Y_axis_description\" value=\"Turbine Power/RPM\">");
				ChartOut.println("<PARAM name=\"X_axis_description\" value=\"Time\">");
				ChartOut.println("<PARAM name=\"variation_series\" value=\"" + ctimestring + '"' + ">");
				ChartOut.println("<PARAM name=\"data_set_1\" value=\"" + cpowerstring + '"' + ">");
				ChartOut.println("<PARAM name=\"description_1\" value=\"Power\">");
				ChartOut.println("<PARAM name=\"data_set_2\" value=\"" + cRPMstring + '"' + ">");
				ChartOut.println("<PARAM name=\"description_2\" value=\"RPM\">");
				ChartOut.println("</APPLET>");
				ChartOut.println("<BR><input type=\"button\" value=\"Close this window\" onclick=\"self.close()\">");
				ChartOut.println("</BODY></HTML>");
				ChartOut.close();

				PrintWriter TurbineMostCurrent = new PrintWriter(new FileWriter(mySysTitle + "_mostcurrentwindturbine.csv"));

				TurbineMostCurrent.println(Arrays.toString(values));
				TurbineMostCurrent.close();
			}
			else {
				System.out.println(mySysTitle + "error: no data from turbine: " + mySysTitle + " ID: " + mySysID);
			}
		}
		catch (IOException e) {
			parent.errorLog(mySysTitle + "IO Error (timer): "  + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + " " + mySysTitle + "IOExcepton:");
			e.printStackTrace();
		}
	}
	/**
	 * @param dateFormat Format for the date.
	 * @return Current date formatted using date parameters.
	 * Method for requesting portions of the date, or a formatted date.
	 */
	public String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
}