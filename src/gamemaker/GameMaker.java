package gamemaker;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import game.*;
import game.gameobject.GameObject;
import game.utility.Point;
import main.BasicFunctionality;
import main.GameSystem;

public class GameMaker implements BasicFunctionality, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	
	private OptionPanel optionPanel;
	private Level level;
	private Class<GameObject>[] objClasses;
	private int placement = -1;
	private int wallBlock = 1;
	private int selectRow = 0, selectCol = 0;
	private int selectLayoutRow = 0, selectLayoutCol = 0;
	private int selectRoom = 0;
	private int mouseX = 0, mouseY = 0;
	private double placeX = 0, placeY = 0;
	private BufferedImage preview = null;
	private boolean ctrlPressed = false;
	private JFrame mainFrame;
	
	@SuppressWarnings("unchecked")
	public GameMaker(JFrame f) throws ClassNotFoundException, IOException {
		//init listeners
		GameSystem.getCurrentScreen().addFunctionality(this);
		GameSystem.getCurrentScreen().addKeyListener(this);
		optionPanel = new OptionPanel(this);
		f.addKeyListener(this);
		f.addMouseListener(this);
		f.addMouseMotionListener(this);
		f.addMouseWheelListener(this);
		f.add(optionPanel);
		mainFrame = f;
		
		//read in object classes
		BufferedReader br  = new BufferedReader(new FileReader("GameObjectList.txt"));
		ArrayList<Class<?>> classes = new ArrayList<>();
		String name = br.readLine();
		while(name != null) {
			classes.add(Class.forName("game.gameobject." + name));
			name = br.readLine();
		}
		br.close();
		objClasses = classes.toArray(new Class[classes.size()]);
	}
	
	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Graphics2D g) {
		// TODO Auto-generated method stub
		if(level != null) {
			level.defaultRender(g);
			Point offset = level.getGraphicOffset();
			double scale = level.getGraphicsScale();
			double ws = level.getWallSize();
			int width = level.getRoomWidth();
			int height = level.getRoomHeight();
			Point coord = level.getEmptySpot();
			
			g.translate(offset.getX(), offset.getY());
			g.scale(scale, scale);
			
			g.setColor(Color.GRAY);
			g.fillRect(coord.getIntY() * width, coord.getIntX() * height, width, height);
			
			//draw preview of placement
			if(placement == -1) {
				g.setColor(Color.BLACK);
				g.drawRect((int)(selectCol * ws), (int)(selectRow * ws), (int)ws, (int)ws);
			}else {
				g.drawImage(preview, (int)(placeX), (int)(placeY), null);
			}
			
			g.scale(1/scale, 1/scale);
			g.translate(-offset.getX(), -offset.getY());
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int key = e.getKeyCode();
		if(key == KeyEvent.VK_E)
			changePlacement(1);
		else if(key == KeyEvent.VK_Q)
			changePlacement(-1);
		else if(key == KeyEvent.VK_CONTROL)
			ctrlPressed = true;
		
		if(ctrlPressed) {
			if(key == KeyEvent.VK_S)
				saveLevel();
			else if(key == KeyEvent.VK_N)
				newLevel();
			else if(key == KeyEvent.VK_O)
				openLevel();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_CONTROL)
			ctrlPressed = false;
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		updateSelect(e.getX(), e.getY(), e.isShiftDown());
		if(e.getButton() == 1) {
			wallBlock = 1;
			if(placement == -1)
				this.placeWall();
			else
				try {
					this.placeObject();
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
						| IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
					// TODO Auto-generated catch block
					exception.printStackTrace();
				}
		}else {
			wallBlock = 0;
			if(placement == -1)
				this.placeWall();
			else{
				Iterator<GameObject> iter = level.getObjectList().iterator();
				while(iter.hasNext()) {
					if(iter.next().getHitbox().contains(mouseX / level.getGraphicsScale(), mouseY / level.getGraphicsScale())) {
						iter.remove();
						return;
					}
				}
			}
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		updateSelect(e.getX(), e.getY(), e.isShiftDown());
		if(placement == -1) this.placeWall();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		updateSelect(e.getX(), e.getY(), e.isShiftDown());
		
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		updateSelect(e.getX(), e.getY(), false);
		int tick = e.getWheelRotation();
		changePlacement(tick);
	}
	
	public void newLevel() {
		//pop up window
		JTextField nameText = new JTextField();
		JTextField layout_rows = new JTextField();
		JTextField layout_cols = new JTextField();
		JTextField room_rows = new JTextField();
		JTextField room_cols = new JTextField();
		JTextField emptySpot_row = new JTextField();
		JTextField emptySpot_col = new JTextField();
		JTextField durationText = new JTextField();
		Object[] parts = {
				"name: ", nameText, 
				"\nlayout size(2x2-5x5): \n", layout_rows, "by", layout_cols, 
				"\nroom size(5x5-30x30): \n", room_rows, "by", room_cols,
				"\nempty spot position:\nrow:", emptySpot_row, "column: \n", emptySpot_col,
				"\ntimed duration(0-300 max): \n", durationText};
		JOptionPane.showConfirmDialog(null, parts, "Create new level", JOptionPane.DEFAULT_OPTION);
		
		//create level based on information entered
		try {
			String name = nameText.getText();
			int layoutRows = Integer.parseInt(layout_rows.getText());
			int layoutCols = Integer.parseInt(layout_cols.getText());
			int roomRows = Integer.parseInt(room_rows.getText());
			int roomCols = Integer.parseInt(room_cols.getText());
			int emptySpotRow = Integer.parseInt(emptySpot_row.getText());
			int emptySpotCol = Integer.parseInt(emptySpot_col.getText());
			int duration = Integer.parseInt(durationText.getText());
			//check for parameter limits
			if(layoutRows < 2 || layoutCols < 2 || layoutRows > 5 || layoutCols > 5 ||
			   roomRows < 5 || roomCols < 5 || roomRows > 30 || roomCols > 30 ||
			   emptySpotRow < 1 || emptySpotRow > layoutRows || emptySpotCol < 1 || emptySpotCol > layoutCols ||
			   duration < 0 || duration > 300)
				throw new NumberFormatException();
			Point emptySpot = new Point(emptySpotRow - 1, emptySpotCol - 1);
			Level level = new Level(name, layoutRows, layoutCols, roomRows, roomCols, emptySpot, duration);
			GameSystem.loadLevel(level);
			setLevel(level);
		}catch(NumberFormatException exception) {
			
		}
	}
	
	public void saveLevel() {
		if(level == null) return;
		try {
			FileDialog saveDialog = new FileDialog(mainFrame, "save level", FileDialog.SAVE);
			saveDialog.setFile(level.getName() + ".lvl");
			saveDialog.setVisible(true);
			String fileName = saveDialog.getFile();
			String directory = saveDialog.getDirectory();
			if(fileName != null && directory != null)
				level.save(new File(directory + fileName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void openLevel() {
		FileDialog fileDialog = new FileDialog(mainFrame, "choose a level file", FileDialog.LOAD);
		fileDialog.setVisible(true);
		try {
			if(fileDialog.getFiles().length > 0) {
				GameSystem.loadLevel(fileDialog.getFiles()[0]);
				setLevel(GameSystem.getLevel());
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e1) {
			JOptionPane.showConfirmDialog(mainFrame, "Invalid file!", "Error", JOptionPane.DEFAULT_OPTION);
		}
	}
	
	private void updateSelect(int x, int y, boolean adjustToGrid) {
		if(level == null) return;
		double ws = level.getWallSizeOnScreen();
		Point offset = level.getGraphicOffset();
		mouseX = (int)(x - 13 - offset.getX());
		mouseY = (int)(y - 32 - offset.getY());
		selectRow = (int) (mouseY / ws);
		selectCol = (int) (mouseX / ws);
		
		//draw preview of GameObject
		if(preview != null)
			if(adjustToGrid) {
				placeX = selectCol * level.getWallSize();
				placeY = (selectRow + 1) * level.getWallSize() - preview.getHeight();
			}else {
				placeX = (mouseX - preview.getWidth() / 4) / level.getGraphicsScale();
				placeY = (mouseY - preview.getHeight() / 4) / level.getGraphicsScale();
			}
		
		//check mouse out of bounds
		if(selectRow < 0)
			selectRow = 0;
		else if(selectRow >= level.getTotalRows())
			selectRow = level.getTotalRows() - 1;
		if(selectCol < 0)
			selectCol = 0;
		else if(selectCol >= level.getTotalCols())
			selectCol = level.getTotalCols() - 1;
		
		selectLayoutRow = selectRow / level.getRoomRows();
		selectLayoutCol = selectCol / level.getRoomCols();
		selectRoom = level.getLayout()[selectLayoutRow][selectLayoutCol];
	}
	
	private void placeWall() {
		if(level == null) return;
		if(selectRoom == -1) return;
		level.getWalls()[selectRow][selectCol] = wallBlock;
		level.getRooms().get(selectRoom)[selectRow % level.getRoomRows()][selectCol % level.getRoomCols()] = wallBlock;
		level.reconstructWalls();
	}
	
	private void placeObject() throws NumberFormatException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String className = objClasses[placement].getName();
		GameObject newObj = GameObject.parseObject(className + " " + placeX + " " + placeY);
		//check if obj is out of bounds
		if(newObj.getX() < 0 || newObj.getY() < 0 ||
		   newObj.getX() + newObj.getWidth() > level.getWidth() ||
		   newObj.getY() + newObj.getHeight() > level.getHeight())
			return;
		//check if obj is in between rooms
		if(level.getRoomPos(newObj.getX(), newObj.getY()) > -1)
			level.getObjectList().add(newObj);
	}
	
	private void changePlacement(int increment) {
		placement += increment;
		if(placement >= objClasses.length) placement = -1;
		if(placement < -1) placement = objClasses.length - 1;
		if(placement != -1)
			preview = GameObject.getImageByClass(objClasses[placement]);
	}
	
	//getters
	public int getPlacement() {
		return placement;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public OptionPanel getOptionPanel() {
		return optionPanel;
	}
	
	public int getPlaceX() { 
		return (int) placeX;
	}
	
	public int getPlaceY() {
		return (int) placeY;
	}
	
	public Class<GameObject>[] getObjectClasses() {
		return objClasses;
	}
	
	//setters
	public void setLevel(Level level) {
		this.level = level;
	}
}
