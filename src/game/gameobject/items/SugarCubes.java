package game.gameobject.items;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import game.gameobject.Player;
import game.utility.TimedAction;
import main.GameSystem;

public class SugarCubes extends Item {
	
	private static final double speedBoost = 1;
	private static TimedAction removeEffect = new TimedAction(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Player p = GameSystem.getLevel().getPlayer();
					p.setSpeed(1.25);
					p.getInventory().remove(SugarCubes.class);
				}
			}, 8000, false);
	
	public SugarCubes(double x, double y) {
		super(x, y);
		removeEffect.terminate();
	}
	
	@Override
	public void onPickUp(Player player) {
		if(!removeEffect.isDone()) {
			removeEffect.reset();
		}else {
			player.setSpeed(player.getSpeed() + speedBoost);
			GameSystem.addTimedAction(removeEffect);
		}
	}
	
	@Override
	protected void onReset() {
		if(!removeEffect.isDone())
			removeEffect.setOff();
		pickedUp = false;
		this.setEnabled(true);
	}
}
