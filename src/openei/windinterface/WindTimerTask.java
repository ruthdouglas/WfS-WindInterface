package openei.windinterface;

import java.util.TimerTask;

class WindTimerTask extends TimerTask {
<<<<<<< HEAD
	private WindTurbine[] turbines;

	/**
	 * Task to pull data from Wind Turbines.
	 */
	public WindTimerTask() {
	}

	/**
	 * @param turbineList
	 *            List of WindTurbines to pull data from. Initialization method
	 *            to pull in list of WindTurbines and start the timer.
	 */
	public void init(WindTurbine[] turbineList) {
		turbines = turbineList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		for (int i = 0; i < turbines.length; i++)
			turbines[i].timerrun();
=======
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
>>>>>>> e2369ab1c56cc94a62d696a96d2cc83d54ba214c
	}
}