import com.google.gson.Gson;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("serial")
public class windinterface2b_openei
extends HttpServlet
implements ActionListener
{
	static BufferedReader inputData;
	static String[] inData = { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
	static String[][] avgData = new String[20][40];
	static int sendToDBError = 1;
	static int sendToDBOptError = 1;
	static int avgCount = 0;
	static int maxAvgCount = 20;
	static double[] tenMinAvgData = { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D };
	static double myDailyTotal = 0.0D;
	static double[] tenMinAvgData2 = { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D };
	static boolean averagesReadyToPrint = false;
	static String mySysTitle;
	static String mySysID;
	static String mySerialNum;
	static String myDBURL;
	static String myMysqlURL;
	static String myMysqlUser;
	static String myMysqlPass;
	static String mySysName;
	static String myApiKey;
	static String myGMT_Offset;
	static String myPowerOffset;
	public static String turbineName = "Test";
	public static String turbineID = "10xxx";
	public static String myPath = "resources/";
	static double power = 0.0D;
	static double wind = 0.0D;
	static double speed = 0.0D;
	static double windConvert = 7.2D;
	static double speedConvert = 1.125D;
	static double powerConvert = 0.12D;
	static double voltsConvert = 0.03571428571428571D;
	static double dayEnergyConvert = 0.2D;
	static String ts = "0000";
	static String ss = "0000";
	static String gs = "0000";
	static double dayEnergy = 35.0D;
	static double totEnergy = 10000.0D;
	static String volts = "250";
	static String windowSystemName = "Turbine";
	static String windowTitle = "Skystream Wind Turbine";
	boolean moved = false;
	boolean displayFrame = true;
	boolean displayWindow = true;
	int coorX;
	int coorY;
	JButton button = new JButton("Exit", new ImageIcon("resources/Images/close.png"));
	JLabel label = new JLabel("Title Bar  ");
	JWindow window = new JWindow();
	Container con;
	JFrame frame = new JFrame();
	JPanel contentPanel = new JPanel();
	JButton jButton1 = new JButton("Save");
	JButton jButton2 = new JButton("Cancle", new ImageIcon("resources/Images/close.png"));
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel7 = new JLabel();
	JLabel jLabel8 = new JLabel();
	JLabel jLabel9 = new JLabel();
	JLabel jLabel10 = new JLabel();
	JLabel jLabel11 = new JLabel();
	JTextField jTextField1 = new JTextField();
	JTextField jTextField2 = new JTextField();
	JTextField jTextField3 = new JTextField();
	JTextField jTextField4 = new JTextField();
	JTextField jTextField5 = new JTextField();
	JTextField jTextField6 = new JTextField();
	JTextField jTextField7 = new JTextField();
	JTextField jTextField8 = new JTextField();
	JTextField jTextField9 = new JTextField();
	JTextField jTextField10 = new JTextField();
	JTextField jTextField11 = new JTextField();
	static boolean task2Suspend = false;

	public static String[] getPref()
	{
		String[] system_data = { "none", "none", "none", "none", "none", "none", "none", "none", "none", "0", "0" };
		try
		{
			File file = new File(myPath + "windinterfacepref.xml");
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();

			NodeList list = doc.getElementsByTagName("system");
			for (int i = 0; i < list.getLength(); i++)
			{
				Node node1 = list.item(i);
				if (node1.getNodeType() == 1)
				{
					Element element = (Element)node1;

					NodeList sysTitleNodeElementList = element.getElementsByTagName("sys_title");
					Element element1 = (Element)sysTitleNodeElementList.item(0);
					NodeList sysTitleNodeList = element1.getChildNodes();
					system_data[0] = sysTitleNodeList.item(0).getNodeValue();

					NodeList sysIDNodeElementList = element.getElementsByTagName("sys_id");
					Element element2 = (Element)sysIDNodeElementList.item(0);
					NodeList sysIDNodeList = element2.getChildNodes();
					system_data[1] = sysIDNodeList.item(0).getNodeValue();

					NodeList serNumNodeElementList = element.getElementsByTagName("serial_num");
					Element element3 = (Element)serNumNodeElementList.item(0);
					NodeList serNumNodeList = element3.getChildNodes();
					system_data[2] = serNumNodeList.item(0).getNodeValue();

					NodeList dbURLNodeElementList = element.getElementsByTagName("dbURL");
					Element element4 = (Element)dbURLNodeElementList.item(0);
					NodeList dbURLNodeList = element4.getChildNodes();
					system_data[3] = dbURLNodeList.item(0).getNodeValue();

					NodeList sysNameNodeElementList = element.getElementsByTagName("sys_name");
					Element element5 = (Element)sysNameNodeElementList.item(0);
					NodeList sysNameNodeList = element5.getChildNodes();
					system_data[4] = sysNameNodeList.item(0).getNodeValue();

					NodeList sysMysqlNodeElementList = element.getElementsByTagName("mysqlURL");
					Element element6 = (Element)sysMysqlNodeElementList.item(0);
					NodeList sysMysqlNodeList = element6.getChildNodes();
					system_data[5] = sysMysqlNodeList.item(0).getNodeValue();

					NodeList sysUserNodeElementList = element.getElementsByTagName("mysqlUser");
					Element element7 = (Element)sysUserNodeElementList.item(0);
					NodeList sysUserNodeList = element7.getChildNodes();
					system_data[6] = sysUserNodeList.item(0).getNodeValue();

					NodeList sysPassNodeElementList = element.getElementsByTagName("mysqlPass");
					Element element8 = (Element)sysPassNodeElementList.item(0);
					NodeList sysPassNodeList = element8.getChildNodes();
					system_data[7] = sysPassNodeList.item(0).getNodeValue();

					NodeList apiKeyNodeElementList = element.getElementsByTagName("api_key");
					Element element9 = (Element)apiKeyNodeElementList.item(0);
					NodeList apiKeyNodeList = element9.getChildNodes();
					system_data[8] = apiKeyNodeList.item(0).getNodeValue();

					NodeList gmtoffsetNodeElementList = element.getElementsByTagName("gmt_offset");
					Element element10 = (Element)gmtoffsetNodeElementList.item(0);
					NodeList gmtoffsetNodeList = element10.getChildNodes();
					system_data[9] = gmtoffsetNodeList.item(0).getNodeValue();

					NodeList pwroffsetNodeElementList = element.getElementsByTagName("pwr_offset");
					Element element11 = (Element)pwroffsetNodeElementList.item(0);
					NodeList pwroffsetNodeList = element11.getChildNodes();
					system_data[10] = pwroffsetNodeList.item(0).getNodeValue();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return system_data;
	}

	public static String now(String dateFormat)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(cal.getTime());
	}

	public static void doWindInterface(String[] args)
	{
		try
		{
			if (args.length >= 0)
			{
				//NOT USED: String[] testdata = null;

				//NOT USED: String inFileName = "tenminaveragewindturbine.csv";
				String outFileName = "windturbinecurrent.txt";
				//NOT USED: String outFileName4 = "ss" + now("yyyy_MM") + ".csv";
				String outFileName3 = "mostcurrentwindturbine.csv";
				String outFileName2 = "chart.html";
				//NOT USED: String outFileName5 = "tenminaveragewindturbine.csv";


				FileWriter outFileWriter = new FileWriter(outFileName);
				FileWriter outFileWriter2 = new FileWriter(outFileName2);
				FileWriter outFileWriter3 = new FileWriter(outFileName3);

				PrintWriter outStream = new PrintWriter(outFileWriter);
				PrintWriter outStream2 = new PrintWriter(outFileWriter2);
				PrintWriter outStream3 = new PrintWriter(outFileWriter3);

				int arraysize = maxAvgCount;
				//NOT USED: String inLine = null;String lastLine = null;
				String power = "0";String Watts = "0";String RPM = "0";String Wind = "0";String TurbineStatus = "0";String GridStatus = "0";String SystemStatus = "0";String myTime = "0";
				//NOT USED: String volts = "0";
				String cpowerstring = "";String cRPMstring = "";String ctimestring = "";
				double avgpower = 0.0D;double KWatts = 0.0D;double avgRPM = 0.0D;double avgWind = 0.0D;double dailyTotal = 0.0D;
				//NOT USED: double avgvolts = 0.0D;
				//NOT USED: double[] cpower = new double[arraysize];double[] cRPM = new double[arraysize];double[] ctime = new double[arraysize];
				double[] aa = { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D };
				//NOT USED: int numberofdatalines = 0;int mycounter = arraysize;
				int i = 0;

				runskzcmd();

				aa = tenMinAvgData;
				String[] d = inData;

				int numberOfd = Array.getLength(d);
				if (aa[15] != 0.0D)
				{
					avgpower = aa[13];
					//NOT USED: avgvolts = aa[6];
					avgRPM = aa[19];
					avgWind = aa[20];
					KWatts = aa[4];
				}
				if (numberOfd >= 39)
				{
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
				for (i = 0; i <= arraysize - 1; i++)
				{
					cpowerstring = cpowerstring + "," + avgData[i][13];
					cRPMstring = cRPMstring + "," + avgData[i][19];
					ctimestring = ctimestring + "," + Double.toString(i);
				}
				if (!d[0].equals("0"))
				{
					String openeiTurbineID = mySerialNum.replace("-", "").substring(2);


					String openEIurl = "http://en.openei.org/services/api/2/wfs/w/" + openeiTurbineID;
					sendToDBError = sendToOpenEIDataBase(openEIurl, inData);
					if (!myDBURL.equals("none")) {
						sendToDBOptError = sendToLocalDataBase(myDBURL, inData);
					}
					if (((myDBURL.equals("none")) || (sendToDBOptError == -1)) && (!myMysqlURL.equals("none"))) {
						sendTo30sMysqlDatabase(inData);
					}
					System.out.println("************************* Current Readings ************************       |       ******************** 10 min Averages **************************");

					System.out.print("Status[TSG]:" + TurbineStatus + "," + SystemStatus + "," + GridStatus + ", ");
					System.out.print("power:" + power + ", ");
					System.out.print("RPM:" + RPM + ", ");
					System.out.print("Wind:" + Wind + ", ");
					System.out.format("%s %.2f %s", new Object[] { "Kwatt-Hrs:", Double.valueOf(KWatts), ", " });


					System.out.print("| ");


					System.out.format("%s %.2f %s", new Object[] { "Avg_power:", Double.valueOf(avgpower), ", " });

					System.out.format("%s %.2f %s", new Object[] { "Avg_RPM:", Double.valueOf(avgRPM), ", " });
					System.out.format("%s %.2f", new Object[] { "Avg_Wind:", Double.valueOf(avgWind) });
					System.out.print("\r\n");


					outStream.println("   *** " + mySysTitle + " Current Readings ***   " + "\n");
					outStream.println("Last update: " + myTime);
					outStream.println("Status - Turbine:" + TurbineStatus + ", System:" + SystemStatus + ", Grid:" + GridStatus + "\n");
					outStream.println("power:         " + power + " Watts");

					outStream.println("Turbine Speed: " + RPM + " RPM");
					outStream.println("Wind Speed:    " + Wind + " m/s" + "," + NumberFormat.getInstance().format(Double.parseDouble(Wind) * 2.2369363D) + "mph");
					outStream.print("Daily Energy:   ");
					outStream.format("%s%.2f%s%n", new Object[] { " ", Double.valueOf(dailyTotal), " KWatt-Hrs" });
					outStream.print("Total Energy:   ");
					if (d[0].equals("103853")) {
						outStream.format("%s%.2f%s%n%n", new Object[] { " ", Double.valueOf(KWatts), " KWatt-Hrs (from 8/2/08)" });
					} else {
						outStream.format("%s%.2f%s%n%n", new Object[] { " ", Double.valueOf(KWatts), " KWatt-Hrs" });
					}
					outStream.println("            *** 10 min Averages ***   \n");
					outStream.format("%s %.2f%s%n", new Object[] { "Avg power:     ", Double.valueOf(avgpower), " Watts" });

					outStream.format("%s %.2f%s%n", new Object[] { "Turbine Speed: ", Double.valueOf(avgRPM), " RPM" });
					outStream.format("%s %.2f %s %.2f %s %n", new Object[] { "Wind Speed:    ", Double.valueOf(avgWind), " m/s", Double.valueOf(avgWind * 2.2369363D), "mph" });
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

					/*NOT USED: double tempTime = 0.0D;
          try
          {
            tempTime = Double.parseDouble(d[2]);
          }
          catch (Exception e)
          {
            System.out.println(e);
            tempTime = 0.0D;
          }*/
				}
				else
				{
					System.out.println("error: no data from turbine");
				}
			}
			else
			{
				System.out.println("Usage: windinterface2b 'input filename', 'output filiename'");
				System.exit(0);
			}
		}
		catch (IOException e)
		{
			System.out.println("IOExcepton:");
			e.printStackTrace();
		}
	}

	public static void runskzcmd()
	{
		try
		{
			double myTime = 0.0D;
			//NOT USED: double timeUb = 0.0D;double timeLb = 0.0D;double pwrUb = 0.0D;double pwrLb = 0.0D;
			double pwrTot = 0.0D;
			//NOT USED: double tempVar = 0.0D;
			int i = 0;int numberOfChar = 0;int j = 0;int k = 0;
			String dataOutFileName = "ss" + now("yyyy_MM") + ".csv";
			String dataOutFileName3 = "tenminaverage_ss" + now("yyyy_MM") + ".csv";
			String dataOutFileName2 = "tenminaveragewindturbine.csv";

			FileWriter outFileWriterTest = new FileWriter(dataOutFileName, true);




			PrintWriter outStreamTest = new PrintWriter(outFileWriterTest);






			String OS = System.getProperty("os.name");
			String execPath = myPath + "skzcmd.exe -z +" + mySysID + " dstat 1 0";
			if (!OS.startsWith("Windows")) {
				execPath = "./" + myPath + "s2zcmd -z +" + mySysID + " dstat 1 0";
			}
			Process p = Runtime.getRuntime().exec(execPath);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null)
			{
				String[] d = line.split(",");
				if (d.length >= 2)
				{
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

					inData[4] = Double.toString(pwrTot + Double.parseDouble(myPowerOffset));




					String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:z").format(Double.valueOf((myTime + Double.parseDouble(myGMT_Offset) * 3600.0D) * 1000.0D));

					inData[3] = date;

					int tempHrs = Integer.parseInt(now("HH"));
					int tempMin = Integer.parseInt(now("mm"));
					if ((tempHrs == 0) && (tempMin == 0)) {
						myDailyTotal = 0.0D;
					}
					myDailyTotal += Double.parseDouble(inData[13]) * 0.0083333D;
					inData[5] = Double.toString(myDailyTotal);

					File dataFile = new File(dataOutFileName);
					if (dataFile.length() > 0L)
					{
						outStreamTest.println(Arrays.toString(inData));
					}
					else
					{
						outStreamTest.println("Turbine ID,SW Version,Time(sec),Time(MDY:HMS),watt-hours,DailyTot,Voltage In,Voltage DC Bus,Voltage L1,Voltage L2,voltage rise,min v from rpm,Current out,Power out,Power reg,Power max,Line Frequency,Inverter Frequency,Line Resistance,RPM,Windspeed (ref meters/sec),TargetTSR,Ramp RPM,Boost pulswidth,Max BPW,current amplitude, T1,T2,T3,Event count,Last event code,Event status,Event value,Turbine status,Grid status,System status,Slave Status,Access Status,Timer,");
						outStreamTest.println(Arrays.toString(inData));
					}
				}
			}
			input.close();

			outStreamTest.close();
			if ((avgCount < maxAvgCount) && (numberOfChar >= 39)) {
				for (i = 0; i < numberOfChar; i++)
				{
					avgData[avgCount][i] = inData[i];
					if (i == numberOfChar - 1) {
						avgCount += 1;
					}
					if (avgCount >= maxAvgCount)
					{
						avgCount = 0;
						for (j = 0; j < maxAvgCount; j++) {
							for (k = 0; k < numberOfChar - 1; k++)
							{
								if (k == 3)
								{
									tenMinAvgData[3] = 0.0D;
									k = 4;
								}
								double tempDouble = 0.0D;
								try
								{
									tempDouble = Double.parseDouble(avgData[j][k]);
								}
								catch (NumberFormatException localNumberFormatException) {}
								if (j == 0) {
									tenMinAvgData[k] = tempDouble;
								} else {
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
			if (averagesReadyToPrint)
			{
				//NOT USED: String message;
			FileWriter outFileWriterTest2 = new FileWriter(dataOutFileName2);
			PrintWriter outStreamTest2 = new PrintWriter(outFileWriterTest2);



			outStreamTest2.println(Arrays.toString(tenMinAvgData));
			outStreamTest2.close();



			FileWriter outFileWriterTest3 = new FileWriter(dataOutFileName3, true);
			PrintWriter outStreamTest3 = new PrintWriter(outFileWriterTest3);

			File dataFile3 = new File(dataOutFileName3);
			if (dataFile3.length() > 0L)
			{
				outStreamTest3.println(inData[3] + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
			}
			else
			{
				outStreamTest3.println("Date, Power(watts), RPM, Wind(meters/sec), Total Energy(Watt-Hrs)");
				outStreamTest3.println(inData[3] + ", " + tenMinAvgData[13] + ", " + tenMinAvgData[19] + ", " + tenMinAvgData[20] + ", " + tenMinAvgData[4]);
			}
			outStreamTest3.close();



			sendToDBOptError = sendTo10minLocalDatabase();
			if (((sendToDBOptError == -1) || (myDBURL.equals("none"))) && (!myMysqlURL.equals("none")))
			{
				Connection connection;
				try
				{
					Class.forName("org.gjt.mm.mysql.Driver");
					String dbURL = myMysqlURL;
					String username = myMysqlUser;
					String password = myMysqlPass;
					connection = DriverManager.getConnection(dbURL, username, password);
				}
				catch (ClassNotFoundException e)
				{
					System.out.println("Database driver not found.");
					return;
				}
				catch (SQLException e)
				{
					System.out.println("Error opening the local db connection: " + e.getMessage());
					return;
				}
				try
				{
					String myQry = "INSERT into windturbine10avg( \tpower       ,\tvolts,\twindspeed,\ttotalpower,\trpm,\tcurrenttime) VALUES (?,?,?,?,?,?) ";
					PreparedStatement ps = connection.prepareStatement(myQry);
					ps.setDouble(1, tenMinAvgData[13]);
					ps.setDouble(2, tenMinAvgData[6]);
					ps.setDouble(3, tenMinAvgData[20]);
					ps.setDouble(4, tenMinAvgData[4]);
					ps.setDouble(5, tenMinAvgData[19]);
					Timestamp sqlTimestamp = new Timestamp((long) ((tenMinAvgData[2] + Double.parseDouble(myGMT_Offset) * 3600.0D) * 1000L));
					ps.setTimestamp(6, sqlTimestamp);


					System.out.println("Attempting to send to backup MySql Database (10minAvg)...");
					ps.executeUpdate();
				}
				catch (SQLException e)
				{
					System.out.println("Error executing the SQL statement: <br>" + e.getMessage());
					System.out.println("Error sending to backup MySQL Database (10minAvg)");
				}
				try
				{
					connection.close();
				}
				catch (SQLException e)
				{
					System.out.println("Error closing the db connection: " + e.getMessage());
				}
			}
			averagesReadyToPrint = false;
			}
		}
		catch (Exception err)
		{
			err.printStackTrace();
		}
	}

	private static int sendToOpenEIDataBase(String baseURL, String[] inData)
	{
		//NOT USED: String message;

		int didWork = 0;



		String[] d = inData;



		//NOT USED: String sysID = d[0].replace("[", "");
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


		//NOT USED: double tempTime = 0.0D;
		//NOT USED: String myGMT_Offset = "7.0";



		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;
		/*NOT USED: try
    {
      tempTime = Double.parseDouble(d[2]);
    }
    catch (Exception e)
    {
      System.out.println("Exception error : " + e);
      tempTime = 0.0D;
    }*/
		try
		{
			String keyString = "turbineID,GMT time,Day & time,PowerW, DailyKW, TotalKW, RPM, Wind, Volts, Tstat, Sstat, Gstat";
			keyString = keyString.replaceAll(" ", "");

			String openeiTurbineID = mySerialNum.replace("-", "").substring(2);
			String skyDataString = openeiTurbineID + "," + GMT + "," + tempString + "," + power + "," + dailyTotal + "," + Watts + "," + RPM + "," + Wind + "," + volts + "," + TurbineStatus + "," + SystemStatus + "," + GridStatus;

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] keys = keyString.split(",");
			String[] data = skyDataString.split(",");

			int i = 0;
			while (i < keys.length)
			{
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
			while ((s = br.readLine()) != null)
			{
				if (s != null) {
					check = s;
				}
			}
			if (check.substring(0, 6).equals("{\"Item"))
			{
				didWork = 1;
				System.out.println("Data was Successfully sent to OpenEI");
			}
			else
			{
				didWork = -1;
				System.out.println("OpenEI Error! " + s);
			}
			br.close();
		}
		catch (MalformedURLException e)
		{
			System.out.println("Error executing the  statement: <br>" + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("IO exception: <br>" + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	private static int sendToLocalDataBase(String baseURL, String[] inData)
	{
		//NOT USED: String message;

		int didWork = 0;



		String[] d = inData;



		//NTO USED: String sysID = d[0].replace("[", "");
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


		//NOT USED: double tempTime = 0.0D;
		//NOT USED: String myGMT_Offset = "7.0";



		int year = Integer.parseInt(myDate[2]);
		int month = Integer.parseInt(myDate[0]);
		int day = Integer.parseInt(myDate[1]);
		int hour = Integer.parseInt(myTimeHMS[0]);
		int min = Integer.parseInt(myTimeHMS[1]);
		int sec = Integer.parseInt(myTimeHMS[2]);

		String tempString = year + "-" + month + "-" + day + "%20" + hour + ":" + min + ":" + sec;
		/*NOT USED: try
    {
    	tempTime = Double.parseDouble(d[2]);
    }
    catch (Exception e)
    {
      System.out.println("Exception error : " + e);
      tempTime = 0.0D;
    }*/
		try
		{
			String keyString = "turbineID,GMT time,Day & time,PowerW, DailyKW, TotalKW, RPM, Wind, Volts, Tstat, Sstat, Gstat";
			keyString = keyString.replaceAll(" ", "");

			String openeiTurbineID = mySerialNum.replace("-", "").substring(2);
			String skyDataString = openeiTurbineID + "," + GMT + "," + tempString + "," + power + "," + dailyTotal + "," + Watts + "," + RPM + "," + Wind + "," + volts + "," + TurbineStatus + "," + SystemStatus + "," + GridStatus;

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] keys = keyString.split(",");
			String[] data = skyDataString.split(",");

			int i = 0;
			while (i < keys.length)
			{
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
			while ((s = br.readLine()) != null)
			{
				if (s != null) {
					check = s;
				}
			}
			if (check.equals("SUCCESS"))
			{
				didWork = 1;
				System.out.println("Data was Successfully sent to Local 30s Database (HTTP)");
				System.out.println("Success");
			}
			else
			{
				didWork = -1;
				System.out.println("Error sending to Local 30s Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e)
		{
			System.out.println("Error executing the  statement: <br>" + e.getMessage());
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("IO exception: <br>" + e.getMessage());
			e.printStackTrace();
		}
		return didWork;
	}

	private static int sendTo30sMysqlDatabase(String[] inData)
	{
		//NOT USED: String message;
		int error = 1;



		String[] d = inData;

		String power = d[13];
		String volts = d[6];
		String Watts = d[4];
		String RPM = d[19];
		String Wind = d[20];


		double tempTime = 0.0D;
		try
		{
			tempTime = Double.parseDouble(d[2]);
		}
		catch (Exception e)
		{
			System.out.println("Exception error : " + e);
			tempTime = 0.0D;
		}
		Connection connection;
		try
		{
			Class.forName("org.gjt.mm.mysql.Driver");

			String dbURL = myMysqlURL;
			String username = myMysqlUser;
			String password = myMysqlPass;
			connection = DriverManager.getConnection(dbURL, username, password);
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Database driver not found.");
			return -1;
		}
		catch (SQLException e)
		{
			System.out.println("Local Error opening the db connection: " + 
					e.getMessage());
			return -1;
		}
		try
		{
			String myQry = "INSERT into windturbine( \tpower       ,\tvolts,\twindspeed,\ttotalpower,\trpm,\tcurrenttime) VALUES (?,?,?,?,?,?) ";











			PreparedStatement ps = connection.prepareStatement(myQry);
			ps.setDouble(1, Double.parseDouble(power));
			ps.setDouble(2, Double.parseDouble(volts));
			ps.setDouble(3, Double.parseDouble(Wind));
			ps.setDouble(4, Double.parseDouble(Watts));
			ps.setDouble(5, Double.parseDouble(RPM));
			Timestamp sqlTimestamp = new Timestamp((long) ((tempTime + Double.parseDouble(myGMT_Offset) * 3600.0D) * 1000L));
			ps.setTimestamp(6, sqlTimestamp);


			System.out.println("Trying Backup MySQL Database (30s)...");
			ps.executeUpdate();
		}
		catch (SQLException e)
		{
			System.out.println("Error executing the SQL statement: <br>" + e.getMessage());
			error = -1;
		}
		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			System.out.println("Error closing the db connection: " + e.getMessage());
		}
		if (error == 1) {
			System.out.println("Data was Successfully sent to Backup Mysql Database (30s)");
		}
		return error;
	}

	private static int sendTo10minLocalDatabase()
	{
		//NOT USED: String message;
		System.out.println(myDBURL);
		if (myDBURL.equals("none")) {
			System.out.println("Skipped 10min avg DB send because no DBURL set");
			return -1;
		}
		int didWork = 0;




		String power = String.valueOf(tenMinAvgData[13]);
		String volts = String.valueOf(tenMinAvgData[6]);
		String Watts = String.valueOf(tenMinAvgData[4]);
		String RPM = String.valueOf(tenMinAvgData[19]);
		String Wind = String.valueOf(tenMinAvgData[20]);
		Timestamp sqlTimestamp = new Timestamp((long) ((tenMinAvgData[2] + Double.parseDouble(myGMT_Offset) * 3600.0D) * 1000L));
		String CurrentTime = sqlTimestamp.toString();
		try
		{
			String keyString = "Day & time,PowerW, TotalKW, RPM, Wind, Volts";
			keyString = keyString.replaceAll(" ", "");

			String skyDataString = CurrentTime + "," + power + "," + Watts + "," + RPM + "," + Wind + "," + volts;

			Map<String, String> dataMap = new HashMap<String, String>();

			String[] keys = keyString.split(",");
			String[] data = skyDataString.split(",");

			int i = 0;
			while (i < keys.length)
			{
				dataMap.put(keys[i], data[i]);
				i++;
			}
			Gson gson = new Gson();
			String dataJsonString = gson.toJson(dataMap);
			String jsonString = URLEncoder.encode(dataJsonString, "UTF-8");
			String urlString = myDBURL + "?10minavg=yes&systemname=" + mySysName + "&" + "json_data=" + jsonString;


			URL url = new URL(urlString);




			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));


			String check = null;
			String s;
			while ((s = br.readLine()) != null)
			{
				if (s != null) {
					check = s;
				}
			}
			if (check.equals("SUCCESS"))
			{
				didWork = 1;
				System.out.println("Data was Successfully sent to Local 10minAvg Database (HTTP)");
				System.out.println("Success");
			}
			else
			{
				didWork = -1;
				System.out.println("Error sending to Local 10minAvg Database (HTTP) " + s);
			}
			br.close();
		}
		catch (MalformedURLException e)
		{
			//NOT USED: message = "Error executing the  statement: <br>" + e.getMessage();
			e.printStackTrace();
		}
		catch (IOException e)
		{
			//NOT USED: message = "IO exception: <br>" + e.getMessage();
			e.printStackTrace();
		}
		return didWork;
	}

	private static double readDailyTot()
	{
		double dailyTot = 0.0D;
		try
		{
			String inFileName = "mostcurrentwindturbine.csv";
			FileReader inFileReader = new FileReader(inFileName);
			BufferedReader inStream = new BufferedReader(inFileReader);
			String inLine = null;
			String lastLine = null;
			//NOT USED: int i = 0;
			while ((inLine = inStream.readLine()) != null)
			{
				//NOT USED: i++;
				lastLine = inLine;
			}
			if (lastLine != null)
			{
				String[] d = lastLine.split(",");
				if (d.length > 2) {
					dailyTot = Double.parseDouble(d[5]);
				}
			}
			inStream.close();
		}
		catch (IOException e)
		{
			System.out.println("IOExcepton:");
			e.printStackTrace();
		}
		return dailyTot;
	}

	public static void main(String[] args)
			throws AWTException, IOException
	{
		new windinterface2b_openei(args);
	}

	public windinterface2b_openei(final String[] args)
	{
		for (int j = 0; j < 20; j++) {
			for (int k = 0; k < 40; k++) {
				avgData[j][k] = "0";
			}
		}
		String[] data = getPref();
		mySysTitle = data[0];
		mySysID = data[1].replaceAll("\\W", "");
		mySerialNum = data[2];
		myDBURL = data[3];
		mySysName = data[4];
		myMysqlURL = data[5];
		myMysqlUser = data[6];
		myMysqlPass = data[7];
		myApiKey = data[8];
		myGMT_Offset = data[9];
		myPowerOffset = data[10];

		System.out.println("windinterface2b version 2.0D using:");
		System.out.print("system - Title: " + mySysTitle);
		System.out.print(", ID: " + mySysID);
		System.out.print(", SN: " + mySerialNum);
		System.out.print(", dbURL: " + myDBURL);
		System.out.print(", sys_name: " + mySysName);
		System.out.print(", api key: " + myApiKey);
		System.out.print(", GMT+ " + myGMT_Offset);
		System.out.print(", +pwr " + myPowerOffset);

		System.out.println("");

		myDailyTotal = readDailyTot();

		windowSystemName = mySysTitle;







		TimerTask task = new TimerTask()
		{
			public void run()
			{
				if (!windinterface2b_openei.task2Suspend)
				{
					String s = windinterface2b_openei.getskzcmd();

					String[] values = s.split(",");
					if (!values[0].equals("NullPointer Error"))
					{
						values[0] = values[0].replace("[", "");
						values[(values.length - 1)] = values[(values.length - 1)].replace("]", "");



						windinterface2b_openei.wind = Double.parseDouble(values[20]);
						windinterface2b_openei.power = Double.parseDouble(values[13]);
						windinterface2b_openei.speed = Double.parseDouble(values[19]);
						windinterface2b_openei.ts = values[33];
						windinterface2b_openei.ss = values[35];
						windinterface2b_openei.gs = values[34];
						windinterface2b_openei.dayEnergy = Double.parseDouble(values[5]) / 1000.0D;
						windinterface2b_openei.totEnergy = Double.parseDouble(values[4]) / 1000.0D;
						windinterface2b_openei.volts = values[6];
					}
				}
				windinterface2b_openei.doWindInterface(args);
			}
		};
		/*NOT USED: TimerTask task2 = new TimerTask()
    {
      public void run()
      {
        if (!windinterface2b_openei.task2Suspend)
        {
          String s = windinterface2b_openei.getskzcmd();

          String[] values = s.split(",");
          if (!values[0].equals("NullPointer Error"))
          {
            values[0] = values[0].replace("[", "");
            values[(values.length - 1)] = values[(values.length - 1)].replace("]", "");



            windinterface2b_openei.wind = Double.parseDouble(values[20]);
            windinterface2b_openei.power = Double.parseDouble(values[13]);
            windinterface2b_openei.speed = Double.parseDouble(values[19]);
            windinterface2b_openei.ts = values[33];
            windinterface2b_openei.ss = values[35];
            windinterface2b_openei.gs = values[34];
            windinterface2b_openei.dayEnergy = Double.parseDouble(values[5]) / 1000.0D;
            windinterface2b_openei.totEnergy = Double.parseDouble(values[4]) / 1000.0D;
            windinterface2b_openei.volts = values[6];
          }
        }
      }
    };*/
		Timer timer = new Timer();
		timer.schedule(task, 0L, 30000L);





		System.out.println("\n");
	}

	public static String getskzcmd()
	{
		String[] theData = { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
		try
		{
			double myTime = 0.0D;
			//NOT USED:double timeUb = 0.0D;double timeLb = 0.0D;double pwrUb = 0.0D;double pwrLb = 0.0D;
			double pwrTot = 0.0D;
			//NOT USED: double tempVar = 0.0D;
			//NOT USED: int i = 0; int numberOfChar = 0;int j = 0;int k = 0;

			String OS = System.getProperty("os.name");
			String execPath = myPath + "skzcmd.exe -z +" + mySysID + " dstat 1 0";
			if (!OS.startsWith("Windows")) {
				execPath = "./" + myPath + "s2zcmd -z +" + mySysID + " dstat 1 0";
			}
			Process p = Runtime.getRuntime().exec(execPath);
			BufferedReader input2 = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input2.readLine()) != null)
			{
				String[] d = line.split(",");
				if (d.length >= 2)
				{
					theData[0] = d[0].replaceAll("\\D", "");
					String[] tempd1 = d[1].split(" ");
					theData[1] = tempd1[0].replaceAll("\\D", "");
					theData[2] = tempd1[1];
					theData[4] = d[2];
					int ii = 0;
					for (ii = 3; ii < d.length - 1; ii++) {
						theData[(ii + 3)] = d[ii];
					}
					//NOT USED: numberOfChar = Array.getLength(theData);

					myTime = Double.parseDouble(theData[2]);

					pwrTot = Double.parseDouble(theData[4]);

					theData[4] = Double.toString(pwrTot + Double.parseDouble(myPowerOffset));




					String date = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:z").format(Double.valueOf((myTime + Double.parseDouble(myGMT_Offset) * 3600.0D) * 1000.0D));

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
		catch (Exception err)
		{
			err.printStackTrace();
		}
		return Arrays.toString(theData);
	}

	public static String doPatch()
	{
		//NOT USED: String today = now("MM/dd/yyyy");
		String result;



		BufferedReader br = null;

		//NOT USED: String year_month = now("yyyy_MM");
		try
		{
			URL url = new URL("http://69.20.174.50/mostcurrentwindturbine.csv");

			InputStream is = url.openStream();
			br = new BufferedReader(new InputStreamReader(is));
		}
		catch (MalformedURLException e)
		{
			System.out.println("Bad URL");
		}
		catch (IOException e)
		{
			System.out.println("IO Error Get Data: " + e.getMessage());
		}
		try
		{
			//NOT USED: boolean eof = false;
			String s = br.readLine();
			//NOT USED: int i = 0;int numberintemp = 0;int numberOfReadings = 0;int ii = 0;
			//NOT USED: String date = "nothing";
			//NOT USED: double dayEnergy = 0.0D;double dayHours = 0.0D;double yesterdayEnergy = 0.0D;double dayStart = 0.0D;
			String lastLine = s;

			String myString = "";

			String[] d = lastLine.split(",");
			String power = d[13];
			String volts = d[6];
			String Watts = d[4];
			String RPM = d[19];
			String Wind = d[20];



			myString = "************* Current Readings at: " + d[3] + "****************" + String.valueOf('\n');
			myString = myString + " power: " + power + " Watts\t";
			myString = myString + " volts: " + volts + "\t";
			myString = myString + " RPM: " + RPM + " RPM\t";
			myString = myString + " Wind: " + Wind + " m/s" + String.valueOf('\n');
			myString = myString + " Daily Total Energy " + NumberFormat.getNumberInstance().format(dayEnergy / 1000.0D) + " KWatts-Hrs" + "\t";
			myString = myString + " Total Energy:" + NumberFormat.getNumberInstance().format(Double.parseDouble(Watts) / 1000.0D) + " KWatts" + String.valueOf('\n');

			result = s;
		}
		catch (IOException e)
		{
			System.out.println("IO Error : " + e.getMessage());
			return "IO Error";
		}
		catch (NullPointerException npe)
		{
			System.out.println("NullPointer Error : " + npe.getMessage());
			inData[0] = "0";
			return "NullPointer Error";
		}
		return result;
	}

	public void BasicDraw()
	{
		this.window.setSize(530, 250);
		this.window.setLocation(50, 50);

		JComponent myComp = new windinterface2b_openei.MyComponent();


		this.window.getContentPane().add(myComp);





		this.window.setVisible(this.displayWindow);

		windinterface2b_openei.ComponentMover cm = new windinterface2b_openei.ComponentMover();
		cm.registerComponent(new Component[] { this.window });
	}

	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource() == this.button) {
			System.exit(0);
		}
	}

	public void doEditPref()
	{
		String line = "";
		int doEdit = 0;int doSave = 0;

		System.out.println("\nwindinterface Prefernces Editor:");

		String[] data = getPref();
		mySysTitle = data[0];
		mySysID = data[1];
		mySerialNum = data[2];
		myDBURL = data[3];
		mySysName = data[4];
		myMysqlURL = data[5];
		myMysqlUser = data[6];
		myMysqlPass = data[7];
		myApiKey = data[8];
		myGMT_Offset = data[9];
		myPowerOffset = data[10];

		printPrefs();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			System.out.print("Do you wish to change preferences? yes:no ");

			line = reader.readLine();
		}
		catch (IOException ioe)
		{
			System.out.println("error reading IO:");
			ioe.printStackTrace();
		}
		if ((line.equals("yes")) || (line.equals("YES")) || (line.equals("Yes")) || (line.equals("y")) || (line.equals("Y"))) {
			doEdit = 1;
		}
		if ((line.equals("no")) || (line.equals("NO")) || (line.equals("No")) || (line.equals("n")) || (line.equals("N"))) {
			doEdit = 0;
		}
		if (doEdit > 0)
		{
			editPref();
			try
			{
				System.out.print("Do you wish to save changes? yes:no ");

				line = reader.readLine();
			}
			catch (IOException ioe)
			{
				System.out.println("error reading IO:");
				ioe.printStackTrace();
			}
			if ((line.equals("yes")) || (line.equals("YES")) || (line.equals("Yes")) || (line.equals("y")) || (line.equals("Y"))) {
				doSave = 1;
			}
			if ((line.equals("no")) || (line.equals("NO")) || (line.equals("No")) || (line.equals("n")) || (line.equals("N"))) {
				doSave = 0;
			}
			if (doSave > 0) {
				setPref();
			}
		}
	}

	private void addElement(Container c, Component e, int x, int y, int h, int w)
	{
		e.setBounds(x, y, h, w);
		c.add(e);
	}

	public void simpleForm()
	{
		String[] data = getPref();



		mySysTitle = data[0];
		mySysID = data[1];
		mySerialNum = data[2];
		myDBURL = data[3];
		mySysName = data[4];
		myMysqlURL = data[5];
		myMysqlUser = data[6];
		myMysqlPass = data[7];
		myApiKey = data[8];
		myGMT_Offset = data[9];
		myPowerOffset = data[10];

		this.frame.setBackground(new Color(255, 255, 255, 255));


		this.frame.getContentPane().add(this.contentPanel, "Center");
		this.contentPanel.setLayout(null);
		this.jLabel1 = new JLabel("System Title:");
		addElement(this.contentPanel, this.jLabel1, 10, 0, 250, 20);
		this.jTextField1 = new JTextField(mySysTitle);
		this.jTextField1.setToolTipText("Enter the Name for your system");
		addElement(this.contentPanel, this.jTextField1, 10, 20, 250, 20);
		this.jLabel2 = new JLabel("Turbine System ID (zigbee)");
		addElement(this.contentPanel, this.jLabel2, 10, 40, 250, 20);
		this.jTextField2 = new JTextField(mySysID);
		this.jTextField2.setToolTipText("Enter the zigbee ID number i.e. 000abc00");
		addElement(this.contentPanel, this.jTextField2, 10, 60, 250, 20);
		this.jLabel3 = new JLabel("Turbine System Serial Number");
		addElement(this.contentPanel, this.jLabel3, 10, 80, 250, 20);
		this.jTextField3 = new JTextField(mySerialNum);
		this.jTextField3.setToolTipText("Enter the System serial number i.e. 1010-XXXX");
		addElement(this.contentPanel, this.jTextField3, 10, 100, 250, 20);
		this.jLabel4 = new JLabel("Local Database URL");
		addElement(this.contentPanel, this.jLabel4, 10, 120, 250, 20);
		this.jTextField4 = new JTextField(myDBURL);
		this.jTextField4.setToolTipText("Optional: leave as \"none\" for no local database");
		addElement(this.contentPanel, this.jTextField4, 10, 140, 250, 20);
		this.jLabel5 = new JLabel("System Name for local Database");
		addElement(this.contentPanel, this.jLabel5, 10, 160, 250, 20);
		this.jTextField5 = new JTextField(mySysName);
		this.jTextField5.setToolTipText("Optional: leave as \"none\" for no local database");
		addElement(this.contentPanel, this.jTextField5, 10, 180, 250, 20);

		this.jLabel6 = new JLabel("Address for backup Mysql Database");
		addElement(this.contentPanel, this.jLabel6, 10, 200, 250, 20);
		this.jTextField6 = new JTextField(myMysqlURL);
		this.jTextField6.setToolTipText("Optional: leave as \"none\" for no backup database");
		addElement(this.contentPanel, this.jTextField6, 10, 220, 250, 20);

		this.jLabel7 = new JLabel("User name for backup Mysql Database");
		addElement(this.contentPanel, this.jLabel7, 10, 240, 250, 20);
		this.jTextField7 = new JTextField(myMysqlUser);
		this.jTextField7.setToolTipText("Optional: leave as \"none\" for no backup database");
		addElement(this.contentPanel, this.jTextField7, 10, 260, 250, 20);

		this.jLabel8 = new JLabel("Password for backup Mysql Database");
		addElement(this.contentPanel, this.jLabel8, 10, 280, 250, 20);
		this.jTextField8 = new JTextField(myMysqlPass);
		this.jTextField8.setToolTipText("Optional: leave as \"none\" for no backup database");
		addElement(this.contentPanel, this.jTextField8, 10, 300, 250, 20);

		this.jLabel9 = new JLabel("API-Key for OpenEI Database");
		addElement(this.contentPanel, this.jLabel9, 10, 320, 250, 20);
		this.jTextField9 = new JTextField(myApiKey);
		this.jTextField9.setToolTipText("API Key is required for openEI access");
		addElement(this.contentPanel, this.jTextField9, 10, 340, 250, 20);
		this.jLabel10 = new JLabel("GMT time offset for local timezone");
		addElement(this.contentPanel, this.jLabel10, 10, 360, 250, 20);
		this.jTextField10 = new JTextField(myGMT_Offset);
		this.jTextField10.setToolTipText("Enter in the time offset from Greenwich Mean Time for your local time zone");
		addElement(this.contentPanel, this.jTextField10, 10, 380, 250, 20);
		this.jLabel11 = new JLabel("*Power Offset");
		addElement(this.contentPanel, this.jLabel11, 10, 400, 250, 20);
		this.jTextField11 = new JTextField(myPowerOffset);
		this.jTextField11.setToolTipText("Optional: add watts to correct/adjust output");
		addElement(this.contentPanel, this.jTextField11, 10, 420, 250, 20);

		this.jButton1 = new JButton("Save");
		addElement(this.contentPanel, this.jButton1, 200, 440, 100, 30);
		this.jButton2 = new JButton("Cancel");
		addElement(this.contentPanel, this.jButton2, 0, 440, 100, 30);

		this.jButton1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int reply = JOptionPane.showConfirmDialog(null, "Do You Wish Save Settings?", "Saving Settings?", 0);
				if (reply == 0)
				{
					windinterface2b_openei.windowSystemName = windinterface2b_openei.mySysTitle = windinterface2b_openei.this.jTextField1.getText();
					windinterface2b_openei.mySysID = windinterface2b_openei.this.jTextField2.getText();
					windinterface2b_openei.mySerialNum = windinterface2b_openei.this.jTextField3.getText();
					windinterface2b_openei.myDBURL = windinterface2b_openei.this.jTextField4.getText();
					windinterface2b_openei.mySysName = windinterface2b_openei.this.jTextField5.getText();
					windinterface2b_openei.myMysqlURL = windinterface2b_openei.this.jTextField6.getText();
					windinterface2b_openei.myMysqlUser = windinterface2b_openei.this.jTextField7.getText();
					windinterface2b_openei.myMysqlPass = windinterface2b_openei.this.jTextField8.getText();
					windinterface2b_openei.myApiKey = windinterface2b_openei.this.jTextField9.getText();
					windinterface2b_openei.myGMT_Offset = windinterface2b_openei.this.jTextField10.getText();
					windinterface2b_openei.myPowerOffset = windinterface2b_openei.this.jTextField11.getText();

					windinterface2b_openei.this.setPref();
					windinterface2b_openei.task2Suspend = false;
					windinterface2b_openei.this.frame.dispose();
				}
				if (reply == 1)
				{
					windinterface2b_openei.task2Suspend = false;
					windinterface2b_openei.this.frame.dispose();
				}
			}
		});
		this.jButton2.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				windinterface2b_openei.task2Suspend = false;
				windinterface2b_openei.this.frame.dispose();
			}
		});
		this.frame.setTitle("Windinterface Preferences");
		this.frame.setSize(320, 510);
		this.frame.setLocation(new Point(150, 150));

		this.frame.setVisible(true);
	}

	public void editPref()
	{
		String line = "";
		int loop = 1;    


		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			while(loop > 0)
			{
				System.out.print("Enter the line number to change Preferences or '0' to exit: ");
				String firstline = reader.readLine();
				try
				{
					long lint = Long.parseLong(firstline);
					int l = (int)lint;
					line = "";
					switch (l)
					{
					case 0: 
						loop = 0;
						break;
					case 1: 
						System.out.print("System Title: " + mySysTitle + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							mySysTitle = line;
						}
						System.out.println("System Title: " + mySysTitle);
						break;
					case 2: 
						System.out.print("System ID: " + mySysID + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							mySysID = line;
						}
						System.out.println("System ID: " + mySysID);
						break;
					case 3: 
						System.out.print("System Serial Number: " + mySerialNum + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							mySerialNum = line;
						}
						System.out.println("System Serial Number: " + mySerialNum);
						break;
					case 4: 
						System.out.print("Greenwich Mean Time Offset (GMT+): " + myGMT_Offset + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myGMT_Offset = line;
						}
						System.out.println("GMT: " + myGMT_Offset);
						break;
					case 5: 
						System.out.print("Power Offset: " + myPowerOffset + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myPowerOffset = line;
						}
						System.out.println("+pwr: " + myPowerOffset);
						break;
					case 6: 
						System.out.print("db URL: " + myDBURL + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myDBURL = line;
						}
						System.out.println("dbURL: " + myDBURL);
						break;
					case 7: 
						System.out.print("db System Name: " + mySysName + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							mySysName = line;
						}
						System.out.println("System Name: " + mySysName);
						break;
					case 8: 
						System.out.print("OpenEI API Key: " + myApiKey + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myApiKey = line;
						}
						System.out.println("openei api key: " + myApiKey);
						break;
					case 9: 
						System.out.print("Backup Mysql URL: " + myMysqlURL + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myMysqlURL = line;
						}
						System.out.println("Backup Mysql URL: " + myMysqlURL);
						break;
					case 10: 
						System.out.print("Backup Mysql User: " + myMysqlUser + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myMysqlUser = line;
						}
						System.out.println("Backup Mysql User: " + myMysqlUser);
						break;
					case 11: 
						System.out.print("Backup Mysql Pass: " + myMysqlPass + ": ");

						line = reader.readLine();
						if (line.length() > 0) {
							myMysqlPass = line;
						}
						System.out.println("Backup Mysql Pass: " + myMysqlPass);
					}
				}
				catch (NumberFormatException localNumberFormatException) {}
			}

		}
		catch (IOException ioe)
		{
			System.out.println("Something went wrong reading IO:");
			ioe.printStackTrace();
		}
		printPrefs();
	}

	public void printPrefs()
	{
		System.out.println("\nPrefernce Settings:");
		System.out.println("1)  System Title: " + mySysTitle);
		System.out.println("2)  System ID: " + mySysID);
		System.out.println("3)  System SN: " + mySerialNum);
		System.out.println("4)  GMT+ " + myGMT_Offset);
		System.out.println("5)  +pwr " + myPowerOffset);
		System.out.println("6)  dbURL: " + myDBURL);
		System.out.println("7)  System Name: " + mySysName);
		System.out.println("8)  Backup MySQL Url: " + myMysqlURL);
		System.out.println("9)  Backup MySQL User: " + myMysqlUser);
		System.out.println("10) Backup MySQL Pass: " + myMysqlPass);
		System.out.println("11) OpenEI API key: " + myApiKey);
		System.out.println("");
	}

	public String[] setPref()
	{
		String[] system_data = { "none", "none", "none", "none", "none", "none", "none", "none", "none", "0", "0" };
		String myFile = myPath + "windinterfacepref.xml";
		try
		{
			System.out.println("Saving Data To " + myFile);

			File file = new File(myFile);



			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.parse(file);
			doc.getDocumentElement().normalize();

			NodeList list = doc.getElementsByTagName("system");
			for (int i = 0; i < list.getLength(); i++)
			{
				Node node1 = list.item(i);
				if (node1.getNodeType() == 1)
				{
					Element element = (Element)node1;

					NodeList sysTitleNodeElementList = element.getElementsByTagName("sys_title");
					Element element1 = (Element)sysTitleNodeElementList.item(0);
					NodeList sysTitleNodeList = element1.getChildNodes();
					sysTitleNodeList.item(0).setNodeValue(mySysTitle);

					NodeList sysIDNodeElementList = element.getElementsByTagName("sys_id");
					Element element2 = (Element)sysIDNodeElementList.item(0);
					NodeList sysIDNodeList = element2.getChildNodes();
					sysIDNodeList.item(0).setNodeValue(mySysID);

					NodeList serNumNodeElementList = element.getElementsByTagName("serial_num");
					Element element3 = (Element)serNumNodeElementList.item(0);
					NodeList serNumNodeList = element3.getChildNodes();
					serNumNodeList.item(0).setNodeValue(mySerialNum);

					NodeList dbURLNodeElementList = element.getElementsByTagName("dbURL");
					Element element4 = (Element)dbURLNodeElementList.item(0);
					NodeList dbURLNodeList = element4.getChildNodes();
					dbURLNodeList.item(0).setNodeValue(myDBURL);

					NodeList sysNameNodeElementList = element.getElementsByTagName("sys_name");
					Element element5 = (Element)sysNameNodeElementList.item(0);
					NodeList sysNameNodeList = element5.getChildNodes();
					sysNameNodeList.item(0).setNodeValue(mySysName);

					NodeList sysMysqlNodeElementList = element.getElementsByTagName("mysqlURL");
					Element element6 = (Element)sysMysqlNodeElementList.item(0);
					NodeList sysMysqlNodeList = element6.getChildNodes();
					sysMysqlNodeList.item(0).setNodeValue(myMysqlURL);

					NodeList sysUserNodeElementList = element.getElementsByTagName("mysqlUser");
					Element element7 = (Element)sysUserNodeElementList.item(0);
					NodeList sysUserNodeList = element7.getChildNodes();
					sysUserNodeList.item(0).setNodeValue(myMysqlUser);

					NodeList sysPassNodeElementList = element.getElementsByTagName("mysqlPass");
					Element element8 = (Element)sysPassNodeElementList.item(0);
					NodeList sysPassNodeList = element8.getChildNodes();
					sysPassNodeList.item(0).setNodeValue(myMysqlPass);

					NodeList apiKeyNodeElementList = element.getElementsByTagName("api_key");
					Element element9 = (Element)apiKeyNodeElementList.item(0);
					NodeList apiKeyNodeList = element9.getChildNodes();
					apiKeyNodeList.item(0).setNodeValue(myApiKey);

					NodeList gmtoffsetNodeElementList = element.getElementsByTagName("gmt_offset");
					Element element10 = (Element)gmtoffsetNodeElementList.item(0);
					NodeList gmtoffsetNodeList = element10.getChildNodes();
					gmtoffsetNodeList.item(0).setNodeValue(myGMT_Offset);

					NodeList pwroffsetNodeElementList = element.getElementsByTagName("pwr_offset");
					Element element11 = (Element)pwroffsetNodeElementList.item(0);
					NodeList pwroffsetNodeList = element11.getChildNodes();
					pwroffsetNodeList.item(0).setNodeValue(myPowerOffset);
				}
			}
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer trs = tFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new FileOutputStream(new File(myFile)));
			trs.transform(source, result);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		windowSystemName = mySysTitle;

		return system_data;
	}

	class MyComponent
	extends JComponent
	{
		MyComponent() {}

		public void paint(Graphics g)
		{
			String path = windinterface2b_openei.myPath + "Images/";
			DecimalFormat df = new DecimalFormat("#.0");







			String sysName = windinterface2b_openei.windowSystemName + " " + windinterface2b_openei.windowTitle;
			g.setFont(new Font("Helvetica", 3, 16));
			FontMetrics fm = g.getFontMetrics();
			int width = fm.stringWidth(sysName);
			g.setFont(new Font("Helvetica", 0, 14));
			FontMetrics fm2 = g.getFontMetrics();
			int width2 = fm2.stringWidth("Daily Energy: " + df.format(windinterface2b_openei.dayEnergy) + " (KWatt-Hrs)");
			int width3 = fm2.stringWidth("Total Energy: " + df.format(windinterface2b_openei.totEnergy) + " (KWatt-Hrs)");



			Graphics2D g2d = (Graphics2D)g;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			Image img0top = Toolkit.getDefaultToolkit().getImage(path + "TitleBack1.png");
			Image img0bot = Toolkit.getDefaultToolkit().getImage(path + "TitleBack1.png");
			g2d.drawImage(img0top, 5, 5, width + 25, 30, this);
			g2d.drawImage(img0bot, 20, 190, width2 + 25, 30, this);
			g2d.drawImage(img0bot, 270, 190, width3 + 25, 30, this);


			Image img0 = Toolkit.getDefaultToolkit().getImage(path + "frontTransP.png");
			int imgWidth0 = img0.getWidth(this);
			int imgHeight0 = img0.getHeight(this);

			g2d.drawImage(img0, 0, 20, imgWidth0 + 50, imgHeight0, this);


			Image img0a = Toolkit.getDefaultToolkit().getImage(path + "close.png");
			int imgWidth0a = img0a.getWidth(this);
			int imgHeight0a = img0a.getHeight(this);

			g2d.drawImage(img0a, -5, -3, imgWidth0a / 2, imgHeight0a / 2, this);

			Image img0b = Toolkit.getDefaultToolkit().getImage(path + "prefImg.png");
			int imgWidth0b = img0b.getWidth(this);
			int imgHeight0b = img0b.getHeight(this);
			g2d.drawImage(img0b, 0, 30, imgWidth0b / 2, imgHeight0b / 2, this);




			Image img1Off = Toolkit.getDefaultToolkit().getImage(path + "skystreamm-trans.png");
			Image img1On = Toolkit.getDefaultToolkit().getImage(path + "skystreamm-trans2.png");
			Image img1 = img1Off;
			if (windinterface2b_openei.speed > 0.0D) {
				img1 = img1On;
			} else {
				img1 = img1Off;
			}
			g2d.drawImage(img1, imgWidth0 - 60, 0, 170, imgHeight0 + 10, this);

			Image imgOff = Toolkit.getDefaultToolkit().getImage(path + "GaugeOff.png");
			Image imgOn = Toolkit.getDefaultToolkit().getImage(path + "GaugeOn.png");
			Image imgWarn = Toolkit.getDefaultToolkit().getImage(path + "GaugeWarning.png");
			Image imgCrit = Toolkit.getDefaultToolkit().getImage(path + "GaugeCritical.png");

			Image img2 = imgOff;

			double rw = windinterface2b_openei.wind * windinterface2b_openei.windConvert;
			if (rw == 0.0D) {
				img2 = imgOff;
			} else if ((rw > 0.0D) && (rw <= 180.0D)) {
				img2 = imgOn;
			} else if ((rw > 180.0D) && (rw < 270.0D)) {
				img2 = imgWarn;
			} else if (rw >= 270.0D) {
				img2 = imgCrit;
			}
			g2d.drawImage(img2, 15, 85, 110, 110, this);

			Image img3 = imgOff;

			double rs = windinterface2b_openei.speed * windinterface2b_openei.speedConvert;
			if (rs == 0.0D) {
				img3 = imgOff;
			} else if ((rs > 0.0D) && (rs <= 180.0D)) {
				img3 = imgOn;
			} else if ((rs > 180.0D) && (rs < 270.0D)) {
				img3 = imgWarn;
			} else if (rs >= 270.0D) {
				img3 = imgCrit;
			}
			g2d.drawImage(img3, 125, 85, 110, 110, this);

			Image img4 = imgOff;

			double rp = windinterface2b_openei.power * windinterface2b_openei.powerConvert;
			if (rp == 0.0D) {
				img4 = imgOff;
			} else if ((rp > 0.0D) && (rp <= 180.0D)) {
				img4 = imgOn;
			} else if ((rp > 180.0D) && (rp < 270.0D)) {
				img4 = imgWarn;
			} else if (rp >= 270.0D) {
				img4 = imgCrit;
			}
			g2d.drawImage(img4, 260, 58, 140, 140, this);

			Image imgVOff = Toolkit.getDefaultToolkit().getImage(path + "VerticalOff.png");
			Image imgVOn = Toolkit.getDefaultToolkit().getImage(path + "VerticalOn.png");
			Image imgVWarn = Toolkit.getDefaultToolkit().getImage(path + "VerticalWarning.png");
			Image imgVCrit = Toolkit.getDefaultToolkit().getImage(path + "VerticalCritical.png");

			Image img5 = imgVOff;
			for (int i = 0; i <= 10; i++) {
				g2d.drawImage(imgVOff, 400, 155 - i * 10, this);
			}
			double v = Double.parseDouble(windinterface2b_openei.volts) * windinterface2b_openei.voltsConvert;
			if (v == 0.0D) {
				img5 = imgVOff;
			} else if ((v > 0.0D) && (v <= 3.0D)) {
				img5 = imgVOn;
			} else if ((v > 3.0D) && (v < 8.0D)) {
				img5 = imgVWarn;
			} else if (v >= 8.0D) {
				img5 = imgVCrit;
			}
			for (int i = 0; i <= v; i++) {
				g2d.drawImage(img5, 400, 155 - i * 10, this);
			}
			Image img6 = imgVOff;
			for (int i = 0; i <= 8; i++) {
				g2d.drawImage(imgVOff, 240, 155 - i * 10, this);
			}
			double de = windinterface2b_openei.dayEnergy * windinterface2b_openei.dayEnergyConvert;
			if (de == 0.0D) {
				img6 = imgVOff;
			} else if ((de > 0.0D) && (de <= 3.0D)) {
				img6 = imgVOn;
			} else if ((de > 3.0D) && (de < 6.0D)) {
				img6 = imgVWarn;
			} else if (de >= 6.0D) {
				img6 = imgVCrit;
			}
			for (int i = 0; i <= de; i++) {
				g2d.drawImage(img6, 240, 155 - i * 10, this);
			}
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			env.getAvailableFontFamilyNames();



			g.setFont(new Font("Helvetica", 3, 16));

			g2d.setPaint(Color.LIGHT_GRAY);


			g2d.drawString(sysName, 17, 26);
			g2d.setPaint(Color.black);
			g2d.drawString(sysName, 15, 25);


			g.setFont(new Font("Helvetica", 0, 14));
			g2d.setPaint(Color.LIGHT_GRAY);
			g2d.drawString("Daily Energy: " + df.format(windinterface2b_openei.dayEnergy) + " (KWatt-Hrs)", 37, 211);
			g2d.drawString("Total Energy: " + df.format(windinterface2b_openei.totEnergy) + " (KWatt-Hrs)", 282, 211);
			g2d.drawString("Wind Speed", 36, 71);
			g2d.drawString("(m/s)", 51, 86);

			g2d.drawString("Turbine Speed", 136, 71);
			g2d.drawString("(RPM)", 151, 86);
			g2d.drawString("Power (Watts)", 281, 61);
			g2d.drawString("Volts", 391, 196);
			g2d.drawString("KWatt/Hrs", 226, 196);
			g2d.setPaint(Color.black);
			g2d.drawString("Wind Speed", 35, 70);
			g2d.drawString("(m/s)", 50, 85);

			g2d.drawString("Turbine Speed", 135, 70);
			g2d.drawString("(RPM)", 150, 85);
			g2d.drawString("Power (Watts)", 280, 60);
			g2d.drawString("Daily Energy: " + df.format(windinterface2b_openei.dayEnergy) + " (KWatt-Hrs)", 35, 210);
			g2d.drawString("Total Energy: " + df.format(windinterface2b_openei.totEnergy) + " (KWatt-Hrs)", 280, 210);
			g2d.drawString("Volts", 390, 195);
			g2d.drawString("KWatt/Hrs", 225, 195);

			setFont(new Font("Helvetica", 0, 14));
			g2d.setPaint(Color.white);

			String sysDate = windinterface2b_openei.now("hh:mm:ss a MM/dd/yyyy z");
			g2d.drawString(sysDate, 20, 50);

			g2d.drawString("T:" + windinterface2b_openei.ts + " S:" + windinterface2b_openei.ss + " G:" + windinterface2b_openei.gs + " Status", 240, 48);
			g2d.drawString(windinterface2b_openei.volts, 392, 180);



			g2d.drawString(df.format(windinterface2b_openei.wind), 55, 180);
			g2d.drawString(df.format(windinterface2b_openei.speed), 163, 180);
			g2d.drawString(df.format(windinterface2b_openei.power), 310, 180);
			g2d.drawString(df.format(windinterface2b_openei.dayEnergy), 230, 180);


			AffineTransform tx = new AffineTransform();

			double pi = 3.141592653589793D;
			if (rw < 30.0D) {
				rw = 30.0D;
			}
			if (rw > 330.0D) {
				rw = 330.0D;
			}
			tx.scale(0.3D, 0.3D);
			tx.translate(200.0D, 440.0D);


			ImageIcon dial = new ImageIcon(path + "myGaugePointer2.png");
			int iconX = 0;
			int iconY = 0;
			tx.rotate(rw * pi / 180.0D, 25.0D, 22.0D);
			g2d.setTransform(tx);
			dial.paintIcon(this, g, iconX, iconY);
			if (rs < 30.0D) {
				rs = 30.0D;
			}
			if (rs > 330.0D) {
				rs = 330.0D;
			}
			tx.setToRotation(0.0D);
			tx.setToTranslation(0.0D, 0.0D);
			tx.scale(0.3D, 0.3D);
			tx.translate(570.0D, 440.0D);

			tx.rotate(rs * pi / 180.0D, 25.0D, 22.0D);
			g2d.setTransform(tx);
			dial.paintIcon(this, g, iconX, iconY);
			if (rp < 30.0D) {
				rp = 30.0D;
			}
			if (rp > 330.0D) {
				rp = 330.0D;
			}
			tx.setToRotation(0.0D);
			tx.setToTranslation(0.0D, 0.0D);

			tx.scale(0.4D, 0.4D);
			tx.translate(801.0D, 295.0D);

			tx.rotate(rp * pi / 180.0D, 25.0D, 22.0D);
			g2d.setTransform(tx);
			dial.paintIcon(this, g, iconX, iconY);
		}
	}

	public class ComponentMover
	extends MouseAdapter
	{
		private Class<?> destinationClass;
		private Component destinationComponent;
		private Component destination;
		private Component source;
		private boolean changeCursor = true;
		private Point pressed;
		private Point location;
		private Cursor originalCursor;
		private boolean autoscrolls;
		private Insets dragInsets = new Insets(0, 0, 0, 0);
		private Dimension snapSize = new Dimension(1, 1);

		public ComponentMover() {}

		public ComponentMover(Class<?> destinationClass, Component... components)
		{
			this.destinationClass = destinationClass;
			registerComponent(components);
		}

		public ComponentMover(Component destinationComponent, Component... components)
		{
			this.destinationComponent = destinationComponent;
			registerComponent(components);
		}

		public boolean isChangeCursor()
		{
			return this.changeCursor;
		}

		public void setChangeCursor(boolean changeCursor)
		{
			this.changeCursor = changeCursor;
		}

		public Insets getDragInsets()
		{
			return this.dragInsets;
		}

		public void setDragInsets(Insets dragInsets)
		{
			this.dragInsets = dragInsets;
		}

		public void deregisterComponent(Component... components)
		{
			for (Component component : components) {
				component.removeMouseListener(this);
			}
		}

		public void registerComponent(Component... components)
		{
			for (Component component : components) {
				component.addMouseListener(this);
			}
		}

		public Dimension getSnapSize()
		{
			return this.snapSize;
		}

		public void setSnapSize(Dimension snapSize)
		{
			this.snapSize = snapSize;
		}

		public void mousePressed(MouseEvent e)
		{
			this.source = e.getComponent();
			int width = this.source.getSize().width - this.dragInsets.left - this.dragInsets.right;
			int height = this.source.getSize().height - this.dragInsets.top - this.dragInsets.bottom;
			Rectangle r = new Rectangle(this.dragInsets.left, this.dragInsets.top, width, height);
			if (r.contains(e.getPoint())) {
				setupForDragging(e);
			}
		}

		public void mouseClicked(MouseEvent e)
		{
			int deltaX = (int)this.pressed.getX() - (int)this.location.getX() - 10;
			int deltaY = (int)this.pressed.getY() - (int)this.location.getY() - 10;
			if ((deltaX >= -10) && (deltaX < 10) && (deltaY >= -10) && (deltaY < 10))
			{
				int reply = JOptionPane.showConfirmDialog(null, "Do You Wish To Exit?", "Quit", 0);
				if (reply == 0) {
					System.exit(0);
				}
			}
			if ((deltaX >= -10) && (deltaX < 10) && (deltaY >= 20) && (deltaY < 40))
			{
				int reply = JOptionPane.showConfirmDialog(null, "Do You Wish Edit Settings?", "Edit Settings?", 0);
				if (reply == 0)
				{
					windinterface2b_openei.task2Suspend = true;
					windinterface2b_openei.this.simpleForm();
				}
			}
		}

		private void setupForDragging(MouseEvent e)
		{
			this.source.addMouseMotionListener(this);
			if (this.destinationComponent != null) {
				this.destination = this.destinationComponent;
			} else if (this.destinationClass == null) {
				this.destination = this.source;
			} else {
				this.destination = SwingUtilities.getAncestorOfClass(this.destinationClass, this.source);
			}
			this.pressed = e.getLocationOnScreen();
			this.location = this.destination.getLocation();
			if (this.changeCursor)
			{
				this.originalCursor = this.source.getCursor();
				this.source.setCursor(Cursor.getPredefinedCursor(13));
			}
			if ((this.destination instanceof JComponent))
			{
				JComponent jc = (JComponent)this.destination;
				this.autoscrolls = jc.getAutoscrolls();
				jc.setAutoscrolls(false);
			}
		}

		public void mouseDragged(MouseEvent e)
		{
			Point dragged = e.getLocationOnScreen();
			int dragX = getDragDistance(dragged.x, this.pressed.x, this.snapSize.width);
			int dragY = getDragDistance(dragged.y, this.pressed.y, this.snapSize.height);
			this.destination.setLocation(this.location.x + dragX, this.location.y + dragY);
		}

		private int getDragDistance(int larger, int smaller, int snapSize)
		{
			int halfway = snapSize / 2;
			int drag = larger - smaller;
			drag += (drag < 0 ? -halfway : halfway);
			drag = drag / snapSize * snapSize;

			return drag;
		}

		public void mouseReleased(MouseEvent e)
		{
			this.source.removeMouseMotionListener(this);
			if (this.changeCursor) {
				this.source.setCursor(this.originalCursor);
			}
			if ((this.destination instanceof JComponent)) {
				((JComponent)this.destination).setAutoscrolls(this.autoscrolls);
			}
		}
	}
}
