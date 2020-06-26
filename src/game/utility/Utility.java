package game.utility;

import main.GameSystem;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.*;

public class Utility {
	
	public static int[][] read2DArray(BufferedReader br, int row, int col) throws IOException {
		int[][] arr = new int[row][col];
		for(int i = 0; i < row; ++i) {
			String[] tokens = br.readLine().split(" ");
			for(int j = 0; j < col; ++j) {
				arr[i][j] = Integer.parseInt(tokens[j]);
			}
		}
		return arr;
	}
	
	public static void write2DArray(BufferedWriter bw, int[][] arr) throws IOException {
		for(int i = 0; i < arr.length; ++i) {
			for(int j = 0; j < arr[i].length; ++j)
				bw.write("" + arr[i][j] + " ");
			bw.newLine();
		}
	}
	
	public static int readNumber(BufferedReader br) throws IOException {
		return Integer.parseInt(br.readLine());
	}
	
	public static void writeNumber(BufferedWriter bw, int num) throws IOException {
		bw.write("" + num);
		bw.newLine();
	}
	
	public static void drawCenteredString(Graphics2D g, String s, int offsetY) {
		int stringWidth = g.getFontMetrics().stringWidth(s);
		int stringHeight = g.getFontMetrics().getAscent();
		g.drawString(s, GameSystem.SCREEN_WIDTH / 2 - stringWidth / 2, GameSystem.SCREEN_HEIGHT / 2 + stringHeight / 2 + offsetY);
	}
	
	private static int orientation(Point p, Point vect_p1, Point vect_p2) {
		int value = (int) (
				(vect_p1.getX() - vect_p2.getX()) * (p.getY() - vect_p2.getY()) -
				(p.getX() - vect_p2.getX()) * (vect_p1.getY() - vect_p2.getY())
		);
		if(value > 0) return 1;
		else if(value < 0) return -1;
		else return 0;
	}
	
	private static boolean liesOnLine(Point p, Point line_p1, Point line_p2) {
		double x0 = p.getX();
		double y0 = p.getY();
		double x1 = line_p1.getX();
		double x2 = line_p2.getX();
		double y1 = line_p1.getY();
		double y2 = line_p2.getY();
		//check if the X and Y of the point is within the line's range of Xs and Ys
		return (x0 <= Math.max(x1, x2) && x0 >= Math.min(x1, x2)) &&
				(y0 <= Math.max(y1, y2) && y0 >= Math.min(y1, y2));
	}
	
	public static boolean checkLineCollision(Point p1, Point p2, Point p3, Point p4) {
		int orien1 = orientation(p1, p3, p4);
		int orien2 = orientation(p2, p3, p4);
		int orien3 = orientation(p3, p1, p2);
		int orien4 = orientation(p4, p1, p2);
		
		//special case: the two lines are collinear
		if(orien1 == 0 && orien2 == 0 && orien3 == 0 && orien4 == 0) {
			if(liesOnLine(p3, p1, p2) || liesOnLine(p4, p1, p2))
				return true;
			else
				return false;
		}
		
		//if the two points of a line segment have the same orientation(on the same 
		//side of the other line segment), then the two line segments don't intersect
		if(orien1 == orien2 || orien3 == orien4)
			return false;
		
		return true;
	}
	
	public static boolean checkLineCollision(Point line_p1, Point line_p2, Rectangle rect) {
		Point rect_p1 = new Point(rect.getX(), rect.getY());
		Point rect_p2 = new Point(rect.getX(), rect.getY() + rect.getHeight());
		
		//left
		if(checkLineCollision(line_p1, line_p2, rect_p1, rect_p2)) return true;
		
		//top
		rect_p2.setX(rect.getX() + rect.getWidth());
		rect_p2.setY(rect.getY());
		if(checkLineCollision(line_p1, line_p2, rect_p1, rect_p2)) return true;
		
		//right
		rect_p1.setX(rect_p2.getX());
		rect_p1.setY(rect_p2.getY() + rect.getHeight());
		if(checkLineCollision(line_p1, line_p2, rect_p1, rect_p2)) return true;
		
		//bottom
		rect_p2.setX(rect.getX());
		rect_p2.setY(rect.getY() + rect.getHeight());
		if(checkLineCollision(line_p1, line_p2, rect_p1, rect_p2)) return true;
		
		return false;
	}

}
