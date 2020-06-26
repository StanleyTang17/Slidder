package game.gameobject;

import java.awt.Rectangle;

import game.*;
import main.GameSystem;

public class Stalactite extends Spike {
	
	private Rectangle sight;
	
	public Stalactite(double x, double y) {
		super(x, y);
		sight = new Rectangle();
	}
	
	@Override
	protected void onUpdate() {
		ActiveLevel l = GameSystem.getActiveLevel();
		if(l.collidesWithWall(this))
			this.disable();
		
		int[][] walls = l.getWalls();
		int ws = l.getWallSize();
		
		//check if player is directly below
		sight.x = getIntX();
		sight.y = getIntY();
		sight.width = width;
		sight.height = 0;
		
		int row = getIntY() / ws;
		int col = getIntX() / ws;
		while(row <= l.getTotalRows() && walls[row][col] == 0) {
			row++;
			sight.height += ws;
		}
		
		if(sight.intersects(l.getPlayer().getHitbox()))
			this.setPhysics(true);
	}
	
	@Override
	protected void checkWallCollision() {
		
	}
	
	@Override
	public void onReset() {
		this.setPhysics(false);
	}
	
}
