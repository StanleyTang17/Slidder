package game;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.awt.*;

import game.gameobject.GameObject;
import game.gameobject.Player;
import game.utility.Point;
import game.utility.TimedActionQueue;
import game.utility.Utility;
import main.GameSystem;

public class Level {
	
	protected String name;
	protected int[][] layout, walls;
	protected LinkedList<int[][]> rooms = new LinkedList<>();
	protected ConcurrentLinkedDeque<GameObject> objects = new ConcurrentLinkedDeque<>();
	protected int roomRows, roomCols, totalRows, totalCols;
	protected int width, height, roomWidth, roomHeight, wallSize = 30;
	protected Point emptySpot;
	protected TimedActionQueue timedQueue = new TimedActionQueue();
	protected Player player = null;
	protected double graphicsScale;
	protected Point offset;
	protected int duration = 0;
	
	public Level(File levelFile) throws IOException, NumberFormatException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		this.name = levelFile.getName();
		name = name.substring(0, name.length() - 4);
		
		BufferedReader br = new BufferedReader(new FileReader(levelFile));
		duration = Utility.readNumber(br);
		//read layout
		int layoutRows = Utility.readNumber(br);
		int layoutCols = Utility.readNumber(br);
		layout = Utility.read2DArray(br, layoutRows, layoutCols);
		//read rooms
		int numRooms = Utility.readNumber(br);
		roomRows = Utility.readNumber(br);
		roomCols = Utility.readNumber(br);
		for(int i = 0; i < numRooms; ++i) {
			rooms.add(Utility.read2DArray(br, roomRows, roomCols));
			br.readLine();
		}
		//read objects
		String objStr = br.readLine();
		while(objStr != null) {
			GameObject obj = GameObject.parseObject(objStr);
			if(obj.getClass() == Player.class)
				player = (Player) obj;
			else
				objects.add(obj);
			objStr = br.readLine();
		}
		br.close();
		
		if(player == null)
			throw new IOException();
		objects.add(player);
		
		totalRows = layout.length * roomRows;
		totalCols = layout[0].length * roomCols;
		walls = new int[totalRows][totalCols];
		width = totalCols * wallSize;
		height = totalRows * wallSize;
		roomWidth = roomCols * wallSize;
		roomHeight = roomRows * wallSize;
		for(int i = 0; i < layout.length; ++i)
			for(int j = 0; j < layout[0].length; ++j)
				if(layout[i][j] == -1) {
					emptySpot = new Point(i, j);
					break;
				}
		
		graphicsScale = Math.min((double)GameSystem.SCREEN_WIDTH / (double)width, (double)GameSystem.SCREEN_HEIGHT / (double)height);
		offset = new Point((GameSystem.SCREEN_WIDTH - width * graphicsScale) / 2, (GameSystem.SCREEN_HEIGHT - height * graphicsScale) / 2);
		
