package game.utility;

public class Point {
	
	private double x, y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	//getters
	public double getY() {
		return y;
	}
	
	public double getX() {
		return x;
	}
	
	public int getIntX() {
		return (int)x;
	}
	
	public int getIntY() {
		return (int)y;
	}
	
	//setters
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "(" + x + " " + y + ")";
	}
}
