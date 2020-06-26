package game.gameobject;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

import game.gameobject.items.Item;
import main.GameSystem;

public class Player extends GameObject implements KeyListener {
	
	private double speed = 1.25;
	private double jumpSpeed = 5;
	private HashSet<Class<? extends Item>> inventory = new HashSet<>();
	private boolean leftPressed, rightPressed, downPressed;
	private boolean doubleJump = false;
	private boolean escaped = false;
	
	public Player(double x, double y) {
		super(x, y);
		this.setBoundable(false);
		this.initSoundEffect();
	}

	@Override
	protected void onUpdate() {
		if(!enabled) {
			airborne = false;
			doubleJump = false;
			return;
		}
		
		if(!airborne) doubleJump = false;
		
		if(leftPressed)
			velx = -speed;
		else if(rightPressed)
			velx = speed;
		else
			velx = 0;
		
		int levelWidth = GameSystem.getLevel().getWidth();
		if(x < 0)
			x = 0;
		else if(x + width > levelWidth)
			x = levelWidth - width;
		
		if(y > GameSystem.getLevel().getHeight()) {
			disable();
			soundEffect.play();
		}
	}
	
	@Override
	protected void onReset() {
		inventory.clear();
		escaped = false;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(!enabled) return;
		if(!GameSystem.getActiveLevel().isZoomed()) return;
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_RIGHT) {
			leftPressed = false;
			rightPressed = true;
		}else if(key == KeyEvent.VK_LEFT) {
			rightPressed = false;
			leftPressed = true;
		}else if(key == KeyEvent.VK_UP) {
			if(!airborne) {
				vely = -jumpSpeed;
				airborne = true;
			}else if(!doubleJump) {
				vely = -jumpSpeed;
				doubleJump = true;
			}
		}else if(key == KeyEvent.VK_DOWN) {
			downPressed = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_RIGHT) {
			rightPressed = false;
		}else if(key == KeyEvent.VK_LEFT) {
			leftPressed = false;
		}else if(key == KeyEvent.VK_DOWN) {
			downPressed = false;
		}
	}
	
	//getters
	public HashSet<Class<? extends Item>> getInventory() {
		return inventory;
	}
	
	public double getSpeed() {
		return speed;
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getJumpSpeed() {
		return jumpSpeed;
	}
	
	public void setJumpSpeed(double jumpSpeed) {
		this.jumpSpeed = jumpSpeed;
	}
	
	public boolean isPressingLeft() {
		return leftPressed;
	}
	
	public boolean isPressingRight() {
		return rightPressed;
	}
	
	public boolean isPressingDown() {
		return downPressed;
	}
	
	public boolean hasEscaped() {
		return escaped;
	}
	
	public void setEscaped(boolean escaped) {
		this.escaped = escaped;
	}
}
