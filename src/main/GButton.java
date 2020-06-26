package main;

import java.awt.*;
import java.awt.event.*;

public class GButton implements BasicFunctionality, MouseListener, MouseMotionListener {
	
	private Rectangle body;
	private String text;
	private ActionListener action;
	private boolean hoveredOver = false;
	private boolean borderless = false;
	private boolean enabled = true;
	private boolean invisible = false;
	
	public GButton(int x, int y, int width, int height, String text, ActionListener action) {
		body = new Rectangle(x, y, width, height);
		this.text = text;
		this.action = action;
	}
	
	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}
	
	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Graphics2D g) {
		if(invisible) return;
		
		//draw body
		if(!enabled)
			g.setColor(Color.WHITE);
		else if(hoveredOver)
			g.setColor(Color.GRAY);
		else
			g.setColor(Color.LIGHT_GRAY);
		g.fill(body);
		
		//draw border
		if(!borderless) {
			g.setColor(Color.BLACK);
			g.draw(body);
		}
		
		//draw text at the center
		if(enabled)
			g.setColor(Color.BLACK);
		else
			g.setColor(Color.LIGHT_GRAY);
		g.setFont(new Font("impact", Font.PLAIN, 24));
		FontMetrics fm = g.getFontMetrics();
		int stringX = body.x + body.width / 2 - fm.stringWidth(text) / 2;
		int stringY = body.y + ((body.height - fm.getHeight()) / 2) + fm.getAscent();
		g.drawString(text, stringX, stringY);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(!enabled) return;
		if(body.contains(e.getX() - 9, e.getY() - 30))
			action.actionPerformed(null);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
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
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(!enabled) return;
		if(body.contains(e.getX() - 9, e.getY() - 30))
			hoveredOver = true;
		else
			hoveredOver = false;
	}
	
	public void reset() {
		hoveredOver = false;
	}
	
	//getters
	public String getText() {
		return text;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	//setters
	public void setText(String text) {
		this.text = text;
	}
	
	public void setBorderless(boolean borderless) {
		this.borderless = borderless;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
