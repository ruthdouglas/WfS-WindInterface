package openei.windinterface;
import java.util.TimerTask;

class WindTimerTask extends TimerTask {
	private WindTurbine turbine;
	/**
	 * Task to pull data from Wind Turbines.
	 */
	public WindTimerTask () {}
	/**
	 * @param turbineList List of WindTurbines to pull data from.
	 * Initialization method to pull in list of WindTurbines and start the timer.
	 */
	public void init(WindTurbine turbinea) {
		turbine = turbinea;
	}
	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		turbine.timerrun();
	}
}