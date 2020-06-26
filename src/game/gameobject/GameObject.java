package game.gameobject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import game.Level;
import game.SoundEffect;
import game.utility.Point;
import main.BasicFunctionality;
import main.GameSystem;

import java.awt.*;

public abstract class GameObject implements BasicFunctionality {
	
	protected double x, y, velx = 0, vely = 0;
	protected int width, height;
	protected boolean airborne = true;
	protected boolean physics = true;
	protected boolean boundable = true;
	protected boolean enabled = true;
	protected Rectangle hitbox;
	protected BufferedImage image;
	protected Point respawnPoint;
	protected SoundEffect soundEffect;
	
	private boolean hitLeft = false, hitRight = false;
	
	private static final double GRAVITY = -0.12;
	
	private static HashMap<Class<? extends GameObject>, BufferedImage> objImages = new HashMap<>();
	private static HashMap<Class<? extends GameObject>, SoundEffect> objSoundEffects = new HashMap<>();
	
	public static BufferedImage getImageByClass(Class<? extends GameObject> cls) {
		if(objImages.containsKey(cls))
			return objImages.get(cls);
		String className = cls.getName();
		String fileName = className.substring(className.lastIndexOf(".") + 1) + ".png";
		try {
			BufferedImage img = ImageIO.read(new File("res\\gfx\\" + fileName));
			objImages.put(cls, img);
			return img;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static SoundEffect getSoundEffectByClass(Class<? extends GameObject> cls) {
		if(objSoundEffects.containsKey(cls))
			return objSoundEffects.get(cls);
		String className = cls.getName();
		String fileName = className.substring(className.lastIndexOf(".") + 1) + ".wav";
		SoundEffect soundEffect = new SoundEffect(new File("res\\sound\\" + fileName));
		objSoundEffects.put(cls, soundEffect);
		return soundEffect;
	}
	
	public static GameObject parseObject(String objString) throws ClassNotFoundException, NoSuchMethodException, SecurityException, NumberFormatException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String[] tokens = objString.split(" ");
		Class<?> cls = Class.forName(tokens[0]);
		Class<?>[] argTypes = new Class[tokens.length-1];
		Object[] args = new Object[argTypes.length];
		for(int i = 0; i < argTypes.length; ++i) {
			String s = tokens[i+1];
			Class<?> type = null;
			Object arg = null;
			//checks if the String token is int, double, boolean, or String
			//sadly this was only used to parse doubles because there aren't
			//any objects with more parameters than x and y.
			try {
				arg = Integer.parseInt(s);
				type = int.class;
			}catch(NumberFormatException e) {
				try {
					arg = Double.parseDouble(s);
					type = double.class;
				}catch(NumberFormatException e2) {
					if(s.equals("true") || s.equals("false")) {
						arg = Boolean.parseBoolean(s);
						type = boolean.class;
					}else {
						arg = s;
						type = String.class;
					}
				}
			}
			argTypes[i] = type;
			args[i] = arg;
		}
		Constructor<?> cons = cls.getConstructor(argTypes);
		Object obj = cons.newInstance(args);
		return (GameObject) obj;
	}
	
	public GameObject(double x, double y, int width, int height) {
		this.x = x;
		this.y = y;
		respawnPoint = new Point(x, y);
		this.width = width;
		this.height = height;
		hitbox = new Rectangle(getIntX(), getIntY(), width, height);
	}
	
	public GameObject(double x, double y) {
		this.image = getImageByClass(this.getClass());
		this.x = x;
		this.y = y;
		respawnPoint = new Point(x, y);
		this.width = image.getWidth();
		this.height = image.getHeight();
		hitbox = new Rectangle(getIntX(), getIntY(), width, height);
	}
	
	//methods to be overriden
	protected void onUpdate() {};
	protected void onRender(Graphics2D g) {};
	protected void onReset() {}
	
	public void disable() {
		enabled = false;
	}
	
	public void enable() {
		enabled = true;
	}
	
	@Override
	public void update() {
		if(enabled) {
			if(physics) {
				if((velx > 0 && !hitRight) || (velx < 0 && !hitLeft))
					x += velx;
				if(airborne)
					vely -= GRAVITY;
				this.y += vely;
				this.checkWallCollision();
			} else {
				x += velx;
				y += vely;
			}
			onUpdate();
			if(boundable)
				keepInBound();
			updateHitbox();
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		if(enabled) {
			if(image != null)
				g.drawImage(image, (int)getIntX(), (int)getIntY(), null);
			onRender(g);
		}
	}
	
	public void reset() {
		x = respawnPoint.getX();
		y = respawnPoint.getY();
		velx = 0;
		vely = 0;
		this.updateHitbox();
		enable();
		onReset();
	}

	public void updateHitbox() {
		if(hitbox != null) {
			hitbox.x = (int) x;
			hitbox.y = (int) y;
		}
	}
	
	protected void checkWallCollision() {
		if(!physics) return;
		int[][] walls = GameSystem.getLevel().getWalls();
		int wallsize = GameSystem.getLevel().getWallSize();
		//the following methods checks if the object is colliding with the wall
		//and stops the object if it does. It does so by checking every wall
		//the object will pass through in the next tick based on the object's velocity
		if(vely < 0)
			checkTopCollision(walls, wallsize);
		else
			checkBottomCollision(walls, wallsize);
		if(velx < 0)
			checkLeftCollision(walls, wallsize);
		else
			checkRightCollision(walls, wallsize);
	}
	
	private void checkTopCollision(int[][] walls, int ws) {
		double collideX = x;
		double collideY = y + vely;
		double rightMostX = x + width - ws;
		int collideCol = (int) collideX / ws - 1;
		int collideRow = (int) collideY / ws;
		
		if(collideY <= 0) return;
		
		do
		{
			collideCol++;
			if(collideRow >= walls.length || collideCol >= walls[0].length) return;
			if(walls[collideRow][collideCol] > 0)
			{
				y = collideRow * ws + ws;
				vely = 0;
			}
		}
		while(collideCol * ws < rightMostX);
	}
	
	private void checkBottomCollision(int[][] walls, int ws) {
		double collideX = x;
		double collideY = y + height + vely;
		double rightMostX = x + width - ws;
		int collideCol = (int) collideX / ws - 1;
		int collideRow = (int) collideY / ws;
		
		do
		{
			collideCol++;
			if(collideRow >= walls.length || collideCol >= walls[0].length) return;
			if(collideRow * ws < 0) continue;
			if(walls[collideRow][collideCol] > 0)
			{
				y = collideRow * ws - height;
				airborne = false;
				vely = 0;
				break;
			}
			else
			{
				airborne = true;
			}
		}
		while(collideCol * ws < rightMostX);
	}
	
	private void checkLeftCollision(int[][] walls, int ws) {
		double collideX = x + velx;
		double collideY = y;
		double bottomY = y + height - ws;
		int collideCol = (int) collideX / ws;
		int collideRow = (int) collideY / ws - 1;
		
		if(collideX <= 0)
		{
			x = 0;
			hitLeft = true;
			return;
		}
		
		do
		{
			collideRow++;
			if(collideRow >= walls.length || collideCol >= walls[0].length) return;
			if(collideRow * ws < 0) continue;
			if(walls[collideRow][collideCol] > 0)
			{
				x = collideCol * ws + ws;
				hitLeft = true;
				velx = 0;
				break;
			}
			else
			{
				hitLeft = false;
			}
			
		}while(collideRow * ws < bottomY);
	}
	
	private void checkRightCollision(int walls[][], int ws) {
		double collideX = x + width + velx;
		double collideY = y;
		double bottomY = y + height - ws;
		int collideCol = (int) collideX / ws;
		int collideRow = (int) collideY / ws - 1;
		
		do
		{
			collideRow++;
			if(collideRow >= walls.length || collideCol >= walls[0].length) return;
			if(collideRow * ws < 0) continue;
			if(walls[collideRow][collideCol] > 0)
			{
				x = collideCol * ws - width;
				hitRight = true;
				velx = 0;
				break;
			}
			else
			{
				hitRight = false;
			}
			
		}while(collideRow * ws < bottomY);
		
	}
	
	private void keepInBound() {
		Level level = GameSystem.getLevel();
		if(x < 0) {
			x = 0;
			hitLeft = true;
		}
		else if(x + width > level.getWidth()) {
			x = level.getWidth() - width;
			hitRight = true;
		}
		if(y < 0) {
			y = 0;
			vely = 0;
		}
		else if(y + height > level.getHeight()) {
			y = level.getHeight() - height;
			vely = 0;
			airborne = false;
		}
	}
	
	public void removeSelf() {
		GameSystem.getLevel().removeGameObject(this);
	}
	
	public boolean collidesWith(GameObject obj) {
		if(hitbox != null && obj.getHitbox() != null)
			return hitbox.intersects(obj.getHitbox());
		return false;
	}
	
	protected void initSoundEffect() {
		soundEffect = getSoundEffectByClass(this.getClass());
	}
	
	public String toString() {
		return this.getClass().getName() + " " + x + " " + y;
	}
	
	//getters
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getVelX() {
		return velx;
	}
	
	public double getVelY() {
		return vely;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean isAirborne() {
		return airborne;
	}
	
	public boolean isHittingLeft() {
		return hitLeft;
	}
	
	public boolean isHittingRight() {
		return hitRight;
	}
	
	public boolean isUsingPhysics() {
		return physics;
	}
	
	public boolean isBoundable() {
		return boundable;
	}
	
	public Rectangle getHitbox() {
		return hitbox;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public int getIntX() { 
		return (int)x;
	}
	
	public int getIntY() {
		return (int)y;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	//setters
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setVelX(double velx) {
		this.velx = velx;
	}
	
	public void setVelY(double vely) {
		this.vely = vely;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setAirborne(boolean airborne) {
		this.airborne = airborne;
	}
	
	public void setHitLeft(boolean hitLeft) {
		this.hitLeft = hitLeft;
	}
	
	public void setHitRight(boolean hitRight) {
		this.hitRight = hitRight;
	}
	
	public void setPhysics(boolean physics) {
		this.physics = physics;
	}
	
	public void setBoundable(boolean boundable) {
		this.boundable = boundable;
	}
	
	public void setHitbox(Rectangle hitbox) {
		this.hitbox = hitbox;
	}
	
	public void setImage(BufferedImage image) {
		this.image = image;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