		this.reconstructWalls();
	}
	
	public Level(String name, int layoutRows, int layoutCols, int roomRows, int roomCols, Point emptySpot, int duration) {
		this.name = name;
		layout = new int[layoutRows][layoutCols];
		this.roomRows = roomRows;
		this.roomCols = roomCols;
		this.emptySpot = emptySpot;
		this.duration = duration;
		totalRows = layout.length * roomRows;
		totalCols = layout[0].length * roomCols;
		walls = new int[totalRows][totalCols];
		
		layout[emptySpot.getIntX()][emptySpot.getIntY()] = -1;
		
		//create empty rooms
		int room = 0;
		for(int i = 0; i < layoutRows; ++i)
			for(int j = 0; j < layoutCols; ++j) {
				if(layout[i][j] != -1) {
					layout[i][j] = room;
					rooms.add(new int[roomRows][roomCols]);
					room++;
				}
			}
		
		width = totalCols * wallSize;
		height = totalRows * wallSize;
		roomWidth = roomCols * wallSize;
		roomHeight = roomRows * wallSize;
		
		graphicsScale = Math.min((double)GameSystem.SCREEN_WIDTH / (double)width, (double)GameSystem.SCREEN_HEIGHT / (double)height);
		offset = new Point((GameSystem.SCREEN_WIDTH - getWidth() * graphicsScale) / 2, (GameSystem.SCREEN_HEIGHT - getHeight() * graphicsScale) / 2);
	}
	
	public void defaultRender(Graphics2D g) {
		g.translate(offset.getX(), offset.getY());
		g.scale(graphicsScale, graphicsScale);
		
		//draw walls
		int[][] layout = getLayout(), walls = getWalls();
		int wallsize = getWallSize();
		for(int i = 0; i < walls.length; ++i) {
			for(int j = 0; j < walls[0].length; ++j) {
				g.setColor(Color.DARK_GRAY);
				if(walls[i][j] == 1) {
					g.fillRect(j * wallsize, i * wallsize, wallsize, wallsize);
				}
			}
		}
		
		//draw room borders
		g.setColor(Color.RED);
		g.setStroke(new BasicStroke(3));
		for(int i = 0; i < layout.length; ++i)
			for(int j = 0; j < layout[0].length; ++j)
				if(layout[i][j] != -1)
					g.drawRect(j * getRoomWidth(), i * getRoomHeight(), getRoomWidth(), getRoomHeight());
		g.setStroke(new BasicStroke(1));
		
		//draw GameObjects
		for(GameObject obj : objects) obj.render(g);
		
		g.scale(1/graphicsScale, 1/graphicsScale);
		g.translate(-offset.getX(), -offset.getY());
	}
	
	public Point getGridPos(double x, double y) {
		int row, col;
		if(x < 0)
			col = -1;
		else
			col = (int) (x / wallSize);
		if(y < 0)
			row = -1;
		else
			row = (int) (y / wallSize);
		return new Point(row, col);
	}
	
	public int getRoomPos(double x, double y) {
		int row = (int)y / roomHeight;
		int col = (int)x / roomWidth;
		if((int)y % roomHeight == 0)
			row--;
		if((int)x % roomWidth == 0)
			col--;
		return layout[row][col];
	}
	
	public void reconstructWalls() {
		//go through each room
		for(int i = 0; i < layout.length; ++i)
			for(int j = 0; j < layout[0].length; ++j) {
				int roomNum = layout[i][j];
				//go through the walls in each room
				if(roomNum != -1) {
					int[][] w = rooms.get(roomNum);
					for(int a = 0; a < w.length; ++a)
						for(int b = 0; b < w[0].length; ++b)
							walls[i * roomRows + a][j * roomCols + b] = w[a][b];
				}else {
					for(int a = 0; a < roomRows; ++a)
						for(int b = 0; b < roomCols; ++b)
							walls[i * roomRows + a][j * roomCols + b] = 0;
				}
			}
	}
	
	public void save(File saveFile) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
		Utility.writeNumber(bw, duration);
		Utility.writeNumber(bw, layout.length);
		Utility.writeNumber(bw, layout[0].length);
		Utility.write2DArray(bw, layout);
		Utility.writeNumber(bw, rooms.size());
		Utility.writeNumber(bw, roomRows);
		Utility.writeNumber(bw, roomCols);
		for(int[][] room : rooms) {
			Utility.write2DArray(bw, room);
			bw.newLine();
		}
		for(GameObject obj : objects) {
			bw.write(obj.toString());
			bw.newLine();
		}
		bw.close();
	}
	
	public void addGameObject(GameObject object) {
		objects.add(object);
	}
	
	public void removeGameObject(GameObject object) {
		objects.remove(object);
	}
	
	//getters
	public int getRoomRows() {
		return roomRows;
	}
	
	public int getRoomCols() {
		return roomCols;
	}
	
	public ConcurrentLinkedDeque<GameObject> getObjectList() {
		return objects;
	}
	
	public String getName() {
		return name;
	}
	
	public int[][] getLayout() {
		return layout;
	}
	
	public int[][] getWalls() {
		return walls;
	}
	
	public LinkedList<int[][]> getRooms() {
		return rooms;
	}
	
	public int getWallSize() {
		return wallSize;
	}
	
	public double getWallSizeOnScreen() {
		return wallSize * graphicsScale;
	}
	
	public int getTotalRows() {
		return totalRows;
	}
	
	public int getTotalCols() {
		return totalCols;
	}
	
	public int getRoomWidth() {
		return roomWidth;
	}
	
	public int getRoomHeight() {
		return roomHeight;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Point getEmptySpot() {
		return emptySpot;
	}
	
	public double getGraphicsScale() {
		return graphicsScale;
	}
	
	public Point getGraphicOffset() {
		return offset;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public TimedActionQueue getTimedActionQueue() {
		return timedQueue;
	}
	
	public int getDuration() {
		return duration;
	}
	
	//setter
	public void setDuration(int duration) {
		this.duration = duration;
	}
}
