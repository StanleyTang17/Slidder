package game.gameobject;

import game.Level;
import game.ActiveLevel;
import main.GameSystem;

public class Bullet extends GameObject implements PlayerCollisionListener {

	public Bullet(double x, double y) {
		super(x, y);
		this.setPhysics(false);
		this.setBoundable(false);
	}
	
	@Override
	public void onUpdate() {
		Level level = GameSystem.getLevel();
		//disappear if this hits a wall
		if(x < 0)
			this.removeSelf();
		else if(x + width > level.getWidth())
			this.removeSelf();
		if(y < 0)
			this.removeSelf();
		else if(y + height > level.getHeight())
			this.removeSelf();
		if(((ActiveLevel)GameSystem.getLevel()).collidesWithWall(this)) {
			this.removeSelf();
		}
	}
	
	@Override
	public void onReset() {
		this.removeSelf();
	}
	
	@Override
	public void onCollision(Player p) {
		p.disable();
		p.soundEffect.play();
		this.removeSelf();
	}

}
