import java.util.TimerTask;

class WindTimerTask extends TimerTask {
  protected WindTurbine[] turbines;
  public WindTimerTask () {}
  public void init(WindTurbine[] t) {
    turbines = t;
  }
  public void run() {
	  for (int i = 0; i < turbines.length; i++)
			turbines[i].timerrun();
  }
}