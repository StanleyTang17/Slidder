package game.utility;

import java.awt.Graphics2D;
import java.awt.event.ActionListener;

import main.BasicFunctionality;

public class TimedAction implements BasicFunctionality {
	
	private ActionListener action;
	private int delay;
	private boolean repeat;
	private boolean done = false;
	protected long startTime;
	
	public TimedAction(ActionListener action, int delay, boolean repeat) {
		this.action = action;
		this.delay = delay;
		this.repeat = repeat;
		startTime = System.currentTimeMillis();
	}
	
	public boolean isDone() {
		return done;
	}
	
	@Override
	public void update() {
		if(done) return;
		if(System.currentTimeMillis() >= startTime + delay)
			this.setOff();
	}

	@Override
	public void render(Graphics2D g) {
		
	}
	
	public void reset() {
		startTime = System.currentTimeMillis();
		done = false;
	}
	
	public void setOff() {
		action.actionPerformed(null);
		if(!repeat)
			done = true;
		else
			startTime = System.currentTimeMillis();
	}
	
	public void terminate() {
		done = true;
	}
	
	public int getTimeLeftSeconds() {
		if(done)
			return delay / 1000;
		else
			return (delay - (int)(System.currentTimeMillis() - startTime)) / 1000;
	}
}
