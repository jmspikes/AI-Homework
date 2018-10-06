package homework4;

import java.util.Scanner;

public class Homework4 {

	public static void main(String[] args) {
		String[][] gameBoard = new String[3][3];
		int[][] board = new int[3][3];
		Input input = new Input();
		WinCheck winCheck = new WinCheck();
		int counter = 0;
		for(int i = 1; i < 10; i+=3) {
			for(int j = 0; j < 3; j++){
				board[counter][j] = (i+j);
				gameBoard[counter][j] = Integer.toString(board[counter][j]);
		}
			counter++;
		}
		
		while(true){

				//gets user input
				for(int i = 0; i < 3; i++){

					System.out.println(" "+(gameBoard[i][0])+" |"+" "+(gameBoard[i][1])+" | "+(gameBoard[i][2]));
					System.out.println("---+---+---");
				}
				//put in logic to if game is won here, will print completed board
				if(winCheck.player) {
					System.out.println("Player wins!");
					break;
				}
				else if(winCheck.computer) {
					System.out.println("Computer wins!");
					break;
				}
				int move = input.givePromptReturnInput();
				while((!(move >= 1) || (move > 9))) {
					System.out.println("Invalid move!");
					move = input.givePromptReturnInput();
				}
				System.out.println();
				//draw the move on the board
				//gets row of where move should go
				int row = move < 4 ? 0 : move < 7 ? 1 : 2;
				//scales choice down to 2d array
				int where = move < 4 ? move-1 : move < 7 ? move-4 : move-7;
				gameBoard[row][where] = "X";
				board[row][where] = 1;
				winCheck.board = board;
				winCheck.checkBoard(row, where, 1);
				
				
		}
	}
}

class WinCheck{
	boolean player = false;
	boolean computer = false;
	int[][] board;
	
	void checkBoard(int row, int col, int id) {
		
		if(checkHorizontal(row) && id == 1) {
			player = true;
			return;
		}
		if(checkVertical(col) && id == 1) {
			player = true;
			return;
		}
		if(checkDiagonal() && id == 1) {
			player = true;
			return;
		}
		
		if(checkHorizontal(row) && id == 0) {
			computer = true;
			return;
		}
		if(checkVertical(col) && id == 0) {
			computer = true;
			return;
		}
		if(checkDiagonal() && id == 0) {
			computer = true;
			return;
		}
			
	}
	
	boolean checkHorizontal(int row) {
		
		if(board[row][0] == 1 && board[row][1] == 1 && board[row][2] == 1)
			return true;
		return false;
	}

	boolean checkVertical(int col) {
		
		if(board[0][col] == 1 && board[1][col] == 1 && board[2][col] == 1)
			return true;
		return false;	
	}	
	
	boolean checkDiagonal() {
		//only two possible diagonal win conditions to check
		if(board[0][0] == 1 && board[1][1] == 1 && board[2][2] == 1)
			return true;
		if(board[0][2] == 1 && board[1][1] == 1 && board[2][0] == 1)
			return true;
		return false;
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

/*
class Movement{

	int position;
	int[] board;
	int[] depths;
	boolean maxPlayer;
	Movement(int position, int[] board){

		this.position = position;
		this.board = board;
	}

	int findBestMove(){
		int move = 0;

		move = minimax(getDepths(),false);


		return move;
	}

	int minimax(){

		int bestVal = 1000;

	}

	int[] getDepths(){

	}


}*/