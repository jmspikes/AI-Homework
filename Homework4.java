import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) {


		Board board = new Board();
		String[] boardBefore = new String[9];
		while(true){
			board.printBoard();	
			if(board.oWin(board.gameBoard)){
				System.out.println("Computer wins!");
				break;
			}
			
			int which = board.promptAndResponse();
			while(which < 0 || which > 9 || board.gameBoard[which-1] == "X" || board.gameBoard[which-1] == "O"){
				System.out.println("Invalid move!");
				which = board.promptAndResponse();
			
			}
			//player update
			board.update(which-1, 0);
			
			if(board.draw(board.gameBoard)){
				board.printBoard();
				System.out.println("Draw!");
				break;
			}
			if(board.xWin(board.gameBoard)){
				board.printBoard();
				System.out.println("Player wins!");
				break;
			}
			for(int i = 0; i < board.gameBoard.length; i++)
				boardBefore[i] = board.gameBoard[i];
			ArrayList<Integer> possibleMoves = new ArrayList<Integer>();
			for(int i = 0; i < 9; i++){
				if(board.gameBoard[i] != "X" && board.gameBoard[i] != "O")
					possibleMoves.add(i);
			}

			ArrayList<Integer> scores = new ArrayList<Integer>();
			for(Integer move : possibleMoves){
			board.update(move, 1);
			if(board.oWin(board.gameBoard)){
				scores.add(1);
				//board.printBoard();
				break;
			}
			while(true){				
				board.computerMove("X");
				board.update(board.bestMove, 0);
					if(board.xWin(board.gameBoard)){
						scores.add(-1);
						//board.printBoard();
						break;
					}
					if(board.draw(board.gameBoard)){
						scores.add(0);
						//board.printBoard();
						break;
					}
				//board.printBoard();
				board.computerMove("O");
				board.update(board.bestMove, 1);
					if(board.draw(board.gameBoard)){
						scores.add(0);
						//board.printBoard();
						break;
					}
					if(board.oWin(board.gameBoard)){
						scores.add(1);
						//board.printBoard();
						break;
					}
				}
			for(int i = 0; i < boardBefore.length; i++)
				board.gameBoard[i] = boardBefore[i];
			}
			ArrayList<Integer> win = new ArrayList<Integer>();
			ArrayList<Integer> draw = new ArrayList<Integer>();
			ArrayList<Integer> lose = new ArrayList<Integer>();
			for(int i = 0; i < scores.size(); i++){
				if(scores.get(i) == 1){
					win.add(possibleMoves.get(i));
				}
				if(scores.get(i) == 0){
					draw.add(possibleMoves.get(i));
				}	
				if(scores.get(i) == -1){
					lose.add(possibleMoves.get(i));
				}	
			}
			if(!win.isEmpty()){
				if(win.contains(4)){
					board.update(4, 1);
					continue;
				}
				Random r = new Random();
				board.update(win.get(r.nextInt(win.size())), 1);
				continue;
			}
			if(!draw.isEmpty()){
				if(draw.contains(4)){
					board.update(4, 1);
					continue;
				}
				Random r = new Random();
				board.update(draw.get(r.nextInt(draw.size())), 1);
				continue;
			}
			if(!lose.isEmpty()){
				//if games that have been played all return losses then block inevitable wins
				int block = board.canWeWin("X", possibleMoves, board.gameBoard);
				if(block > -1)
					board.update(block, 1);
				else{
				Random r = new Random();
				board.update(lose.get(r.nextInt(lose.size())), 1);
				continue;
				}
			}	
		}
	}		
}


class Board{
	
	String[] gameBoard;
	String[] tryBoard;
	int bestMove;
	
	Board(){
		gameBoard = new String[9];
		for(int i = 0; i < gameBoard.length; i++)
			gameBoard[i] = Integer.toString(i+1);

	}
	
	Board(String[] tryBoard){
		
		this.tryBoard = new String[9];
		for(int i = 0; i < 9; i++)
			this.tryBoard[i] = tryBoard[i];
		
	}
	
	void computerMove(String id){
		
		/*
		 * What needs to happen:
		 * read open moves
		 * read possible opponents moves
		 * see if we can win 
		 * if we can't can we lose
		 * if not the state is a draw
		 * find move closest to other friendly pieces in open spaces
		 * place move, swap to opposing player and make same decision tree
		 * loop until win or loss
		 * mark that score as the score for that state
		 */
		Board simMove = new Board(gameBoard);
		ArrayList<Integer> open;
		ArrayList<Integer> like;
		int move = -1;
		open = simMove.openMoves();
		int win = simMove.canWeWin(id, open, simMove.tryBoard);
		if(win != -1){
			bestMove = win;
			return;
		}
		if(open.contains(4)){
			bestMove = 4;
			return;
		}
		int block = simMove.canOpponentWin(id, open, simMove.tryBoard);
		if(block != -1){
			bestMove = block;
			return;
		}
		boolean stop = false;
		if(block < 0){
			like = simMove.likeSpots(id);
			for(int i = 0; i < like.size(); i++){
				if(stop)
					break;
				for(int j = 0; j < open.size(); j++){
					if(stop)
						break;
					//diagonal
					if(open.get(j)%2 == 0){
						if(like.get(i) == 0 || like.get(i) == 2 || like.get(i) == 6 || like.get(i) == 8)
							if(simMove.tryBoard[4].equals("5"))
							{move = 4; stop = true; break;}
						else if(like.get(i) == 4){
							int op = open.get(j);
							if(op == 0)
								{move = 0; stop = true; break;}
							if(op == 2)
								{move = 2; stop = true; break;}
							if(op == 6)
								{move = 6; stop = true; break;}
							if(op == 8)
								{move = 8; stop = true; break;}
						}
					}
					//horizontal
					if(Math.abs(like.get(i) - open.get(j)) == 1){
						move = open.get(j);
						stop = true;
						break;
					}
					//vertical
					if(Math.abs(like.get(i) - open.get(j)) == 3){
						move = open.get(j);
						stop = true;
						break;
					}

				}
			}
		}
		
		if(move == -1)
			bestMove = open.get(0);
		else
			bestMove = move;
	}
	
