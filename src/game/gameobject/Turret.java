package game.gameobject;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Color;

import game.*;
import game.utility.Point;
import game.utility.TimedAction;
import game.utility.Utility;
import main.GameSystem;

public class Turret extends GameObject implements ActionListener {
	
	private boolean blocked = true;
	private Point center;
	private Point playerCenter;
	private TimedAction shootingAction;
	private double bulletSpeed = 2;
	
	public Turret(double x, double y) {
		super(x, y);
		this.setPhysics(false);
		this.initSoundEffect();
		center = new Point(x + width / 2, y + height / 2);
		playerCenter = new Point(0, 0);
		shootingAction = new TimedAction(this, 1500, true);
		GameSystem.addTimedAction(shootingAction);
	}
	
	@Override
	protected void onUpdate() {
		Level l = GameSystem.getLevel();
		Player p = l.getPlayer();
		int[][] walls = l.getWalls();
		int ws = l.getWallSize();
		
		center.setX(x + width / 2);
		center.setY(y + height / 2);
		playerCenter.setX(p.x + p.width / 2);
		playerCenter.setY(p.y + p.height / 2);
		
		blocked = false;
		Rectangle wall = new Rectangle();
		
		int row1 = this.getIntY() / ws;
		int col1 = this.getIntX() / ws;
		int row2 = p.getIntY() / ws;
		int col2 = p.getIntX() / ws;
		
		int minRow = Math.max(0, Math.min(row1, row2));
		int maxRow = Math.min(l.getTotalRows()-1, Math.max(row1, row2));
		int minCol = Math.max(0, Math.min(col1, col2));
		int maxCol = Math.min(l.getTotalCols()-1, Math.max(col1, col2));
		
		//check if line of sight is blocked by a wall
		for(int row = minRow; row <= maxRow; ++row)
			for(int col = minCol; col <= maxCol; ++col)
				if(walls[row][col] == 1) {
					//reset wall hitbox
					wall.height = ws;
					wall.width = ws;
					wall.x = col * ws;
					wall.y = row * ws;
					
					//include adjacent walls as well for better precision
					if(row + 1 < walls.length && walls[row + 1][col] == 1) {
						wall.height += ws;
					}
					if(row - 1 > -1 && walls[row - 1][col] == 1) {
						wall.height += ws;
						wall.y -= ws;
					}
					if(col + 1 < walls[row].length && walls[row][col + 1] == 1) {
						wall.width += ws;
					}
					if(col - 1 > -1 && walls[row][col - 1] == 1) {
						wall.width += ws;
						wall.x -= ws;
					}
					
					if(Utility.checkLineCollision(center, playerCenter, wall)) {
						blocked = true;
						break;
					}
				}
		
		if(blocked)
			shootingAction.reset();
	}

	@Override
	protected void onRender(Graphics2D g) {
		if(!blocked) {
			if(GameSystem.getLevel() == null) return;
			Player p = GameSystem.getLevel().getPlayer();
			if(p == null) return;
			g.setColor(Color.RED);
			g.drawLine((int)(x + width / 2), (int)(y + height / 2), (int)(p.x + p.width / 2), (int)(p.y + p.height / 2));
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Bullet b = new Bullet(center.getX(), center.getY());
		b.x -= b.width / 2;
		b.y -= b.height / 2;
		
		double angle = Math.atan2(
				playerCenter.getX() - center.getX(),
				-(playerCenter.getY() - center.getY())
				) - Math.PI / 2;
		
		b.velx = bulletSpeed * Math.cos(angle);
		b.vely = bulletSpeed * Math.sin(angle);
		GameSystem.getLevel().addGameObject(b);
		
		soundEffect.play();
	}

}
