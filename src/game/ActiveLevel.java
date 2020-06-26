package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;

import game.gameobject.*;
import game.utility.*;
import main.BasicFunctionality;
import main.GameSystem;

public class ActiveLevel extends Level implements BasicFunctionality, KeyListener {
	
	private int[][] originalLayout;
	private Camera camera;
	private ConcurrentHashMap<GameObject, Point> roomPos = new ConcurrentHashMap<>();
	private boolean zoom = false;
	private Rectangle wallHitbox = new Rectangle();
	private TimedAction timeOut = null;
	
	public ActiveLevel(File levelFile) throws NumberFormatException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		super(levelFile);
		
		//update each object's room position
		for(GameObject obj : objects)
			roomPos.put(obj, new Point(0, 0));
		this.updateRoomPos();
		
		camera = new Camera(this, graphicsScale > 1.2 ? graphicsScale : 1.2);
		
		//save layout
		originalLayout = new int[layout.length][layout[0].length];
		for(int i = 0; i < layout.length; ++i)
			for(int j = 0; j < layout[0].length; ++j)
				originalLayout[i][j] = layout[i][j];
		
		wallHitbox.width = wallSize;
		wallHitbox.height = wallSize;
		
		//setup timer if this level is timed
		if(duration > 0) {
			timeOut = new TimedAction(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					player.disable();
				}
			}, duration * 1000, false);
			timeOut.terminate();
		}
	}
	
	@Override
	public void update() {
		if(!player.isEnabled()) return;
		if(timeOut != null) timeOut.update();
		if(zoom && !player.hasEscaped()) {
			for(GameObject obj : objects) {
				obj.update();
				//check player collisions
				if(obj.isEnabled() && PlayerCollisionListener.class.isAssignableFrom(obj.getClass())) {
					if(obj.collidesWith(player))
						((PlayerCollisionListener) obj).onCollision(player);
				}
			}
			timedQueue.update();
			camera.update();
			if(player.hasEscaped()) {
				player.disable();
				if(timeOut != null && !timeOut.isDone()) timeOut.terminate();
			}
		}
	}

	@Override
	public void render(Graphics2D g) {
		if(zoom) {
			//zoom
			camera.render(g);
			if(player.hasEscaped()) {
				g.setFont(new Font("impact", Font.PLAIN, 80));
				g.setColor(Color.GREEN);
				Utility.drawCenteredString(g, "You have escaped!", -120);
				Utility.drawCenteredString(g, "Press [ENTER] to return to menu", 120);
			}else if(!player.isEnabled()) {
				g.setFont(new Font("impact", Font.PLAIN, 80));
				g.setColor(Color.RED);
				Utility.drawCenteredString(g, "You're dead!", -120);
				Utility.drawCenteredString(g, "Press [ENTER] to respawn", 120);
			}
		}else
			//overview
			this.defaultRender(g);
		
		//draw time left
		if(timeOut != null) {
			g.setFont(new Font("impact", Font.PLAIN, 36));
			g.setColor(Color.RED);
			g.drawString("" + timeOut.getTimeLeftSeconds(), 0, 36);
		}
	}
	
	private void slideRoom(int dx, int dy) {
		//check if GameObjects are caught in between rooms
		if(!this.updateRoomPos()) return;
		
		int old_row = emptySpot.getIntX();
		int old_col = emptySpot.getIntY();
		int new_row = old_row - dy;
		int new_col = old_col - dx;
		
		//check if it's possible to slide the room
		if(new_row < 0 || new_col < 0 || new_row >= layout.length || new_col >= layout[0].length) return;
		
		//slide the room
		emptySpot.setX(new_row);
		emptySpot.setY(new_col);
		layout[old_row][old_col] = layout[new_row][new_col];
		layout[new_row][new_col] = -1;
		
		//slide the objects in the room as well
		Iterator<GameObject> it = objects.iterator();
		while(it.hasNext()) {
			GameObject obj = it.next();
			Point pos = roomPos.get(obj);
			if(pos.getIntX() == new_row && pos.getIntY() == new_col) {
				pos.setX(old_row);
				pos.setY(old_col);
				obj.setX(obj.getX() + dx * roomWidth);
				obj.setY(obj.getY() + dy * roomHeight);
			}
		}
		
		this.reconstructWalls();
	}
	
	private boolean updateRoomPos() {
		boolean validRoomPos = true;
		Iterator<GameObject> it = objects.iterator();
		while(it.hasNext()) {
			GameObject obj = it.next();
			double x = obj.getX(), y = obj.getY(), w = obj.getWidth(), h = obj.getHeight();
			
			//four corners of the object's hitbox
			int roomTopLeft = this.getRoomPos(x, y);
			int roomTopRight = this.getRoomPos(x + w, y);
			int roomBottomLeft = this.getRoomPos(x, y + h);
			int roomBottomRight = this.getRoomPos(x + w, y + h);
			
			//check if all four corners are in the same room
			if(roomTopLeft == roomTopRight && roomTopRight == roomBottomLeft && roomBottomLeft == roomBottomRight) {
				int col = (int)(x / roomWidth);
				int row = (int)(y / roomHeight);
				if(layout[row][col] == -1)
					validRoomPos = false;
				roomPos.get(obj).setX(row);
				roomPos.get(obj).setY(col);
			}else {
				roomPos.get(obj).setX(-1);
				roomPos.get(obj).setY(-1);
				validRoomPos = false;
			}
		}
		return validRoomPos;
	}
	
	@Override
	public void addGameObject(GameObject object) {
		Point pos = new Point((int)(object.getY() / getRoomHeight()), (int)(object.getX() / getRoomWidth()));
		roomPos.put(object, pos);
		objects.add(object);
	}
	
	@Override
	public void removeGameObject(GameObject object) {
		objects.remove(object);
		roomPos.remove(object);
	}
	
	public boolean collidesWithWall(GameObject obj) {
		//the grid where the top left corner of the object is located
		int row1 = obj.getIntY() / wallSize;
		int col1 = obj.getIntX() / wallSize;
		
		//the grid where the bottom right corner of the object is located
		int row2 = (obj.getIntY() + obj.getHeight()) / wallSize;
		int col2 = (obj.getIntX() + obj.getWidth()) / wallSize;
		
		//check, from top left to bottom right, if any grid is a solid wall
		for(int row = row1; row <= row2; ++row)
			if(row > -1 && row < totalRows)
				for(int col = col1; col <= col2; ++col)
					if(col > -1 && col < totalCols && walls[row][col] == 1) {
						wallHitbox.x = col * wallSize;
						wallHitbox.y = row * wallSize;
						if(obj.getHitbox().intersects(wallHitbox))
							return true;
					}
		
		return false;
	}
	
	public void restart() {
		//reset layout
		for(int i = 0; i < originalLayout.length; ++i)
			for(int j = 0; j < originalLayout[0].length; ++j) {
				layout[i][j] = originalLayout[i][j];
				if(originalLayout[i][j] == -1) {
					emptySpot.setX(i);
					emptySpot.setY(j);
				}
			}
		this.reconstructWalls();
		
		Iterator<GameObject> it = objects.iterator();
		while(it.hasNext()) it.next().reset();
		if(timeOut != null) timeOut.terminate();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		player.keyPressed(e);
		if(player.isEnabled()) {
			//start the timer when any relevant keys are pressed
			if(key == KeyEvent.VK_ENTER ||
			   key == KeyEvent.VK_UP ||
			   key == KeyEvent.VK_DOWN ||
			   key == KeyEvent.VK_LEFT ||
			   key == KeyEvent.VK_RIGHT)
				if(timeOut != null && timeOut.isDone()) timeOut.reset();
			
			//swap modes
			if(key == KeyEvent.VK_ENTER) {
				if(zoom == true) {
					zoom = false;
					timedQueue.pause();
				}else {
					zoom = true;
					timedQueue.unpause();
				}
			}
			
			//slide rooms
			if(!zoom) {
				if(key == KeyEvent.VK_UP) {
					this.slideRoom(0, -1);
				}else if(key == KeyEvent.VK_DOWN) {
					this.slideRoom(0, 1);
				}else if(key == KeyEvent.VK_LEFT) {
					this.slideRoom(-1, 0);
				}else if(key == KeyEvent.VK_RIGHT) {
					this.slideRoom(1, 0);
				}else if(key == KeyEvent.VK_ESCAPE) {
					GameSystem.setCurrentScreen("menu");
				}
			}
		}else if(key == KeyEvent.VK_ENTER) {
			if(player.hasEscaped())
				GameSystem.setCurrentScreen("menu");
			else
				this.restart();
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		player.keyReleased(e);
	}
	
	//a single getter
	public boolean isZoomed() {
		return zoom;
	}
}
