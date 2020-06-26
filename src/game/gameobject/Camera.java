package game.gameobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import game.Level;
import game.utility.Point;
import main.GameSystem;

public class Camera extends GameObject {
	
	private Level level;
	private double zoomFactor;
	private double zoomWallSize;
	private int rows, cols;
	private Rectangle bound;
	private Player player = null;
	
	public Camera(Level level, double zoomFactor) {
		super(0, 0, (int)(GameSystem.SCREEN_WIDTH / zoomFactor), (int)(GameSystem.SCREEN_HEIGHT / zoomFactor));
		this.level = level;
		this.zoomFactor = zoomFactor;
		player = level.getPlayer();
		bound = new Rectangle((int)(x + width/4), (int)(y + height/4), (int)(width / 2), (int)(height / 2));
		zoomWallSize = zoomFactor * level.getWallSize();
		rows = (int)(GameSystem.SCREEN_HEIGHT / zoomWallSize) + 1;
		cols = (int)(GameSystem.SCREEN_WIDTH / zoomWallSize) + 1;
		this.setPhysics(false);
	}

	@Override
	protected void onUpdate() {
		if(player != null) {
			
			double px = player.x;
			double py = player.y;
			if(px < bound.x) {
				bound.x = player.getIntX();
			}else if(px + player.width > bound.x + bound.width) {
				bound.x = (int)(px + player.width - bound.width);
			}
			if(py < bound.y) {
				bound.y = player.getIntY();
			}else if(py + player.height > bound.y + bound.height) {
				bound.y = (int)(py + player.height - bound.height);
			}
			x = bound.getX() - width/4;
			y = bound.getY() - width/4;
			
		}
		
	}

	@Override
	protected void onRender(Graphics2D g) {
		int ws = level.getWallSize();
		Point offset;
		
		if(zoomFactor > 1.2) {
			//if the level is small enough to fit on the screen, then draw it like in overview mode
			offset = level.getGraphicOffset();
			g.translate(offset.getX(), offset.getY());
			g.scale(zoomFactor, zoomFactor);
			
			g.setColor(Color.BLACK);
			int[][] walls = level.getWalls();
			for(int i = 0; i < walls.length; ++i) {
				for(int j = 0; j < walls[0].length; ++j) {
					g.setColor(Color.DARK_GRAY);
					if(walls[i][j] == 1) {
						g.fillRect(j * ws, i * ws, ws, ws);
					}
				}
			}
			
			for(GameObject obj : level.getObjectList()) obj.render(g);
			
			g.scale(1/zoomFactor, 1/zoomFactor);
			g.translate(-offset.getX(), -offset.getY());
			
		}else {
			//if the level is too big to fit on the screen, then draw the zoomed view
			Point topLeft = level.getGridPos(x, y);
			int r1 = topLeft.getIntX();
			int r2 = r1 + rows;
			int c1 = topLeft.getIntY();
			int c2 = c1 + cols;
			offset = new Point(x - c1 * ws, y - r1 * ws);
			g.scale(zoomFactor, zoomFactor);
			g.setColor(Color.DARK_GRAY);
			for(int i = r1; i <= r2; ++i) {
				for(int j = c1; j <= c2; ++j) {
					if(i < 0 || j < 0 || i >= level.getTotalRows() || j >= level.getTotalCols()) continue;
					int id = level.getWalls()[i][j];
					if(id > 0) {
						g.fillRect((int)((j - c1) * ws - offset.getX()), (int)((i - r1) * ws - offset.getY()), (int)ws, (int)ws);
					}
				}
			}
			
			for(GameObject obj : level.getObjectList())
				if(this.collidesWith(obj))
					drawObject(obj, g);
			
			g.scale(1/zoomFactor, 1/zoomFactor);
		}
	}
	
	private void drawObject(GameObject obj, Graphics2D g) {
		double dx = -obj.x + (obj.x - this.x);
		double dy = -obj.y + (obj.y - this.y);
		g.translate(dx, dy);
		obj.render(g);
		g.translate(-dx, -dy);
	}
	
	public Rectangle getBound() { return bound; }
}
