package main;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;

import game.SoundEffect;
import game.utility.Utility;

@SuppressWarnings("serial")
public class LaunchGame extends JPanel implements Runnable {
	
	public static final int linesOfWorkingCode = 3374;
	private int FPS = 120;
	private Thread thread;
	private int screenWidth = GameSystem.SCREEN_WIDTH;
	private int screenHeight = GameSystem.SCREEN_HEIGHT;
	
	public LaunchGame() {
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setVisible(true);
		
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		while(true) {
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
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		JFrame frame = new JFrame ("Slidder");
		
		//init menu screen
		Font titleFont = new Font("impact", Font.ITALIC, 64);
		Screen menuScreen = new Screen();
		BufferedImage soundIcon = ImageIO.read(new File("res\\gfx\\Sound.png"));
		BufferedImage mutedIcon = ImageIO.read(new File("res\\gfx\\SoundMuted.png"));
		GButton btnPlayLevel = new GButton(100, 200, 300, 50, "Play", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(GameSystem.getLevel() == null)
					try {
						GameSystem.loadLevel(new File("levels\\1.lvl"));
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				else
					GameSystem.getActiveLevel().restart();
				GameSystem.setCurrentScreen("level");
			}
		});
		GButton btnSelectClassic = new GButton(100, 260, 300, 50, "Classic Levels", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameSystem.setCurrentScreen("selectClassic");
			}
		});
		GButton btnSelectCustom = new GButton(100, 320, 300, 50, "Custom Levels", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameSystem.setCurrentScreen("selectCustom");
			}
		});
		GButton btnInstructions = new GButton(100, 380, 300, 50, "How to Play", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameSystem.setCurrentScreen("how to play");
			}
		});
		GButton btnAbout = new GButton(100, 440, 300, 50, "About", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameSystem.setCurrentScreen("about");
			}
		});
		GButton btnSound = new GButton(100, 650, 30, 30, "", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(SoundEffect.isMuted())
					SoundEffect.setMuted(false);
				else
					SoundEffect.setMuted(true);
				try {
					GameSystem.saveConfigs();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		BasicFunctionality drawMenuScreen = new BasicFunctionality() {
			@Override
			public void update() {}
			@Override
			public void render(Graphics2D g) {
				g.setFont(titleFont);
				g.drawString("SLIDDER", 100, 100);
				if(SoundEffect.isMuted())
					g.drawImage(mutedIcon, 100, 650, null);
				else
					g.drawImage(soundIcon, 100, 650, null);
			}
		};
		menuScreen.addButton(btnPlayLevel);
		menuScreen.addButton(btnSelectClassic);
		menuScreen.addButton(btnSelectCustom);
		menuScreen.addButton(btnInstructions);
		menuScreen.addButton(btnAbout);
		menuScreen.addButton(btnSound);
		menuScreen.addFunctionality(drawMenuScreen);
		
		//init level screen
		Screen levelScreen = new Screen();
		levelScreen.addFunctionality(new BasicFunctionality() {
			@Override
			public void update() {
				GameSystem.getActiveLevel().update();
			}
			@Override
			public void render(Graphics2D g) {
				GameSystem.getActiveLevel().render(g);
			}
		});
		levelScreen.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				GameSystem.getActiveLevel().keyTyped(e);
			}
			@Override
			public void keyPressed(KeyEvent e) {
				GameSystem.getActiveLevel().keyPressed(e);
			}
			@Override
			public void keyReleased(KeyEvent e) {
				GameSystem.getActiveLevel().keyReleased(e);
			}
		});
		
		//init select classic levels screen
		Screen selectClassicScreen = new Screen();
		for(int i = 0; i <= 4; ++i) {
			int size = 50;
			int x = 100 + i * (size + 20);
			int y = 100 + i;
			final int levelNumber = i + 1;
			selectClassicScreen.addButton(new GButton(x, y, size, size, ""+levelNumber, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						GameSystem.loadLevel(new File("levels\\" + (levelNumber) + ".lvl"));
						GameSystem.setCurrentScreen("level");
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}));
		}
		GButton btnBack = new GButton(0, 0, 200, 30, "Back to Main Menu", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameSystem.setCurrentScreen("menu");
			}
		});
		BasicFunctionality drawClassicScreen = new BasicFunctionality() {
			@Override
			public void update() {}
			@Override
			public void render(Graphics2D g) {
				g.setFont(titleFont);
				Utility.drawCenteredString(g, "Classic Levels", -330);
			}
		};
		selectClassicScreen.addButton(btnBack);
		selectClassicScreen.addFunctionality(drawClassicScreen);
		
		//init select custom levels screen
		Screen selectCustomScreen = new CustomLevelScreen(frame);
		
		//init instruction page
		Screen instructionScreen = new Screen();
		BufferedImage instructions = ImageIO.read(new File("res\\gfx\\Instructions.png"));
		BasicFunctionality drawInstructions = new BasicFunctionality() {
			@Override
			public void update() {}
			@Override
			public void render(Graphics2D g) {
				g.drawImage(instructions, 0, 0, null);
			}
		};
		instructionScreen.addFunctionality(drawInstructions);
		instructionScreen.addButton(btnBack);
		
		//init about screen
		Screen aboutScreen = new Screen();
		Font textFont = new Font("arial", Font.PLAIN, 24);
		BufferedImage avatorImage = ImageIO.read(new File("res\\gfx\\Avatar.png"));
		BasicFunctionality drawAboutScreen = new BasicFunctionality() {
			@Override
			public void update() {}
			@Override
			public void render(Graphics2D g) {
				g.setFont(titleFont);
				Utility.drawCenteredString(g, "About", -330);
				g.drawImage(avatorImage, 100, 100, null);
				g.setFont(textFont);
				g.drawString("Developer: Stanley Tang", 250, 150);
				g.drawString("Lines of Working Code So Far: " + linesOfWorkingCode, 250, 190);
			}
		};
		aboutScreen.addButton(btnBack);
		aboutScreen.addFunctionality(drawAboutScreen);
		
		//add screens
		GameSystem.addScreen("menu", menuScreen);
		GameSystem.addScreen("level", levelScreen);
		GameSystem.addScreen("selectClassic", selectClassicScreen);
		GameSystem.addScreen("selectCustom", selectCustomScreen);
		GameSystem.addScreen("how to play", instructionScreen);
		GameSystem.addScreen("about", aboutScreen);
		
		//init JFrame
		LaunchGame game = new LaunchGame ();
		frame.add(game);
		GameSystem.initListeners(frame);
		frame.setVisible(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
	}
	
	private static class CustomLevelScreen extends Screen {
		
		private String selectName = null;
		private GButton btnBack, btnImport, btnPlay, btnDelete, btnPrev, btnNext;
		private GButton[] fileBtns = new GButton[11];
		private ArrayList<File> levels = new ArrayList<>();
		private int selectIndex = -1;
		private int pages = 1;
		private int currentPage = 1;
		
		public CustomLevelScreen(JFrame f) throws IOException {
			
			BasicFunctionality drawCustomScreen = new BasicFunctionality() {
				@Override
				public void update() {}
				@Override
				public void render(Graphics2D g) {
					g.setColor(Color.BLACK);
					g.setFont(new Font("impact", Font.ITALIC, 64));
					Utility.drawCenteredString(g, "Custom Levels", -330);
					
					g.setStroke(new BasicStroke(3));
					g.drawRect(298, 98, 853, 553);
					
					g.setFont(new Font("impact", Font.PLAIN, 24));
					String s = "Page " + currentPage + " out of " + pages;
					
					int stringX = 1151 - g.getFontMetrics().stringWidth(s);
					int stringY = 651 + g.getFontMetrics().getAscent();
					g.drawString(s, stringX, stringY);
					
					if(selectIndex != -1) {
						g.setColor(Color.RED);
						g.drawRect(301, 101 + selectIndex * 50, 847, 47);
					}
				}
			};
			
			//init buttons
			btnBack = new GButton(0, 0, 200, 30, "Back to Main Menu", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					GameSystem.setCurrentScreen("menu");
				}
			});
			btnImport = new GButton(50, 100, 200, 50, "Import", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					FileDialog fileDialog = new FileDialog(f, "choose a level to import", FileDialog.LOAD);
					fileDialog.setVisible(true);
					File[] files = fileDialog.getFiles();
					for(File file : files)
						try {
							GameSystem.importLevel(file);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					refreshItems();
				}
			});
			btnPlay = new GButton(50, 160, 200, 50, "Play", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						GameSystem.loadLevel(selectName);
						GameSystem.setCurrentScreen("level");
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
							| IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
			btnDelete = new GButton(50, 220, 200, 50, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						GameSystem.deleteLevel(selectName);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					refreshItems();
				}
			});
			btnPrev = new GButton(690, 660, 30, 30, "<", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(currentPage > 1) {
						currentPage--;
						refreshPage();
						deselect();
					}
				}
			});
			btnNext = new GButton(730, 660, 30, 30, ">", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(currentPage < pages) {
						currentPage++;
						refreshPage();
						deselect();
					}
				}
			});
			
			GameSystem.loadConfigs();
			
			for(int i = 0; i < fileBtns.length; ++i) {
				final int index = i;
				GButton btn = new GButton(300, 100 + i * 50, 850, 50, "", new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(selectName == null || (selectIndex != -1 && selectIndex != index)) {
							selectName = fileBtns[index].getText();
							selectIndex = index;
							btnPlay.setEnabled(true);
							btnDelete.setEnabled(true);
						}else {
							deselect();
						}
					}
				});
				btn.setBorderless(true);
				btn.setEnabled(false);
				fileBtns[i] = btn;
				this.addButton(btn);
			}
			btnPlay.setEnabled(false);
			btnDelete.setEnabled(false);
			this.addButton(btnBack);
			this.addButton(btnImport);
			this.addButton(btnPlay);
			this.addButton(btnDelete);
			this.addButton(btnPrev);
			this.addButton(btnNext);
			this.addFunctionality(drawCustomScreen);
			refreshItems();
		}
		
		//refreshes the list when levels are imported or deleted
		private void refreshItems() {
			levels.clear();
			levels.addAll(GameSystem.getImportedLevels());
			pages = (int)Math.ceil(levels.size() / 11.0);
			if(currentPage > pages || currentPage < 1) currentPage = pages;
			selectIndex = -1;
			selectName = null;
			btnPlay.setEnabled(false);
			btnDelete.setEnabled(false);
			refreshPage();
		}
		
		//refreshes the current page of levels 
		private void refreshPage() {
			if(currentPage < 1) {
				fileBtns[0].setEnabled(false);
				fileBtns[0].setInvisible(true);
				btnPrev.setEnabled(false);
				btnNext.setEnabled(false);
				return;
			}
			for(int i = 0; i < 11; ++i) {
				int index = (currentPage - 1) * 11 + i;
				if(index < levels.size()) {
					String fileName = levels.get(index).getName();
					fileBtns[i].setText(fileName.substring(0, fileName.length()-4));
					fileBtns[i].setEnabled(true);
					fileBtns[i].setInvisible(false);
				}else {
					fileBtns[i].setEnabled(false);
					fileBtns[i].setInvisible(true);
				}
			}
			
			if(currentPage > 1)
				btnPrev.setEnabled(true);
			else
				btnPrev.setEnabled(false);
			
			if(currentPage < pages)
				btnNext.setEnabled(true);
			else
				btnNext.setEnabled(false);
		}
		
		//deselect a level
		private void deselect() {
			btnPlay.setEnabled(false);
			btnDelete.setEnabled(false);
			selectName = null;
			selectIndex = -1;
		}
		
		@Override
		public void reset() {
			for(GButton btn : buttons) btn.reset();
			deselect();
			currentPage = pages == 0 ? 0 : 1;
		}
	}
}
