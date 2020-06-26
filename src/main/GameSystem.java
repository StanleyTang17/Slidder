package main;

import java.util.*;

import javax.swing.JFrame;

import game.Level;
import game.SoundEffect;
import game.ActiveLevel;
import game.utility.TimedAction;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.Graphics2D;
import java.awt.event.*;

public class GameSystem {
	
	public static final int SCREEN_WIDTH = 1200;
	public static final int SCREEN_HEIGHT = 730;
	
	private static Level CURRENT_LEVEL = null;
	private static boolean isLoadingLevel = false;
	
	private static TreeMap<String, File> importedLevels = new TreeMap<>();
	private static HashMap<String, Screen> screens = new HashMap<>();
	private static Screen currentScreen = null;
	private static LinkedList<TimedAction> timedActionHolder = new LinkedList<>();
	
	private static KeyListener keyListener = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {
			currentScreen.keyTyped(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			currentScreen.keyPressed(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			currentScreen.keyReleased(e);
		}
		
	};
	
	private static MouseListener mouseListener = new MouseListener() {

		@Override
		public void mouseClicked(MouseEvent e) {
			currentScreen.mouseClicked(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			currentScreen.mousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			currentScreen.mouseReleased(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			currentScreen.mouseEntered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			currentScreen.mouseExited(e);
		}
		
	};
	
	private static MouseMotionListener mouseMotionListener = new MouseMotionListener() {

		@Override
		public void mouseDragged(MouseEvent e) {
			currentScreen.mouseDragged(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			currentScreen.mouseMoved(e);
		}
		
	};
	
	public static void initListeners(JFrame frame) {
		frame.addKeyListener(keyListener);
		frame.addMouseListener(mouseListener);
		frame.addMouseMotionListener(mouseMotionListener);
	}
	
	public static void loadLevel(Level level) {
		CURRENT_LEVEL = level;
	}
	
	public static void loadLevel(File levelFile) throws IOException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		isLoadingLevel = true;
		CURRENT_LEVEL = new ActiveLevel(levelFile);
		ListIterator<TimedAction> iter = timedActionHolder.listIterator();
		while(iter.hasNext()) {
			CURRENT_LEVEL.getTimedActionQueue().add(iter.next());
			iter.remove();
		}
		isLoadingLevel = false;
	}
	
	public static void loadLevel(String customLevelName) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		loadLevel(importedLevels.get(customLevelName));
	}
	
	public static void importLevel(File levelFile) throws IOException {
		String fileName = levelFile.getName();
		importedLevels.put(fileName.substring(0, fileName.length()-4), levelFile);
		GameSystem.saveConfigs();
	}
	
	public static void deleteLevel(String levelName) throws IOException {
		importedLevels.remove(levelName);
		GameSystem.saveConfigs();
	}
	
	public static Collection<File> getImportedLevels() {
		return importedLevels.values();
	}
	
	public static void loadConfigs() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("Configs.txt"));
		boolean soundEffectMuted = Boolean.parseBoolean(br.readLine());
		SoundEffect.setMuted(soundEffectMuted);
		String filePath = br.readLine();
		while(filePath != null && !filePath.isEmpty()) {
			File levelFile = new File(filePath);
			if(levelFile.exists())
				GameSystem.importLevel(new File(filePath));
			filePath = br.readLine();
		}
		br.close();
		saveConfigs();
	}
	
	public static void saveConfigs() throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("Configs.txt"));
		bw.write(Boolean.toString(SoundEffect.isMuted()));
		bw.newLine();
		for(File levelFile : getImportedLevels()) {
			bw.write(levelFile.getAbsolutePath());
			bw.newLine();
		}
		bw.close();
	}
	
	public static void mainUpdate() {
		currentScreen.update();
	}
	
	public static void mainRender(Graphics2D g) { 
		currentScreen.render(g);
	}
	
	public static void addTimedAction(TimedAction action) {
		if(isLoadingLevel)
			timedActionHolder.add(action);
		else
			CURRENT_LEVEL.getTimedActionQueue().add(action);
	}
	
	public static void addScreen(String screenName, Screen screen) {
		screens.put(screenName, screen);
		if(currentScreen == null) currentScreen = screen;
	}
	
	//getters
	public static Screen getScreen(String screenName) {
		return screens.get(screenName);
	}
	
	public static Screen getCurrentScreen() {
		return currentScreen;
	}
	
	public static Level getLevel() {
		return CURRENT_LEVEL;
	}
	
	public static ActiveLevel getActiveLevel() {
		return (ActiveLevel) CURRENT_LEVEL;
	}
	
	//setters
	public static void setCurrentScreen(String screenName) { 
		currentScreen = screens.get(screenName);
		currentScreen.reset();
	}
}
