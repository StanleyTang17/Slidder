package game.gameobject.items;

import java.awt.Graphics2D;

import game.gameobject.GameObject;
import game.gameobject.Player;
import game.gameobject.PlayerCollisionListener;

public abstract class Item extends GameObject implements PlayerCollisionListener {
	
	protected boolean pickedUp = false;
	
	public Item(double x, double y) {
		super(x, y);
		this.setPhysics(false);
		this.initSoundEffect();
	}

	@Override
	protected void onUpdate() {
		
	}

	@Override
	protected void onRender(Graphics2D g) {
		
	}
	
	@Override
	public void onCollision(Player p) {
		if(p.isPressingDown() && !pickedUp) {
			this.onPickUp(p);
			p.getInventory().add(this.getClass());
			pickedUp = true;
			this.setEnabled(false);
			soundEffect.play();
		}
	}
	
	@Override
	protected void onReset() {
		pickedUp = false;
		this.setEnabled(true);
	}
	
	public abstract void onPickUp(Player p);
	
	@Override
	public boolean equals(Object obj) {
		return obj.getClass() == this.getClass();
	}
	
	public boolean isPickedUp() {
		return pickedUp;
	}
	
}
