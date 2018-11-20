package Homework7;

import java.util.Arrays;
import java.util.Random;

public class Main {

	public static void main(String[] args) {

		Board b = new Board();
		
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 20; j++){
				System.out.print(b.scoreBoard[i][j][0] + " ");
			}
			System.out.println();
		}
		
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 20; j++){
				System.out.print(b.displayBoard[i][j][0]);
			}
			System.out.println();
		}
	}

	//prints whatever object is passed to it
	public static void print(Object o){
		System.out.println(o.toString());
	}
	
}


class Board{
	
	double[][][] scoreBoard;
	String[][][] displayBoard;
	int currentX;
	int currentY;
	Random rand = null;
	double epsilon = 0.05;
	int action;
	int x;
	int y;
	
	Board(){
		
		//creates boards
		scoreBoard = new double[10][20][4];
		displayBoard = new String[10][20][4];
		rand = new Random();

		
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 20; j++){
				scoreBoard[i][j][0] = -1;
				displayBoard[i][j][0] = " ";
			}
		}
		
		//sets initial pieces
		//player
		scoreBoard[9][0][0] = 1;
		displayBoard[9][0][0] = "S";
		//goal
		scoreBoard[0][19][0] = 99;
		displayBoard[0][19][0] = "G";
		//roadblocks
		for(int i = 0; i < 10; i++){
			if(i != 5 && i != 4){
			scoreBoard[i][9][0] = -99;
			displayBoard[i][9][0] = "#";
			}
		}
		x = 0;
		y = 0;
	}
	
	void learner() {
		
		
		if(rand.nextDouble() < epsilon) {
			action = rand.nextInt(4);
		}
		
		else {
			
			action = 0;
			for(int candidate = 0; candidate < 4; candidate++) {
				if(scoreBoard[x][y][candidate] > scoreBoard[x][y][action])
					action = candidate;
				
				if(scoreBoard[x][y][action] == 0.0)
					action = rand.nextInt(4);		
			}
			
			doAction(action);
			int[] j = new int[2];
			j[0] = x;
			j[1] = y;
			
			int j_action = 0; 
			
			for(int candidate = 0; candidate < 4; candidate++) {
				
				if(scoreBoard[x][y][candidate] > scoreBoard[x][y][action])
					j_action = candidate;
				
				if(scoreBoard[x][y][action] == 0.0)
					j_action = rand.nextInt(4);		
				
			}
			
			double alphaK = 0.1;
			double gamma = 0.97;
			
			scoreBoard[x][y][action] = (1 - alphaK) * scoreBoard[x][y][action] + alphaK * (reward(scoreBoard, x, y, action) +  gamma*scoreBoard[x][y][j_action]);			
			
		}
	}
	
	double reward(double[][][] scoreBoard, int x, int y, int action) {
		
		double r = 0;
		
		
		return r;
	}
	
	void doAction(int action) {
	
		if(action == 0)
			x++;
		if(action == 1)
			x--;
		if(action == 2)
			y++;
		if(action == 3)
			y--;
		
		
	}
}
