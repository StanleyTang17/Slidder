package game.gameobject;

import game.gameobject.items.ExitKey;

public class LockedExitDoor extends ExitDoor {
	
	public LockedExitDoor(double x, double y) {
		super(x, y);
	}

	@Override
	public void onCollision(Player p) {
		if(p.isPressingDown() && p.getInventory().contains(ExitKey.class)) {
			p.setEscaped(true);
			soundEffect.play();
		}
	}
}
