package main;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.Random;
import java.awt.Color;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import main.Client.ClientThread;
import object.server.Player;
import object.client.AttackWindow;
import object.client.ConfirmWindow;
import object.client.ImageButton;
import object.client.Splash;
import object.tool.AudioPlayer;

public class MainApplet extends PApplet{
	
	private static final long serialVersionUID = 1L;

	// sub window and state
	private Game game;
	private Market market;
	private Scoreboard scoreboard;
	public enum GameState {MAIN, PLAY, PLAYEND, ATTACK};
	public GameState state;
	
	// resources
	private ClientThread thread;
	private Random r = new Random();
	private static AudioPlayer click;
	private static AudioPlayer beAtk;
	
	// player content
	private Player player;
	private ArrayList<Player> list = new ArrayList<Player>();
	
	
	// initialize
	public void setup(){
		
		// set window size
		setSize(800, 1100);
		
		// create sub window
		game = new Game(this);
		market = new Market(this);
		scoreboard = new Scoreboard(this);
		state = GameState.MAIN;
		
		// set music
		click = new AudioPlayer(new File("resource/refrigerater2_O.wav"));
		click.setPlayCount(1);
		beAtk = new AudioPlayer(new File("resource/bomb.wav"));
		beAtk.setPlayCount(1);
		
	}
	
	
	// update screen content
	public void draw(){
		
		game.display();
		market.display();
		scoreboard.display();
		
		fill(0, 0, 0, 150);
		if(state==GameState.ATTACK) rect(0, 0, 1100, 650);
		else if(state!=GameState.MAIN) rect(0, 450, 800, 200);
		
	}
	
	
	// control mouse pressed
	public void mousePressed(){
		
		if(state==GameState.PLAY){
			if(game.inGame()) game.frameStart();
		}
		
	}
	
	
	// control mouse released
	public void mouseReleased(){
		
		if(state==GameState.PLAY){
			if(game.isFrame()) game.frameEnd(false);
		}
		
	}
	
	
	// control mouse clicked
	public void mouseClicked(){

		if(state==GameState.MAIN){
			
			// game process control
			if(game.getGameControlButton().inBtn()){
				player.setOnLine(true);
				player = new Player(player);
				thread.send(player);
				game.gameStart();
				state = GameState.PLAY;
			}
			
			// buy and use at market (general)
			ImageButton[] btns = market.getButtons();
			for(ImageButton btn : btns){
				
				click.play();
				
				if(btn.inBtn()){
					
					state = GameState.ATTACK;
					if(player.getScore() >= btn.getMoney()){
						
						// create new PApplet
						AttackWindow app = new AttackWindow(this, list, btn);
						app.init();
						app.start();
						app.setFocusable(true);
						
						// create new frame
						JFrame window = new JFrame("Attack!!!");
						app.setWindow(window);
						window.setContentPane(app);
						window.setSize(400, 600);
						window.setLocation(450, 40);
						window.setVisible(true);
						window.addWindowListener(new java.awt.event.WindowAdapter() {
						    @Override
						    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
						    	state = GameState.MAIN;
						    }
						});
							
					} else {
						
						// remind that player doesn't have enough money
						JOptionPane.showMessageDialog(null,"Sorry, your money is not enough!");
						state = GameState.MAIN;
						
					}
					
				}
				
			}
			
			// buy and use at market (Random)
			if(market.getRandomBtn().inBtn()){
				
				state = GameState.ATTACK;
				if(player.getScore() >= market.getRandomBtn().getMoney()){	
					calMoney(-market.getRandomBtn().getMoney());
					attacked(list.get(r.nextInt(list.size())).getName(), new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256)).getRGB());
				} else {
					// remind that player doesn't have enough money
					JOptionPane.showMessageDialog(null,"Sorry, your money is not enough!");
					state = GameState.MAIN;
				}
				
			}
			
			// buy shield
			if(market.getShieldBtn().inBtn()){
				
				state = GameState.ATTACK;	
				if(player.getScore() >= market.getShieldBtn().getMoney()){
					calMoney(-market.getShieldBtn().getMoney());
					player.buyShield();
				} else {
					// remind that player doesn't have enough money
					JOptionPane.showMessageDialog(null,"Sorry, your money is not enough!");
				}
				state = GameState.MAIN;
				
			}
			
		} else if(state==GameState.PLAY){
			
			// game process control
			if(game.getGameControlButton().inBtn()){
				state = GameState.PLAYEND;
				player.setOnLine(false);
				player = new Player(player);
				thread.send(player);
				game.checkMoney();
			}
			
		} else if(state==GameState.PLAYEND){
			
			// check money
			if(game.getWallet().in(mouseX, mouseY)){
				game.gameEnd();
				state = GameState.MAIN;
			}

		}
		
	}
	
	
	// control mouse moved
	public void mouseMoved(){
		
		if(state==GameState.MAIN){
			
			// game process control
			if(game.getGameControlButton().inBtn()) game.getGameControlButton().setOver(true);
			else game.getGameControlButton().setOver(false);
			
			// buy and use at market
			if(market.getRandomBtn().inBtn()) market.getRandomBtn().setOver(true);
			else market.getRandomBtn().setOver(false);
			if(market.getShieldBtn().inBtn()) market.getShieldBtn().setOver(true);
			else market.getShieldBtn().setOver(false);
			ImageButton[] btns = market.getButtons();
			for(ImageButton btn : btns){
				if(btn.inBtn()) btn.setOver(true);
				else btn.setOver(false);
			}
			
		} else if(state==GameState.PLAY){
			
			// game process control
			if(game.getGameControlButton().inBtn()) game.getGameControlButton().setOver(true);
			else game.getGameControlButton().setOver(false);
			
		} else if(state==GameState.PLAYEND){
			
			// check money
			if(game.getWallet().in(mouseX, mouseY)) game.getWallet().setOver(true);
			else game.getWallet().setOver(false);
			
		}

		
		
	}
	
	
	// control mouse dragged
	public void mouseDragged(){
		
		if(state==GameState.PLAY){
			if(game.isFrame()&&game.isPlay()) game.frame();
		}
		
	}
	
	
	// attack other players
	public void attacked(String name, int color){
		
		// transmit data to server
		thread.send("attack");
		thread.send(name);
		thread.send(color);
		
	}
	
	
	// be attacked by other players
	public void beAttacked(int color){
		
		if(player.getShield()>0){
			thread.send("protected");
			player.useShield();
			JOptionPane.showMessageDialog(null,"You have been attacked! But you used a shield!");
		} else{
			beAtk.play();
			game.addSplash(color);
			thread.send("picName");
			thread.send(game.getPicName());
			thread.send(game.getSplashList());
		}
		
	}
	
	
	// show screenshot of attack
	public void showPic(String picName, ArrayList<Splash> spl) {
		
		// create new PApplet
		PImage snapshot = loadImage("resource/pic_rsc/" + picName);
		ConfirmWindow confirm = new ConfirmWindow(snapshot, spl);
		confirm.init();
		confirm.start();
		confirm.setFocusable(true);

		// create new frame
		JFrame window = new JFrame("*~ Attack Sucessful ~*");
		window.setContentPane(confirm);
		window.setSize(800, 450);
		window.setLocation(270, 130);
		window.setVisible(true);
		window.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	state = GameState.MAIN;
		    }
		});
		
	}
	
	
	// attack fail
	public void attackNoEffect(){
		JOptionPane.showMessageDialog(null,"The victim used a shiled to defense your attack!\n Attack NO EFFECT!");
		state = GameState.MAIN;
	}
	
	
	// send object to server
	public void send(Object o){
		thread.send(o);
	}
	
	
	// modify money by amount
	public void calMoney(int amount){
		player.setScore(player.getScore()+amount);
		player = new Player(player);
		thread.send(player);
	}
	
	
	/**-----------------------------------------------
	 * �� seter and geter
	 ----------------------------------------------**/
	
	public void setPlayer(Player p){
		player = p;
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public ArrayList<Player> getList(){
		return list;
	}
	 
	public void resetReference(ArrayList<Player> list){
		this.list = list;
	}
	
	public void setClientThread(ClientThread thread){
		this.thread = thread; 
	}
	
	public Game getGame(){
		return this.game;
	}
	
}
