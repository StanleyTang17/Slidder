package gamemaker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import main.BasicFunctionality;
import main.GameSystem;

@SuppressWarnings("serial")
public class OptionPanel extends JPanel implements BasicFunctionality, ActionListener {
	
	private GameMaker gameMaker;
	private JLabel levelLabel = new JLabel();
	private JLabel placementLabel = new JLabel();
	private JLabel xLabel = new JLabel();
	private JLabel yLabel = new JLabel();
	private JButton btnNewFile = new JButton("new  level");
	private JButton btnOpenFile = new JButton("open level");
	private JButton btnSaveFile = new JButton("save level");
	private JButton btnDuration = new JButton("time: ");
	
	public OptionPanel(GameMaker gm) {
		this.gameMaker = gm;
		//init buttons
		btnNewFile.setActionCommand("new");
		btnNewFile.addActionListener(this);
		btnNewFile.setFocusable(false);
		btnOpenFile.setActionCommand("open");
		btnOpenFile.addActionListener(this);
		btnOpenFile.setFocusable(false);
		btnSaveFile.setActionCommand("save");
		btnSaveFile.addActionListener(this);
		btnSaveFile.setFocusable(false);
		btnDuration.setActionCommand("duration");
		btnDuration.addActionListener(this);
		btnDuration.setFocusable(false);
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		Dimension d = new Dimension(150, GameSystem.SCREEN_HEIGHT);
		this.setPreferredSize(d);
		this.setMaximumSize(d);
		this.setMinimumSize(d);
		this.add(btnNewFile);
		this.add(btnOpenFile);
		this.add(btnSaveFile);
		this.add(levelLabel);
		this.add(placementLabel);
		this.add(xLabel);
		this.add(yLabel);
		this.add(btnDuration);
		GameSystem.getCurrentScreen().addFunctionality(this);
		this.setFocusable(false);
	}

	@Override
	public void update() {
		if(gameMaker.getLevel() == null) return;
		//update info about level
		xLabel.setText("x: " + gameMaker.getPlaceX());
		yLabel.setText("y: " + gameMaker.getPlaceY());
		String name = gameMaker.getLevel().getName();
		levelLabel.setText("Level: " + name);
		String placement = null;
		if(gameMaker.getPlacement() == -1)
			placement = "Wall";
		else {
			placement = gameMaker.getObjectClasses()[gameMaker.getPlacement()].getName();
			placement = placement.substring(placement.lastIndexOf('.') + 1);
		}
		placementLabel.setText("Object: " + placement);
		btnDuration.setText("time: " + gameMaker.getLevel().getDuration());
	}

	@Override
	public void render(Graphics2D g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals("exit")) {
			System.exit(0);
		}
		else if(command.equals("new")) {
			gameMaker.newLevel();
		}else if(command.equals("save")) {
			gameMaker.saveLevel();
		}else if(command.equals("open")) {
			gameMaker.openLevel();
		}else if(command.equals("duration")) {
			if(gameMaker.getLevel() == null) return;
			JTextField input = new JTextField(gameMaker.getLevel().getDuration());
			JOptionPane.showConfirmDialog(null, input, "Change Level Duration", JOptionPane.DEFAULT_OPTION);
			try {
				int duration = Integer.parseInt(input.getText());
				gameMaker.getLevel().setDuration(duration);
			}catch(NumberFormatException e1) {
				if(input.getText().equals("infinite"))
					gameMaker.getLevel().setDuration(0);
			}
		}
	}
}
