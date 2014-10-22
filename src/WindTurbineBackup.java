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


public class WindTurbineBackup {
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
	String[] inData = { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
	String[][] avgData = new String[20][40];
	double[] tenMinAvgData = { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D };
	double myDailyTotal = 0.0D;
	String mySysTitle;
	String mySysID;
	String mySerialNum;
	String mySysName;
	String myApiKey;
	double myPowerOffset;
	double power = 0.0D;
	double wind = 0.0D;
	double speed = 0.0D;
	String ts = "0000";
	String ss = "0000";
	String gs = "0000";
	double dayEnergy = 35.0D;
	double totEnergy = 10000.0D;
	String volts = "250";
	int sendToDBError = 1;
	int sendToDBOptError = 1;
	boolean task2Suspend = false;
	windinterface2_openei parent;
	//TODO: Convert indata to only need doubles?
	public WindTurbineBackup(windinterface2_openei wioei, String SystemTitle, String SystemName, String SystemID, String SerialNum, String APIKey, double PowerOffset) {
		parent = wioei;
		mySysTitle = SystemTitle;
		mySysID = SystemID;
		mySerialNum = SerialNum;
		mySysName = SystemName;
		myApiKey = APIKey;
		myPowerOffset = PowerOffset;
		myDailyTotal = readDailyTot();
		//TODO: Make this loop not needed, convert avgdata to doubles?
		for (int j = 0; j < 20; j++) {
			for (int k = 0; k < 40; k++) {
				avgData[j][k] = "0";
			}
		}
		System.out.println("Loaded Wind Turbine: " + SystemTitle);
	}
	//TODO: Figure out why we need a txt file. csv, and web page...
	public void doWindInterface() {
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

			int arraysize = maxAvgCount;
			String power = "0";
			String Watts = "0";
			String RPM = "0";
			String Wind = "0";
			String TurbineStatus = "0";
			String GridStatus = "0";
			String SystemStatus = "0";
			String myTime = "0";
			String cpowerstring = "";
			String cRPMstring = "";
			String ctimestring = "";
			double avgpower = 0.0D;
			double KWatts = 0.0D;
			double avgRPM = 0.0D;
			double avgWind = 0.0D;
			double dailyTotal = 0.0D;
			double[] aa = { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D };
			int i = 0;

			runskzcmd();

			aa = tenMinAvgData;
			String[] d = inData;

			int numberOfd = Array.getLength(d);
			if (aa[15] != 0.0D) {
				avgpower = aa[13];
				avgRPM = aa[19];
				avgWind = aa[20];
				KWatts = aa[4];
			}
			if (numberOfd >= 39) {
				power = d[13];
				volts = d[6];
				Watts = d[4];
				RPM = d[19];
				Wind = d[20];
				TurbineStatus = d[33];
				GridStatus = d[34];
				SystemStatus = d[35];
				myTime = d[3];
				dailyTotal = Double.parseDouble(d[5]) / 1000.0D;
				KWatts = Double.parseDouble(Watts) / 1000.0D;
			}
			for (i = 0; i <= arraysize - 1; i++) {
				cpowerstring = cpowerstring + "," + avgData[i][13];
				cRPMstring = cRPMstring + "," + avgData[i][19];
				ctimestring = ctimestring + "," + Double.toString(i);
			}
			if (!d[0].equals("0")) {
				String openeiTurbineID = mySerialNum.replace("-", "").substring(2);


				String openEIurl = "http://en.openei.org/services/api/2/wfs/w/" + openeiTurbineID;
				sendToDBError = sendToOpenEIDataBase(openEIurl, inData);
				if (!parent.getDBURL().equals("none")) {
					sendToDBOptError = sendToLocalDataBase(parent.getDBURL(), inData);
				}
				if (((parent.getDBURL().equals("none")) || (sendToDBOptError == -1)) && (!parent.getMySQLURL().equals("none"))) {
					sendTo30sMysqlDatabase(inData);
				}
				System.out.println(now("HH:mm dd MM yyyy") + "************************ Current Readings ************************");

				System.out.println("Status[TSG]:" + TurbineStatus + "," + SystemStatus + "," + GridStatus + ", power:" + power + ", RPM:" + RPM + ", Wind:" + Wind + ", " + String.format("%s %.2f %s", new Object[] { "Kwatt-Hrs:", Double.valueOf(KWatts), ""}));

				System.out.println(now("HH:mm dd MM yyyy") + "************************* 10 min Averages ************************");
				System.out.println(String.format("%s %.2f %s", new Object[] { "Avg_power:", avgpower, ", " }) + String.format("%s %.2f %s", new Object[] { "Avg_RPM:", avgRPM, ", " }) + String.format("%s %.2f", new Object[] { "Avg_Wind:", avgWind }));

				outStream.println("   *** " + mySysTitle + " Current Readings ***   " + "\n");
				outStream.println("Last update: " + myTime);
				outStream.println("Status - Turbine:" + TurbineStatus + ", System:" + SystemStatus + ", Grid:" + GridStatus + "\n");
				outStream.println("power:         " + power + " Watts");

				outStream.println("Turbine Speed: " + RPM + " RPM");
				outStream.println("Wind Speed:    " + Wind + " m/s" + "," + NumberFormat.getInstance().format(Double.parseDouble(Wind) * 2.2369363D) + "mph");
				outStream.print("Daily Energy:   ");
				outStream.format("%s%.2f%s%n", new Object[] { " ", dailyTotal, " KWatt-Hrs" });
				outStream.print("Total Energy:   ");
				if (d[0].equals("103853")) {
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

				outStream3.println(Arrays.toString(d));


				outStream.close();
				outStream2.close();
				outStream3.close();
			}
			else {
				System.out.println("error: no data from turbine");
			}
		}
		catch (IOException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IOExcepton:");
			e.printStackTrace();
		}
	}

	public void runskzcmd() {
		try {
			double myTime = 0.0D;
			double pwrTot = 0.0D;
			int i = 0;int numberOfChar = 0;int j = 0;int k = 0;
			String dataOutFileName = "ss" + now("yyyy_MM") + ".csv";
			String dataOutFileName3 = "tenminaverage_ss" + now("yyyy_MM") + ".csv";
			String dataOutFileName2 = "tenminaveragewindturbine.csv";

			FileWriter outFileWriterTest = new FileWriter(dataOutFileName, true);

			PrintWriter outStreamTest = new PrintWriter(outFileWriterTest);

			String OS = System.getProperty("os.name");
			String execPath = parent.getPath() + "skzcmd.exe -z +" + mySysID + " dstat 1 0";
			if (!OS.startsWith("Windows")) {
				execPath = "./" + parent.getPath() + "s2zcmd -z +" + mySysID + " dstat 1 0";
			}
			Process p = Runtime.getRuntime().exec(execPath);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				String[] d = line.split(",");
				if (d.length >= 2) {
					inData[0] = d[0].replaceAll("\\D", "");
					String[] tempd1 = d[1].split(" ");
					inData[1] = tempd1[0].replaceAll("\\D", "");
					inData[2] = tempd1[1];
					inData[4] = d[2];
					int ii = 0;
					for (ii = 3; ii < d.length - 1; ii++) {
						inData[(ii + 3)] = d[ii];
					}
					numberOfChar = Array.getLength(inData);

					myTime = Double.parseDouble(inData[2]);

					pwrTot = Double.parseDouble(inData[4]);

					inData[4] = Double.toString(pwrTot + myPowerOffset);

					String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:z").format(Double.valueOf((myTime + (parent.getGMTOffset()) * 3600.0D) * 1000.0D));

					inData[3] = date;

					int tempHrs = Integer.parseInt(now("HH"));
					int tempMin = Integer.parseInt(now("mm"));
					if ((tempHrs == 0) && (tempMin == 0)) {
						myDailyTotal = 0.0D;
					}
					myDailyTotal += Double.parseDouble(inData[13]) * 0.0083333D;
					inData[5] = Double.toString(myDailyTotal);

					File dataFile = new File(dataOutFileName);
					if (dataFile.length() > 0L) {
						outStreamTest.println(Arrays.toString(inData));
					}
					else {
						outStreamTest.println("Turbine ID,SW Version,Time(sec),Time(MDY:HMS),watt-hours,DailyTot,Voltage In,Voltage DC Bus,Voltage L1,Voltage L2,voltage rise,min v from rpm,Current out,Power out,Power reg,Power max,Line Frequency,Inverter Frequency,Line Resistance,RPM,Windspeed (ref meters/sec),TargetTSR,Ramp RPM,Boost pulswidth,Max BPW,current amplitude, T1,T2,T3,Event count,Last event code,Event status,Event value,Turbine status,Grid status,System status,Slave Status,Access Status,Timer,");
						outStreamTest.println(Arrays.toString(inData));
					}
				}
			}
			input.close();

			outStreamTest.close();
			if ((avgCount < maxAvgCount) && (numberOfChar >= 39)) {
				for (i = 0; i < numberOfChar; i++) {
					avgData[avgCount][i] = inData[i];
					if (i == numberOfChar - 1) {
						avgCount += 1;
					}
					if (avgCount >= maxAvgCount) {
						avgCount = 0;
						for (j = 0; j < maxAvgCount; j++) {
							for (k = 0; k < numberOfChar - 1; k++) {
								if (k == 3) {
									tenMinAvgData[3] = 0.0D;
									k = 4;
								}
								double tempDouble = 0.0D;
								try {
									tempDouble = Double.parseDouble(avgData[j][k]);
								}
								catch (NumberFormatException localNumberFormatException) {}
								if (j == 0) {
									tenMinAvgData[k] = tempDouble;
								}
								else {
									tenMinAvgData[k] += tempDouble;
								}
							}
						}
						for (j = 0; j < numberOfChar - 1; j++) {
							tenMinAvgData[j] /= maxAvgCount;
						}
						tenMinAvgData[2] = Double.parseDouble(inData[2]);
						averagesReadyToPrint = true;
					}
				}
			}
			if (averagesReadyToPrint) {
				FileWriter outFileWriterTest2 = new FileWriter(dataOutFileName2);
				PrintWriter outStreamTest2 = new PrintWriter(outFileWriterTest2);

				outStreamTest2.println(Arrays.toString(tenMinAvgData));
				outStreamTest2.close();

				FileWriter outFileWriterTest3 = new FileWriter(dataOutFileName3, true);
				PrintWriter outStreamTest3 = new PrintWriter(outFileWriterTest3);

				File dataFile3 = new File(dataOutFileName3);
				if (dataFile3.length() > 0L) {
					outStreamTest3.println(inData[3] + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
				}
				else {
					outStreamTest3.println("Date, Power(watts), RPM, Wind(meters/sec), Total Energy(Watt-Hrs)");
					outStreamTest3.println(inData[3] + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
				}
				outStreamTest3.close();



				sendToDBOptError = sendTo10minLocalDatabase();
				if (((sendToDBOptError == -1) || (parent.getDBURL().equals("none"))) && (!parent.getMySQLURL().equals("none"))) {
					Connection connection;
					try {
						Class.forName("org.gjt.mm.mysql.Driver");
						String dbURL = parent.getMySQLURL();
						String username = parent.getMySQLUser();
						String password = parent.getMySQLPass();
						connection = DriverManager.getConnection(dbURL, username, password);
					}
					catch (ClassNotFoundException e) {
						parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Database driver not found.");
						return;
					}
					catch (SQLException e) {
						parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Error opening the local db connection: " + e.getMessage());
						return;
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
						parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Error executing the SQL statement: " + e.getMessage());
						System.out.println("Error sending to backup MySQL Database (10minAvg)");
					}
					try {
						connection.close();
					}
					catch (SQLException e) {
						parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
						System.out.println(now("HH:mm dd MM yyyy") + "Error closing the db connection: " + e.getMessage());
					}
				}
				averagesReadyToPrint = false;
			}
		}
		catch (Exception e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Unknown error:");
			e.printStackTrace();
		}
	}

	private int sendToOpenEIDataBase(String baseURL, String[] inData) {
		int didWork = 0;
		String[] d = inData;
		String power = d[13];
		String volts = d[6];
		String Watts = d[4];
		String RPM = d[19];
		String Wind = d[20];
		String TurbineStatus = d[33];
		String GridStatus = d[34];
		String SystemStatus = d[35];
		String[] myTime = d[3].split(" ");

		String[] myDate = myTime[0].split("/");

		String[] myTimeHMS = myTime[1].split(":");

		String dailyTotal = d[5];
		String GMT = d[2];
		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
		try {
			String keyString = "turbineID,GMT time,Day & time,PowerW, DailyKW, TotalKW, RPM, Wind, Volts, Tstat, Sstat, Gstat";
			keyString = keyString.replaceAll(" ", "");

			String openeiTurbineID = mySerialNum.replace("-", "").substring(2);
			String skyDataString = openeiTurbineID + "," + GMT + "," + tempString + "," + power + "," + dailyTotal + "," + Watts + "," + RPM + "," + Wind + "," + volts + "," + TurbineStatus + "," + SystemStatus + "," + GridStatus;

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] keys = keyString.split(",");
			String[] data = skyDataString.split(",");

			int i = 0;
			while (i < keys.length) {
				dataMap.put(keys[i], data[i]);
				i++;
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = baseURL + "?api_key=" + myApiKey + "&" + "json_data=" + jsonString;

			URL url = new URL(urlString);

			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

			String check = null;
			String s;
			while ((s = br.readLine()) != null) {
				if (s != null) {
					check = s;
				}
			}
			if (check.substring(0, 6).equals("{\"Item")) {
				didWork = 1;
				System.out.println("Data was Successfully sent to OpenEI");
			}
			else {
				didWork = -1;
				System.out.println("OpenEI Error! " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error executing the  statement: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IO exception: " + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}
	private int sendToLocalDataBase(String baseURL, String[] inData) {
		int didWork = 0;
		String[] d = inData;
		String power = d[13];
		String volts = d[6];
		String Watts = d[4];
		String RPM = d[19];
		String Wind = d[20];
		String TurbineStatus = d[33];
		String GridStatus = d[34];
		String SystemStatus = d[35];
		String[] myTime = d[3].split(" ");

		String[] myDate = myTime[0].split("/");

		String[] myTimeHMS = myTime[1].split(":");

		String dailyTotal = d[5];
		String GMT = d[2];

		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + "%20" + hour + ":" + min + ":" + sec;
		try {
			String keyString = "turbineID,GMT time,Day & time,PowerW, DailyKW, TotalKW, RPM, Wind, Volts, Tstat, Sstat, Gstat";
			keyString = keyString.replaceAll(" ", "");

			String openeiTurbineID = mySerialNum.replace("-", "").substring(2);
			String skyDataString = openeiTurbineID + "," + GMT + "," + tempString + "," + power + "," + dailyTotal + "," + Watts + "," + RPM + "," + Wind + "," + volts + "," + TurbineStatus + "," + SystemStatus + "," + GridStatus;

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] keys = keyString.split(",");
			String[] data = skyDataString.split(",");

			int i = 0;
			while (i < keys.length) {
				dataMap.put(keys[i], data[i]);
				i++;
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = baseURL + "?systemname=" + mySysName + "&" + "json_data=" + jsonString;


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
				System.out.println("Data was Successfully sent to Local 30s Database (HTTP)");
				System.out.println("Success");
			}
			else {
				didWork = -1;
				System.out.println("Error sending to Local 30s Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error executing the  statement: " + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IO exception: " + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	private int sendTo30sMysqlDatabase(String[] inData) {
		int error = 1;

		String[] d = inData;

		String power = d[13];
		String volts = d[6];
		String Watts = d[4];
		String RPM = d[19];
		String Wind = d[20];

		double tempTime = 0.0D;
		try {
			tempTime = Double.parseDouble(d[2]);
		}
		catch (Exception e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
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
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Database driver not found.");
			return -1;
		}
		catch (SQLException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Local Error opening the db connection: " + 
					e.getMessage());
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
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error executing the SQL statement: " + e.getMessage());
			error = -1;
		}
		try {
			connection.close();
		}
		catch (SQLException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "Error closing the db connection: " + e.getMessage());
		}
		if (error == 1) {
			System.out.println("Data was Successfully sent to Backup Mysql Database (30s)");
		}
		return error;
	}

	private int sendTo10minLocalDatabase() {
		if (parent.getDBURL().equals("none")) {
			System.out.println(now("HH:mm dd MM yyyy") + "Skipped 10min avg DB send because no DBURL set");
			return -1;
		}
		int didWork = 0;
		String power = String.valueOf(tenMinAvgData[13]);
		String volts = String.valueOf(tenMinAvgData[6]);
		String Watts = String.valueOf(tenMinAvgData[4]);
		String RPM = String.valueOf(tenMinAvgData[19]);
		String Wind = String.valueOf(tenMinAvgData[20]);
		Timestamp sqlTimestamp = new Timestamp((long) ((tenMinAvgData[2] + (parent.getGMTOffset()) * 3600.0D) * 1000L));
		String CurrentTime = sqlTimestamp.toString();
		try {
			String keyString = "Day & time,PowerW, TotalKW, RPM, Wind, Volts";
			keyString = keyString.replaceAll(" ", "");

			String skyDataString = CurrentTime + "," + power + "," + Watts + "," + RPM + "," + Wind + "," + volts;

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] keys = keyString.split(",");
			String[] data = skyDataString.split(",");

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
				System.out.println("Success");
			}
			else {
				didWork = -1;
				System.out.println("Error sending to Local 10minAvg Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

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
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			System.out.println(now("HH:mm dd MM yyyy") + "IOExcepton:");
			e.printStackTrace();
		}
		return dailyTot;
	}
	public String[] getskzcmd() {
		String[] theData = { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
		try {
			double myTime = 0.0D;
			double pwrTot = 0.0D;
			String OS = System.getProperty("os.name");
			String execPath = parent.getPath() + "skzcmd.exe -z +" + mySysID + " dstat 1 0";
			if (!OS.startsWith("Windows")) {
				execPath = "./" + parent.getPath() + "s2zcmd -z +" + mySysID + " dstat 1 0";
			}
			Process p = Runtime.getRuntime().exec(execPath);
			BufferedReader input2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input2.readLine()) != null) {
				String[] d = line.split(",");
				if (d.length >= 2) {
					theData[0] = d[0].replaceAll("\\D", "");
					String[] tempd1 = d[1].split(" ");
					theData[1] = tempd1[0].replaceAll("\\D", "");
					theData[2] = tempd1[1];
					theData[4] = d[2];
					int ii = 0;
					for (ii = 3; ii < d.length - 1; ii++) {
						theData[(ii + 3)] = d[ii];
					}
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
					theData[5] = Double.toString(readDailyTot());
				}
			}
			input2.close();
		}
		catch (Exception e) {
			parent.errorLog(now("HH:mm dd MM yyyy") + e.getMessage());
			e.printStackTrace();
		}
		return theData;
	}
	public void timerrun () {
		if (!task2Suspend) {
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
		}
		doWindInterface();
	}
	public String now(String dateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}
}
