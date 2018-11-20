package Homework7;

import java.util.Arrays;

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
	
	Board(){
		
		//creates boards
		scoreBoard = new double[10][20][1];
		displayBoard = new String[10][20][1];

		
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 20; j++){
				scoreBoard[i][j][0] = 0;
				displayBoard[i][j][0] = " ";
			}
		}

		//sets initial pieces
		//player
		scoreBoard[9][0][0] = 1;
		displayBoard[9][0][0] = "S";
		//goal
		scoreBoard[0][19][0] = 9;
		displayBoard[0][19][0] = "G";
		//roadblocks
		for(int i = 0; i < 10; i++){
			if(i != 5 && i != 4){

			scoreBoard[i][9][0] = -9;
			displayBoard[i][9][0] = "#";
			}
		}
		
		
	}
	
}