package main;

import java.awt.event.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.awt.*;

public class Screen implements BasicFunctionality, KeyListener, MouseListener, MouseMotionListener {
	
	private ConcurrentLinkedDeque<BasicFunctionality> funcList = new ConcurrentLinkedDeque<>();
	private LinkedList<KeyListener> keyListeners = new LinkedList<>();
	private LinkedList<MouseListener> mouseListeners = new LinkedList<>();
	private LinkedList<MouseMotionListener> mouseMotionListeners = new LinkedList<>();
	protected LinkedList<GButton> buttons = new LinkedList<GButton>();
	
	@Override
	public void update() {
		Iterator<BasicFunctionality> iter = funcList.iterator();
		while(iter.hasNext()) iter.next().update();
	}
	
	@Override
	public void render(Graphics2D g) {
		Iterator<BasicFunctionality> iter = funcList.iterator();
		while(iter.hasNext()) iter.next().render(g);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		Iterator<KeyListener> iter = keyListeners.iterator();
		while(iter.hasNext()) iter.next().keyTyped(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		Iterator<KeyListener> iter = keyListeners.iterator();
		while(iter.hasNext()) iter.next().keyPressed(e);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		Iterator<KeyListener> iter = keyListeners.iterator();
		while(iter.hasNext()) iter.next().keyReleased(e);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Iterator<MouseListener> iter = mouseListeners.iterator();
		while(iter.hasNext()) iter.next().mouseClicked(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Iterator<MouseListener> iter = mouseListeners.iterator();
		while(iter.hasNext()) iter.next().mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Iterator<MouseListener> iter = mouseListeners.iterator();
		while(iter.hasNext()) iter.next().mouseReleased(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Iterator<MouseListener> iter = mouseListeners.iterator();
		while(iter.hasNext()) iter.next().mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Iterator<MouseListener> iter = mouseListeners.iterator();
		while(iter.hasNext()) iter.next().mouseExited(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		Iterator<MouseMotionListener> iter = mouseMotionListeners.iterator();
		while(iter.hasNext()) iter.next().mouseDragged(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Iterator<MouseMotionListener> iter = mouseMotionListeners.iterator();
		while(iter.hasNext()) iter.next().mouseMoved(e);
	}
	
	public void addFunctionality(BasicFunctionality func) {
		funcList.add(func);
	}
	
	public void addKeyListener(KeyListener listener) {
		keyListeners.add(listener);
	}
	
	public void addMouseListener(MouseListener listener) {
		mouseListeners.add(listener);
	}

	public void addMouseMotionListener(MouseMotionListener listener) {
		mouseMotionListeners.add(listener);
	}
	
	public void addButton(GButton button) {
		this.addFunctionality(button);
		this.addMouseListener(button);
		this.addMouseMotionListener(button);
		buttons.add(button);
	}
	
	public void reset() {
		for(GButton btn : buttons) btn.reset();
	}

}
