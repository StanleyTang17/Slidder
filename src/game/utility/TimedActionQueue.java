package game.utility;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

import main.BasicFunctionality;

public class TimedActionQueue implements BasicFunctionality {
	
	private ConcurrentLinkedDeque<TimedAction> actionList = new ConcurrentLinkedDeque<>();
	private boolean paused = false;
	private long pauseTime = 0;

	@Override
	public void update() {
		if(!paused) {
			Iterator<TimedAction> iter = actionList.iterator();
			while(iter.hasNext()) {
				TimedAction action = iter.next();
				action.update();
				if(action.isDone()) iter.remove();
			}
		}
	}

	@Override
	public void render(Graphics2D g) {
		
	}
	
	public void pause() {
		paused = true;
		pauseTime = System.currentTimeMillis();
	}
	
	public void unpause() {
		paused = false;
		Iterator<TimedAction> iter = actionList.iterator();
		while(iter.hasNext())
			iter.next().startTime += System.currentTimeMillis() - pauseTime;
	}
	
	public void add(TimedAction action) {
		action.reset();
		actionList.add(action);
	}
	
	public void remove(TimedAction action) {
		actionList.remove(action);
	}
}
