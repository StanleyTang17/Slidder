package main;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

import gamemaker.GameMaker;

@SuppressWarnings("serial") //funky warning, just suppress it. It's not gonna do anything.
public class LaunchGameMaker extends JPanel implements Runnable {
	
	//self explanatory variables
	int FPS = 120;
	Thread thread;
	int screenWidth = GameSystem.SCREEN_WIDTH;
	int screenHeight = GameSystem.SCREEN_HEIGHT;
	
	public LaunchGameMaker() {
		//sets up JPanel
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setVisible(true);
		
		//starting the thread
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		while(true) {
			//main game loop
			GameSystem.mainUpdate();
			this.repaint();
			try {
				Thread.sleep(1000/FPS);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, screenWidth, screenHeight);
		GameSystem.mainRender((Graphics2D) g);
	}
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		GameSystem.addScreen("default", new Screen());
		//init JFrame
		JFrame frame = new JFrame ("Slider's Escape Game Editor");
		frame.setLayout(new FlowLayout());
		LaunchGameMaker myPanel = new LaunchGameMaker ();
		myPanel.setFocusable(false);
		frame.add(myPanel);
		
		new GameMaker(frame);
		
		frame.setUndecorated(false);
		frame.setVisible(true);
		frame.pack();
		frame.setLocation(-7, 0);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setFocusable(true);
		frame.requestFocus();
	}
}
