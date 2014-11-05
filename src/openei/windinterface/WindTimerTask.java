package openei.windinterface;
import java.util.TimerTask;

class WindTimerTask extends TimerTask {
  private WindTurbine[] turbines;
  /**
 * 
 */
public WindTimerTask () {}
  /**
 * @param turbineList
 */
public void init(WindTurbine[] turbineList) {
    turbines = turbineList;
  }
  /* (non-Javadoc)
 * @see java.util.TimerTask#run()
 */
public void run() {
	  for (int i = 0; i < turbines.length; i++)
			turbines[i].timerrun();
  }
}