import java.util.ArrayList;
import java.util.Scanner;

import javafx.util.Pair;

public class Homework4 {

	public static void main(String[] args) {
		String[] gameBoard = new String[9];
		Input input = new Input();
		WinCheck winCheck = new WinCheck();
		Computer computer;
		int counter = 0;

		for(int i = 1; i < 10; i++) {
			gameBoard[i-1] = Integer.toString(i);
		}
		while(true){

				//put in logic to if game is won here, will print completed board
				if(winCheck.player) {
					System.out.println("Player wins!");
					winCheck.printBoard();
					break;
				}
				else if(winCheck.computer) {
					System.out.println("Computer wins!");
					winCheck.printBoard();
					break;
				}
				//player move
				if(counter % 2 == 0) {
					//gets user input
					for(int i = 0; i < 9; i+=3){

						System.out.println(" "+(gameBoard[i])+" |"+" "+(gameBoard[i+1])+" | "+(gameBoard[i+2]));
						System.out.println("---+---+---");
					}
					int move = input.givePromptReturnInput()-1;
					while((!(move >= 0) || (move > 8))) {
						System.out.println("Invalid move!");
						move = input.givePromptReturnInput()-1;
					}
					System.out.println();
					/*
					//gets row of where move should go
					int row = move < 4 ? 0 : move < 7 ? 1 : 2;
					//scales choice down to 2d array
					int where = move < 4 ? move-1 : move < 7 ? move-4 : move-7;*/
					gameBoard[move] = "X";
					winCheck.board = gameBoard;
					winCheck.checkBoard();
				}
				
				//computer move
				if(counter % 2 == 1) {
					computer = new Computer(null, gameBoard, true);
					computer.calculateMove();
					
				}
				
				counter++;
		}
	}
}

class Computer{
	
	Computer parent;
	String[] currentBoard;
	boolean condition = false;
	boolean agent;
	WinCheck check;
	Integer value = null;
	
	Computer(Computer parent, String[] b, boolean agent){
		this.parent = parent;
		giveBoard(b);
		this.agent = agent;
		check = new WinCheck();
		check.board = currentBoard;
	}
	
	//makes copy of board to be used 
	void giveBoard(String[] b) {
		currentBoard = new String[9];
		for(int i = 0; i < 9; i++)
				currentBoard[i] = b[i];
	}
	
	void calculateMove() {
		
		//maximum choice for computer
		//get all possible moves
		ArrayList<Integer> openPos = new ArrayList<Integer>();
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for(int i = 0; i < 9; i++)
			if(currentBoard[i] != "X" && currentBoard[i] != "O")
				openPos.add(i);
		//for each move play until win condition
		if(agent) {
			for(Integer move : openPos) {
					while(true) {
					check.checkBoard();
					if(check.player) {
						value = -1;
						break;
					}
					if(check.computer) {
						value = 1;
						break;
					}
					if(check.draw && parent.value != null) {
						value = 0;
						break;
					}
					//find best move here
					currentBoard[move] = "O";
					check.checkBoard();						
					boolean flip = agent ? false : true;
					Computer next = new Computer(this, currentBoard, flip);
					next.check.printBoard();
					next.calculateMove();
					}
					scores.add(value);
			}
		} else {

			for(Integer move : openPos) {
				while(true) {
					check.checkBoard();
					if(check.player) {
						value = 1;
						break;
					}
					if(check.computer) {
						value = -1;
						break;
					}
					if(check.draw) {
						value = 0;
						break;
					}
					currentBoard[move] = "X";
					boolean flip = agent ? false : true;
					Computer next = new Computer(this, currentBoard, flip);
					next.check.printBoard();
					next.calculateMove();
			
				}
			}
		}

		
			
	}	
}

class WinCheck{
	boolean player = false;
	boolean computer = false;
	boolean draw = false;
	String[] board;
	
	void checkBoard() {
		
		checkHorizontal();
		checkVertical();
		checkDiagonal();
		checkDraw();
			
	}
	
	void checkHorizontal() {
		if((board[0] == "X" && board[1] == "X" && board[2] == "X") || 
		   (board[3] == "X" && board[4] == "X" && board[5] == "X") ||
		   (board[6] == "X" && board[7] == "X" && board[8] == "X"))
				player = true;
		if((board[0] == "O" && board[1] == "O" && board[2] == "O") || 
		   (board[3] == "O" && board[4] == "O" && board[5] == "O") ||
		   (board[6] == "O" && board[7] == "O" && board[8] == "O"))
				computer = true;
	}

	void checkVertical() {
		
		if((board[0] == "X" && board[3] == "X" && board[6] == "X") || 
		   (board[1] == "X" && board[4] == "X" && board[7] == "X") ||
		   (board[2] == "X" && board[5] == "X" && board[8] == "X"))
				player = true;
		if((board[0] == "O" && board[3] == "O" && board[6] == "O") || 
		   (board[1] == "O" && board[4] == "O" && board[7] == "O") ||
		   (board[2] == "O" && board[5] == "O" && board[8] == "O"))
				computer = true;
	}	
	
	void checkDiagonal() {
		if((board[0] == "X" && board[4] == "X" && board[8] == "X") ||
		   (board[2] == "X" && board[4] == "X" && board[6] == "X"))
		   		player = true;
		if((board[0] == "O" && board[4] == "O" && board[8] == "O") ||
		   (board[2] == "O" && board[4] == "O" && board[6] == "O"))
				computer = true;
	}
	
	void checkDraw() {
		for(int i = 0; i < 9; i++)
			if(board[i] != "X" && board[i] != "O")
				return;
		draw = true;
	}
	
	void printBoard() {
		for(int i = 0; i < 9; i+=3){

			System.out.println(" "+(board[i])+" |"+" "+(board[i+1])+" | "+(board[i+2]));
			System.out.println("---+---+---");
		}
		System.out.println();
	}
}
class Input{

	Scanner scanner = new Scanner(System.in);
	int choice;
	int givePromptReturnInput(){
		System.out.println("Choose move: ");
		int choice = scanner.nextInt();

		return choice > 0 ? choice : -1;
	}
}
