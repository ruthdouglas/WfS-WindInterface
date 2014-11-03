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
 * @author Ian Mason
 * Class created to hold properties of a Winnd Turbine. This class holds all things specific to an individual turbine, and
 * methods to help perform tasks specific to that turbine.
 */
public class WindTurbine {
	int avgCount = 0; //Used when printing avgs
	int maxAvgCount = 20;  //Used when printing avgs
	boolean averagesReadyToPrint = false;  //Used when printing avgs
	double[][] avgData = new double[20][40];
	double[] tenMinAvgData = new double[40];
	double myDailyTotal = 0.0D;
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
	String ts = "0000";
	String ss = "0000";
	String gs = "0000";
	double dayEnergy;
	double totEnergy;
	String volts;
	//End Vards from turbine...
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
	 */
	public WindTurbine(windinterface2_openei wioei, String SystemTitle, String SystemName, String SystemID, String SerialNum, String APIKey, double PowerOffset) {
		parent = wioei;
		mySysTitle = SystemTitle;
		mySysID = SystemID;
		mySerialNum = SerialNum;
		mySysName = SystemName;
		myApiKey = APIKey;
		myPowerOffset = PowerOffset;
		myDailyTotal = readDailyTot();
		System.out.println("Loaded Wind Turbine: " + SystemTitle);
	}
	/**
	 * @param inData Data to be sent to database.
	 * @return Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 */
	private int sendToOpenEIDataBase(String[] inData) {
		int didWork = 0;
		String power = inData[13];
		String volts = inData[6];
		String Watts = inData[4];
		String RPM = inData[19];
		String Wind = inData[20];
		//double GMTTime = Double.parseDouble(inData[2]);
		String TurbineStatus = inData[33];
		String GridStatus = inData[34];
		String SystemStatus = inData[35];
		
		String[] myTime = inData[3].split(" ");
		String[] myDate = myTime[0].split("/");
		String[] myTimeHMS = myTime[1].split(":");

		String dailyTotal = inData[5];
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

			String[] data = {mySerialNum,tempString,power,dailyTotal,Watts,RPM,Wind,volts,TurbineStatus,SystemStatus,GridStatus};
			if (parent.getDebug()) System.out.println("OpenEI Data: " + Arrays.toString(data));
			int i = 0;
			while (i < keys.length) {
				dataMap.put(keys[i], data[i]);
				i++;
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = "http://en.openei.org/services/api/2/wfs/w/" + mySerialNum + "?api_key=" + myApiKey + "&" + "json_data=" + jsonString;
			if (parent.getDebug()) System.out.println("OpenEI URL: " + urlString);
			URL url = new URL(urlString);

			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				if (s != null) {
					check = s;
				}
			}
			if (check != null) {
				if (parent.getDebug()) System.out.println("OpenEI return: " + check);
				if (check.substring(0, 6).equals("{\"Item")) {
					didWork = 1;
					System.out.println("Data was Successfully sent to OpenEI");
				}
				else {
					didWork = -1;
					System.out.println("OpenEI Error! " + s);
				}
			}
			else {
				System.out.println("Unable to connect to OpenEI!" + s );
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog("URL ERROR (OpenEI): " +  e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error executing the  statement: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog("IOException (OpenEI): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IO exception: " + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}
	/**
	 * @param baseURL Local database URL.
	 * @param inData Data to be sent to database.
	 * @return  Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 */
	private int sendToLocalDataBase(String baseURL, String[] inData) {
		int didWork = 0;
		
		String power = inData[13];
		String volts = inData[6];
		String Watts = inData[4];
		String RPM = inData[19];
		String Wind = inData[20];
		String TurbineStatus = inData[33];
		String GridStatus = inData[34];
		String SystemStatus = inData[35];
		
		String[] myTime = inData[3].split(" ");
		String[] myDate = myTime[0].split("/");
		String[] myTimeHMS = myTime[1].split(":");

		String dailyTotal = inData[5];
		String GMT = inData[2];

		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + "%20" + hour + ":" + min + ":" + sec;
		try {
			String[] keys = {"turbineID","GMTtime","Day&time","PowerW","DailyKW","TotalKW","RPM","Wind","Volts","Tstat","Sstat","Gstat"};

			String[] data = {mySerialNum,GMT,tempString,power,dailyTotal,Watts,RPM,Wind,volts,TurbineStatus,SystemStatus,GridStatus};
			if (parent.getDebug()) System.out.println("LocalDB Data: " + Arrays.toString(data));
			Map<String, String> dataMap = new HashMap<String, String>();

			int i = 0;
			while (i < keys.length) {
				dataMap.put(keys[i], data[i]);
				i++;
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = baseURL + "?systemname=" + mySysName + "&" + "json_data=" + jsonString;

			if (parent.getDebug()) System.out.println("LocalDB URL: " + urlString);
			URL url = new URL(urlString);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				if (s != null) {
					check = s;
				}
			}
			if (parent.getDebug()) System.out.println("LocalDB return: " + check);
			if (check.equals("SUCCESS")) {
				didWork = 1;
				System.out.println("Data was Successfully sent to Local 30s Database (HTTP)");
			}
			else {
				didWork = -1;
				parent.errorLog("Error sending to Local 30s Database (HTTP) " + s);
				System.out.println("Error sending to Local 30s Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog("URL Error (LocalDB): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error executing the  statement: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog("IOException (localDB): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IO exception: " + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	//TODO: Add debug code
	/**
	 * @param inData Data to be sent to database.
	 * @return  Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 */
	private int sendTo30sMysqlDatabase(String[] inData) {
		int error = 1;

		String power = inData[13];
		String volts = inData[6];
		String Watts = inData[4];
		String RPM = inData[19];
		String Wind = inData[20];

		double tempTime = 0.0D;
		try {
			tempTime = Double.parseDouble(inData[2]);
		}
		catch (Exception e) {
			parent.errorLog("Unknown error (30secSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Exception error : " + e);
			tempTime = 0.0D;
		}
		Connection connection;
		try {
			Class.forName("org.gjt.mm.mysql.Driver");

			String dbURL = parent.getMySQLURL();
			String username = parent.getMySQLUser();
			String password = parent.getMySQLPass();
			connection = DriverManager.getConnection(dbURL, username, password);
		}
		catch (ClassNotFoundException e) {
			parent.errorLog("Class Error (30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Database driver not found.");
			return -1;
		}
		catch (SQLException e) {
			parent.errorLog("SQL Error on open (30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Local Error opening the db connection: " + e.getMessage());
			return -1;
		}
		try {
			String myQry = "INSERT into windturbine( \tpower       ,\tvolts,\twindspeed,\ttotalpower,\trpm,\tcurrenttime) VALUES (?,?,?,?,?,?) ";

			PreparedStatement ps = connection.prepareStatement(myQry);
			ps.setDouble(1, Double.parseDouble(power));
			ps.setDouble(2, Double.parseDouble(volts));
			ps.setDouble(3, Double.parseDouble(Wind));
			ps.setDouble(4, Double.parseDouble(Watts));
			ps.setDouble(5, Double.parseDouble(RPM));
			Timestamp sqlTimestamp = new Timestamp((long) ((tempTime + (parent.getGMTOffset()) * 3600.0D) * 1000L));
			ps.setTimestamp(6, sqlTimestamp);

			System.out.println("Trying Backup MySQL Database (30s)...");
			ps.executeUpdate();
		}
		catch (SQLException e) {
			parent.errorLog("SQL Error on send (30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error executing the SQL statement: " + e.getMessage());
			error = -1;
		}
		try {
			connection.close();
		}
		catch (SQLException e) {
			parent.errorLog("SQL Error on close(30sSQL): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error closing the db connection: " + e.getMessage());
		}
		if (error == 1) {
			System.out.println("Data was Successfully sent to Backup Mysql Database (30s)");
		}
		return error;
	}

	//TODO: Add debug code
	/**
	 * @return  Returns an int corresponding to if the data was sent successfully. 1 for success, 0 for connection error, -1 for OpenEI error.
	 */
	private int sendTo10minLocalDatabase() {
		if (parent.getDBURL().equals("none")) {
			System.out.println(now("HH:mm dd MM yyyy") + "Skipped 10min avg DB send because no DBURL set");
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

			int i = 0;
			while (i < keys.length) {
				dataMap.put(keys[i], data[i]);
				i++;
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = parent.getDBURL() + "?10minavg=yes&systemname=" + mySysName + "&" + "json_data=" + jsonString;

			URL url = new URL(urlString);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				if (s != null) {
					check = s;
				}
			}
			if (check.equals("SUCCESS")) {
				didWork = 1;
				System.out.println("Data was Successfully sent to Local 10minAvg Database (HTTP)");
			}
			else {
				didWork = -1;
				parent.errorLog("Error sending to Local 10minAvg Database (HTTP) " + s);
				System.out.println("Error sending to Local 10minAvg Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog("URL Error (10mDB): "  + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog("IO Error (10mSQL): "  + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	//TODO: Add debug code
	/**
	 * @return
	 */
	private double readDailyTot() {
		double dailyTot = 0.0D;
		try {
			String inFileName = "mostcurrentwindturbine.csv";
			FileReader inFileReader = new FileReader(inFileName);
			BufferedReader inStream = new BufferedReader(inFileReader);
			String inLine = null;
			String lastLine = null;
			while ((inLine = inStream.readLine()) != null) {
				lastLine = inLine;
			}
			if (lastLine != null) {
				String[] d = lastLine.split(",");
				if (d.length > 2) {
					dailyTot = Double.parseDouble(d[5]);
				}
			}
			inStream.close();
		}
		catch (IOException e) {
			parent.errorLog("IO Error (ReadDailyTot): " + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IOExcepton:");
			e.printStackTrace();
		}
		return dailyTot;
	}
	/**
	 * @return Array of Strings containing all turbine data.
	 */
	public String[] getskzcmd() {
		String[] theData = new String[40];
		Arrays.fill(theData, "0");
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
			

			PrintWriter MonthlyData = new PrintWriter(new FileWriter("ss" + now("yyyy_MM") + ".csv", true));
			while ((line = input.readLine()) != null) {
				if (parent.getDebug()) System.out.println("From skzcmd: " + line);
				String[] d = line.split(",");
				if (d.length >= 2) {
					if (parent.getDebug()) System.out.println("Raw data: " + Arrays.toString(d));
					theData[0] = d[0].replaceAll("\\D", "");
					String[] tempd1 = d[1].split(" ");
					theData[1] = tempd1[0].replaceAll("\\D", "");
					theData[2] = tempd1[1];
					theData[4] = d[2];

					for (int ii = 3; ii < d.length - 1; ii++) {
						theData[(ii + 3)] = d[ii];
					}
					numberOfChar = Array.getLength(theData);
					myTime = Double.parseDouble(theData[2]);

					pwrTot = Double.parseDouble(theData[4]);

					theData[4] = Double.toString(pwrTot + myPowerOffset);
					String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:z").format(Double.valueOf((myTime + (parent.getGMTOffset()) * 3600.0D) * 1000.0D));

					theData[3] = date;

					int tempHrs = Integer.parseInt(now("HH"));
					int tempMin = Integer.parseInt(now("mm"));
					if ((tempHrs == 0) && (tempMin == 0)) {
						myDailyTotal = 0.0D;
					}
					myDailyTotal += Double.parseDouble(theData[13]) * 0.0083333D;
					theData[5] = Double.toString(readDailyTot());

					File dataFile = new File("ss" + now("yyyy_MM") + ".csv");
					if (parent.getDebug()) System.out.println("Formatted data: " + Arrays.toString(theData));
					if (dataFile.length() > 0L) {
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
			if ((avgCount < maxAvgCount) && (numberOfChar >= 39)) {
				for (int i = 0; i < numberOfChar; i++) {
					if(i != 3 && i != 30 && i != 31) { avgData[avgCount][i] = Double.parseDouble(theData[i]); } //ignore the non-numerical values, they don't matter
					if (i == numberOfChar - 1) {
						avgCount += 1;
					}
					if (avgCount >= maxAvgCount) {  //When data has been read 20 times (10 min)
						avgCount = 0;
						for (int row = 0; row < maxAvgCount; row++) {
							for (int col = 0; col < numberOfChar - 1; col++) {
								double tempDouble = 0.0D;
								try {
									tempDouble = avgData[row][col];
								}
								catch (NumberFormatException localNumberFormatException) {}
								if (row == 0) {
									tenMinAvgData[col] = tempDouble;
								}
								else {
									tenMinAvgData[col] += tempDouble;
								}
							}
						}
						for (int j = 0; j < numberOfChar - 1; j++) {
							tenMinAvgData[j] /= maxAvgCount;
						}
						tenMinAvgData[2] = Double.parseDouble(theData[2]); //grab current epoch time
						averagesReadyToPrint = true;
					}
				}
			}
			//avg end
			if (averagesReadyToPrint) {
				PrintWriter TenMinAverage = new PrintWriter(new FileWriter("tenminaveragewindturbine.csv"));

				TenMinAverage.println(Arrays.toString(tenMinAvgData));
				TenMinAverage.close();

				PrintWriter TenMinAverageMonthly = new PrintWriter(new FileWriter("tenminaverage_ss" + now("yyyy_MM") + ".csv", true));

				File dataFile3 = new File("tenminaverage_ss" + now("yyyy_MM") + ".csv");
				if (dataFile3.length() > 0L) {
					TenMinAverageMonthly.println(theData[3] + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
				}
				else {
					TenMinAverageMonthly.println("Date, Power(watts), RPM, Wind(meters/sec), Total Energy(Watt-Hrs)");
					TenMinAverageMonthly.println(theData[3] + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
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
						parent.errorLog("Class Error (10mDB): " + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Database driver not found.");
						return null;
					}
					catch (SQLException e) {
						parent.errorLog("SQL Error (10minDB): " + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Error opening the local db connection: " + e.getMessage());
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


						System.out.println("Attempting to send to backup MySql Database (10minAvg)...");
						ps.executeUpdate();
					}
					catch (SQLException e) {
						parent.errorLog("Error sending to backup MySQL Database (10minAvg): " + e.getMessage());
						System.out.println("Error sending to backup MySQL Database (10minAvg): " + e.getMessage());
					}
					try {
						connection.close();
					}
					catch (SQLException e) {
						parent.errorLog("Error closing the db connection(10MinAvg): " + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Error closing the db connection(10MinAvg): " + e.getMessage());
					}
				}
				averagesReadyToPrint = false;
			}
		}
		catch (Exception e) {
			parent.errorLog("Unknwon Error (10minAVG): "  + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Unknown error:");
			e.printStackTrace();
		}
		return theData;
	}
	//TODO: Figure out why we need a txt file. csv, and web page...
	/**
	 * 
	 */
	public void timerrun () {
		int arraysize = maxAvgCount;
		//String power = "0";
		double Watts = 0.0d;
		double RPM = 0.0d;
		String myTime = "0";
		String cpowerstring = "";
		String cRPMstring = "";
		String ctimestring = "";
		double avgpower = 0.0D;
		double KWatts = 0.0D;
		double avgRPM = 0.0D;
		double avgWind = 0.0D;
		double dailyTotal = 0.0D;
		int i = 0;

		String[] values = getskzcmd();
			if (!values[0].equals("NullPointer Error")) {
				values[0] = values[0].replace("[", "");
				values[(values.length - 1)] = values[(values.length - 1)].replace("]", "");
				wind = Double.parseDouble(values[20]);
				power = Double.parseDouble(values[13]);
				speed = Double.parseDouble(values[19]);
				ts = values[33];
				ss = values[35];
				gs = values[34];
				dayEnergy = Double.parseDouble(values[5]) / 1000.0D;
				totEnergy = Double.parseDouble(values[4]) / 1000.0D;
				volts = values[6];
			}
			try {
				String outFileName = "windturbinecurrent.txt";
				String outFileName3 = "mostcurrentwindturbine.csv";
				String outFileName2 = "chart.html";

				FileWriter outFileWriter = new FileWriter(outFileName);
				FileWriter outFileWriter2 = new FileWriter(outFileName2);
				FileWriter outFileWriter3 = new FileWriter(outFileName3);

				PrintWriter outStream = new PrintWriter(outFileWriter);
				PrintWriter outStream2 = new PrintWriter(outFileWriter2);
				PrintWriter outStream3 = new PrintWriter(outFileWriter3);


				//runskzcmd();

				int numberOfd = Array.getLength(values);
				if (tenMinAvgData[15] != 0.0D) {
					avgpower = tenMinAvgData[13];
					avgRPM = tenMinAvgData[19];
					avgWind = tenMinAvgData[20];
					KWatts = tenMinAvgData[4];
				}
				if (numberOfd >= 39) {
					power = Double.parseDouble(values[13]);
					volts = values[6];
					Watts = Double.parseDouble(values[4]);
					RPM = Double.parseDouble(values[19]);
					wind = Double.parseDouble(values[20]);
					ts = values[33];
					gs = values[34];
					ss = values[35];
					myTime = values[3];
					dailyTotal = Double.parseDouble(values[5]) / 1000.0D;
					KWatts = Watts / 1000.0D;
				}
				for (i = 0; i <= arraysize - 1; i++) {
					cpowerstring = cpowerstring + "," + avgData[i][13];
					cRPMstring = cRPMstring + "," + avgData[i][19];
					ctimestring = ctimestring + "," + Double.toString(i);
				}
				if (!values[0].equals("0")) {
					sendToDBError = sendToOpenEIDataBase(values);
					if (!parent.getDBURL().equals("none")) {
						sendToDBOptError = sendToLocalDataBase(parent.getDBURL(), values);
					}
					if (((parent.getDBURL().equals("none")) || (sendToDBOptError == -1)) && (!parent.getMySQLURL().equals("none"))) {
						sendTo30sMysqlDatabase(values);
					}
					System.out.println(now("HH:mm dd MM yyyy") + "********************** Current Readings ************************");

					System.out.println("Status[TSG]:" + ts + "," + ss + "," + gs + ", power:" + power + ", RPM:" + RPM + ", Wind:" + wind + ", " + String.format("%s %.2f %s", new Object[] { "Kwatt-Hrs:", Double.valueOf(KWatts), ""}));

					System.out.println(now("HH:mm dd MM yyyy") + "*********************** 10 min Averages ************************");
					System.out.println(String.format("%s %.2f %s", new Object[] { "Avg_power:", avgpower, ", " }) + String.format("%s %.2f %s", new Object[] { "Avg_RPM:", avgRPM, ", " }) + String.format("%s %.2f", new Object[] { "Avg_Wind:", avgWind }));
					System.out.println();
					
					outStream.println("   *** " + mySysTitle + " Current Readings ***   " + "\n");
					outStream.println("Last update: " + myTime);
					outStream.println("Status - Turbine:" + ts + ", System:" + ss + ", Grid:" + gs + "\n");
					outStream.println("power:         " + power + " Watts");

					outStream.println("Turbine Speed: " + RPM + " RPM");
					outStream.println("Wind Speed:    " + wind + " m/s" + "," + NumberFormat.getInstance().format(wind * 2.2369363D) + "mph");
					outStream.print("Daily Energy:   ");
					outStream.format("%s%.2f%s%n", new Object[] { " ", dailyTotal, " KWatt-Hrs" });
					outStream.print("Total Energy:   ");
					if (values[0].equals("103853")) {
						outStream.format("%s%.2f%s%n%n", new Object[] { " ", KWatts, " KWatt-Hrs (from 8/2/08)" });
					}
					else {
						outStream.format("%s%.2f%s%n%n", new Object[] { " ", KWatts, " KWatt-Hrs" });
					}
					outStream.println("            *** 10 min Averages ***   \n");
					outStream.format("%s %.2f%s%n", new Object[] { "Avg power:     ", avgpower, " Watts" });

					outStream.format("%s %.2f%s%n", new Object[] { "Turbine Speed: ", avgRPM, " RPM" });
					outStream.format("%s %.2f %s %.2f %s %n", new Object[] { "Wind Speed:    ", avgWind, " m/s", (avgWind * 2.2369363D), "mph" });
					outStream.println("\n* windspeed is for reference only");


					outStream2.println("<HTML><HEAD><TITLE>" + mySysTitle + " Wind Power Generation </TITLE> <meta http-equiv=" + '"' + "refresh" + '"' + " content=" + '"' + "30" + '"' + "></HEAD>");
					outStream2.println("<BODY bgcolor = \"gray\">");
					outStream2.println("<a href=\"http://www.inl.gov\">Idaho National Laboratory</a><BR>");
					outStream2.println("<APPLET CODE=\"Line2D.class\" WIDTH=800 HEIGHT=600>");

					outStream2.println("<PARAM name=\"title\" value=\"Wind Turbine Generation\">");
					outStream2.println("<PARAM name=\"show_small_squares\" value=\"6\">");

					outStream2.println("<PARAM name=\"show_legend_on_right\">");
					outStream2.println("<PARAM name=\"legend_border_off\">");
					outStream2.println("<PARAM name=\"show_percents_on_legend\">");
					outStream2.println("<PARAM name=\"back_grid_color\" value=\"0,100,200\">");

					outStream2.println("<PARAM name=\"Y_axis_description\" value=\"Turbine Power/RPM\">");
					outStream2.println("<PARAM name=\"X_axis_description\" value=\"Time\">");

					outStream2.println("<PARAM name=\"variation_series\" value=\"" + ctimestring + '"' + ">");

					outStream2.println("<PARAM name=\"data_set_1\" value=\"" + cpowerstring + '"' + ">");
					outStream2.println("<PARAM name=\"description_1\" value=\"Power\">");

					outStream2.println("<PARAM name=\"data_set_2\" value=\"" + cRPMstring + '"' + ">");
					outStream2.println("<PARAM name=\"description_2\" value=\"RPM\">");

					outStream2.println("</APPLET>");

					outStream2.println("<BR><input type=\"button\" value=\"Close this window\" onclick=\"self.close()\">");
					outStream2.println("</BODY></HTML>");

					outStream3.println(Arrays.toString(values));


					outStream.close();
					outStream2.close();
					outStream3.close();
				}
				else {
					System.out.println("error: no data from turbine");
				}
			}
			catch (IOException e) {
				parent.errorLog("IO Error (timer): "  + e.getMessage());
				System.out.println(now("HH:mm dd MM yyyy") + "IOExcepton:");
				e.printStackTrace();
			}
	}
	/**
	 * @param dateFormat
	 * @return
	 */
	public String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
}
