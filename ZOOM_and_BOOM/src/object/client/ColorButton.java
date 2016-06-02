package object.client;

import processing.core.PApplet;
import object.tool.Button;

public class ColorButton extends Button{
	
	private String colorName;
	private int money, color;

	
	// Constructor
	public ColorButton(PApplet p, float x, float y, int money, String colorName, int color){
		
		super(p, x, y, 70, color);
		this.money = money;
		this.color = color;
		this.colorName = colorName;
		
	}
	
	
	// draw good
	public void display(){
		
		// button
		super.display();
		
		// text
		parent.fill(0);
		parent.textSize(20);
		parent.text(colorName, x-25-scroll, y+80);
		parent.text("$"+money, x-20-scroll, y+100);
		
	}
	
	
	/**-----------------------------------------------
	 * �� seter and geter
	 ----------------------------------------------**/
	
	public int getMoney(){
		return money;
	}
	
	public int getColor(){
		return color;
	}
	
}
