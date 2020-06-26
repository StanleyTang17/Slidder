package game.gameobject;

public class ExitDoor extends GameObject implements PlayerCollisionListener {
	
	public ExitDoor(double x, double y) {
		super(x, y);
		this.initSoundEffect();
	}

	@Override
	public void onCollision(Player p) {
		if(p.isPressingDown()) {
			p.setEscaped(true);
			soundEffect.play();
		}
	}
	
}
