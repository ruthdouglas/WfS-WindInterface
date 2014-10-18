
public class WindTurbine {
	String[] inData = { "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0", "0" };
	String[][] avgData = new String[20][40];
	double[] tenMinAvgData = { 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D };
	double myDailyTotal = 0.0D;
	String mySysTitle;
	String mySysID;
	String mySerialNum;
	String mySysName;
	String myApiKey;
	String myPowerOffset;
	double power = 0.0D;
	double wind = 0.0D;
	double speed = 0.0D;
	String ts = "0000";
	String ss = "0000";
	String gs = "0000";
	double dayEnergy = 35.0D;
	double totEnergy = 10000.0D;
	String volts = "250";
	public WindTurbine(String SystemTitle, String SystemName, String SystemID, String SerialNum, String APIKey, double PowerOffset) {
		
		
		System.out.println("Loaded Wind Turbine: " + SystemTitle);
	}
}
