package main;

import java.io.File;
import java.util.Random;
import java.util.ArrayList;
import java.awt.Color;
import processing.core.PImage;
import object.client.Splash;
import object.tool.Timer;
import object.tool.Button;

//TODO start animate

public class Game {
	
	// content
	private MainApplet parent;
	private PImage img;
	private Button ctrlBtn;
	private Timer timer;
	private ArrayList<Splash> splash, remove;
	
	// variable & constant
	private boolean isFrame=false, isPlay=false;
	private int height=450, width=800, lastTime=0, imageNumber;
	private float startX, startY, frameX, frameY, lineW=(float)2.5;
	
	// resources
	private String path = new String("src/resource/pic_rsc");
	private String[] list;
	private Random r = new Random();
	
	
	// Constructor
	public Game(MainApplet p){
		
		parent = p;
		
		// open file and load all file name
		File folder = new File(path);
		list = folder.list();
		
		// load image
		imageNumber = r.nextInt(list.length);
		img = parent.loadImage(path + "/" + list[imageNumber]);
		
		// set tool bar
		ctrlBtn = new Button(parent, 760, 40, 50, new Color(130, 180, 150).getRGB());
		timer = new Timer(p, 690, 40, 50, 5);
		
		// set splash list
		splash = new ArrayList<Splash>();
		remove = new ArrayList<Splash>();
		
	}

	
	// update screen content
	public void display(){
		
		if(isPlay){
			
			// draw image
			parent.image(img, 0, 0, width, height);
			
			// draw splash
			for(Splash s : splash){
				s.display();
				if(s.getTrans()==0) remove.add(s);
			}
			for(Splash s: remove){
				splash.remove(s);
			}
			remove.clear();
			
			// draw green frame
			if(isFrame){
				parent.stroke(130, 210, 75);
				parent.strokeWeight(lineW*2);
				parent.fill(130, 210, 75, 100);
				parent.rect(startX, startY, frameX, frameY);
			}
			parent.noStroke();
			
			// time related: timer, change image
			if(parent.millis()-lastTime>1000){
				if(timer.getValue()==0){
					frameEnd(true);
				} else {
					timer.work();
					lastTime = parent.millis();
				}
			}
			
		} else {
			
			parent.background(200, 200, 0);
			
		}
		
		// draw tool bar
		parent.fill(125, 125, 125, 150);
		parent.rect(650, 0, 150, 80);
		timer.display();
		ctrlBtn.display();
	}
	
	
	// start to frame object => draw green frame
	public void frameStart(){
		
		isFrame = true;
		startX = parent.mouseX;
		startY = parent.mouseY;
		
	}
	
	
	// end to frame object => frame disappear and transmit answer
	public void frameEnd(boolean timeout){
		
		// prevent regarding clicking as framing
		isFrame = false;
		if(startX==parent.mouseX&&startY==parent.mouseY) return;
		
		// transmit answer
		if(!timeout) sendFrame();
		
		// reset variable
		frameX = 0;
		frameY = 0;
		
		// change image
		int temp = imageNumber;
		while (imageNumber==temp) imageNumber = r.nextInt(list.length);
		img = parent.loadImage(path + "/" + list[imageNumber]);
		
		// restart timer
		timer.reset();
		lastTime = parent.millis();
		
	}

	
	// send framing answer to server
	private void sendFrame(){
		
		parent.thread.send("frameEnd");
		parent.thread.send(list[imageNumber]);
		parent.thread.send(startX);
		parent.thread.send(startY);
		parent.thread.send(frameX);
		parent.thread.send(frameY);
		
	}

	
	// framing object
	public void frame(){
		
		if(parent.mouseX>=lineW&&parent.mouseX<width-lineW) frameX = parent.mouseX-startX;
		else if(parent.mouseX<lineW) frameX = lineW-startX;
		else frameX = width-lineW-startX;
		if(parent.mouseY>=lineW&&parent.mouseY<height-lineW) frameY = parent.mouseY-startY;
		else if(parent.mouseY<0) frameY = lineW-startY;
		else frameY = height-lineW-startY;
		
	}

	
	// to see whether mouse event is in game region
	public boolean inGame(){
		
		if(parent.mouseX>=lineW&&parent.mouseX<width-lineW&&parent.mouseY>=lineW&&parent.mouseY<height-lineW)
			return true;
		else return false;
		
	}

	
	// start to play game
	public void gameStart(){
		
		isPlay = true;
		timer.reset();
		lastTime = parent.millis();
		
	}

	
	// close the game process
	public void gameEnd(){
		
		isPlay = false;
		//TODO confirm earned money and added
		
	}
	
	
	// save money temporarily and show animate
	public void answerCorrect(){
		//TODO
	}
	
	
	// show animate
	public void answerWrong(){
		//TODO
	}

	
	// add new splash
	public void addSplash(int color){
		splash.add(new Splash(parent, r.nextInt(800)-210, r.nextInt(450)-170, 10, color));
	}

	
	/**-----------------------------------------------
	 * �� seter and geter
	 ----------------------------------------------**/
	
	public boolean isFrame(){
		return isFrame;
	}

	public boolean isPlay(){
		return isPlay;
	}
	
	public Button getGameControlButton(){
		return ctrlBtn;
	}
	
}