	ArrayList<Integer> openMoves(){
		ArrayList<Integer> items = new ArrayList<Integer>();
		for(int i = 0; i < 9; i++){
			if(this.tryBoard[i] != "X" && this.tryBoard[i] != "O")
				items.add(i);
		}
		return items;	
	}
	ArrayList<Integer> likeSpots(String id){
		ArrayList<Integer> items = new ArrayList<Integer>();
		for(int i = 0; i < 9; i++){
			if(tryBoard[i] == id)
				items.add(i);
		}
		return items;	
	}
	
	int canOpponentWin(String id, ArrayList<Integer> moves, String[] boardToCheck){
		
		String opponent = (id == "X") ? "O" : "X";
		
		//if opponent can win return that location
		for(int i = 0; i < moves.size(); i++){
			int m = moves.get(i);
			boardToCheck[m] = opponent;
			//if opponent is X then check if X wins, if opponent is O then check if O wins
			if(opponent == "X" ? xWin(boardToCheck) : oWin(boardToCheck))
				return m;
			else
				boardToCheck[m] = Integer.toString(m+1);	
		}
		return -1;
	}
	
	int canWeWin(String id, ArrayList<Integer> moves, String[] boardToCheck){
				
		for(int i = 0; i < moves.size(); i++){
			int m = moves.get(i);
			boardToCheck[m] = id;
			//if we're X and X wins return that location, if we're O and O wins return that location
			if(id == "X" ? xWin(boardToCheck) : oWin(boardToCheck))
				return m;
			else
				boardToCheck[m] = Integer.toString(m+1);	
		}
		
		return -1;
	}
	
	void printBoard(){
		for(int i = 0; i < 9; i+=3){
			System.out.println(" "+gameBoard[i]+" | "+gameBoard[(i+1)]+" | "+gameBoard[(i+2)]);
			if(i != 6)
				System.out.println("---+---+---");
			
		}
		System.out.println();
	}
	
	void update(int which, int id){
		if(id == 0) 
			gameBoard[which] = "X";
		else
			gameBoard[which] = "O";

	}
	
	int promptAndResponse(){
		int response = 0;
		Scanner in = new Scanner(System.in);
		System.out.println("Your move: ");
		response = in.nextInt();
		return response;
	}
	
	boolean xWin(String[] gameBoard){
		//horizontal check
		if((gameBoard[0].equals(gameBoard[1]) && gameBoard[1].equals(gameBoard[2]) && gameBoard[0] == "X") ||
		   (gameBoard[3].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[5]) && gameBoard[3] == "X") ||
		   (gameBoard[6].equals(gameBoard[7]) && gameBoard[7].equals(gameBoard[8]) && gameBoard[6] == "X") ||
		//diagonal check
		   (gameBoard[0].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[8]) && gameBoard[0] == "X") || 
		   (gameBoard[2].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[6]) && gameBoard[2] == "X") ||
		//horizontal check
		   (gameBoard[0].equals(gameBoard[3]) && gameBoard[3].equals(gameBoard[6]) && gameBoard[0] == "X") ||
		   (gameBoard[1].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[7]) && gameBoard[1] == "X") ||
		   (gameBoard[2].equals(gameBoard[5]) && gameBoard[5].equals(gameBoard[8]) && gameBoard[2] == "X"))
				return true;		
		return false;
	}
	
	boolean oWin(String[] gameBoard){
		//horizontal check
		if((gameBoard[0].equals(gameBoard[1]) && gameBoard[1].equals(gameBoard[2]) && gameBoard[0] == "O") ||
		   (gameBoard[3].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[5]) && gameBoard[3] == "O") ||
		   (gameBoard[6].equals(gameBoard[7]) && gameBoard[7].equals(gameBoard[8]) && gameBoard[6] == "O") ||
		//diagonal check
		   (gameBoard[0].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[8]) && gameBoard[0] == "O") || 
		   (gameBoard[2].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[6]) && gameBoard[2] == "O") ||
		//horizontal check
		   (gameBoard[0].equals(gameBoard[3]) && gameBoard[3].equals(gameBoard[6]) && gameBoard[0] == "O") ||
		   (gameBoard[1].equals(gameBoard[4]) && gameBoard[4].equals(gameBoard[7]) && gameBoard[1] == "O") ||
		   (gameBoard[2].equals(gameBoard[5]) && gameBoard[5].equals(gameBoard[8]) && gameBoard[2] == "O"))
				return true;		
		return false;	
	}
	
	boolean draw(String[] gameBoard){
		for(int i = 0; i < gameBoard.length; i++)
			if(gameBoard[i] != "X" && gameBoard[i] != "O")
				return false;
		return true;
	}
	
}
