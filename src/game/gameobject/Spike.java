package game.gameobject;

public class Spike extends GameObject implements PlayerCollisionListener{
	
	public Spike(double x, double y) {
		super(x, y);
		this.setPhysics(false);
		this.initSoundEffect();
	}

	@Override
	public void onCollision(Player p) {
		p.disable();
		soundEffect.play();
		p.soundEffect.play();
	}

}
