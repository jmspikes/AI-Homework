import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Main {

	public static void main(String[] args) {

		int counter = 0;
		Board b = new Board();

		while(true){
			counter++;
			if(b.y == 0 && b.x == b.scoreBoard[0].length-1){
				b.y = b.scoreBoard.length-1;
				b.x = 0;
				b.scoreBoard[0][b.scoreBoard[0].length-1] = b.reward;
				b.displayBoard[0][b.scoreBoard[0].length-1] = "G";
			}

			b.learner();
			
			if(counter % 168559772*18 == 0){
				b.print();
				System.exit(0);		
			}	
		}
	}

}


class Board{
	
	Double[][] scoreBoard;
	String[][] displayBoard;
	int[] moves;
	Random rand = null;
	double epsilon = 0.03;
	int y;
	int x;
	int bX = 20;
	int bY = 10;
	static double reward = 1;
	static double penalty = -1;
	ArrayList<Integer> validMoves;
	int prevAction;
	double baseTileAmount = 0.0;

	
	Board(){
		
		//creates boards
		scoreBoard = new Double[bY][bX];
		displayBoard = new String[bY][bX];
		rand = new Random();
		prevAction = -1;
		
		for(int i = 0; i < scoreBoard.length; i++){
			for(int j = 0; j < scoreBoard[0].length; j++){
				scoreBoard[i][j] = baseTileAmount;
				displayBoard[i][j] = " ";
			}
		}
		
		//sets initial pieces
		//player
		displayBoard[bY-1][0] = "S";
		//goal
		scoreBoard[0][bX-1] = reward;
		displayBoard[0][bX-1] = "G";
		//roadblocks
		for(int i = 0; i < scoreBoard.length; i++){
			if(i != scoreBoard.length/2-1 && i != scoreBoard.length/2){
			scoreBoard[i][scoreBoard[0].length/2-1] = penalty;
			displayBoard[i][scoreBoard[0].length/2-1] = "#";
			}
		}
		y = scoreBoard.length-1;
		x = 0;
		
	}
	
	void learner() {
		
		int xOrig = x;
		int yOrig = y;
		
		//returns an action to move
		int action = 0;
		action = getAction(action);
		//avoids going back and forth
		if(prevAction != -1)
			while(true){
				if((action == 1 && prevAction == 0) || (action == 0 && prevAction == 1) || (action == 3 && prevAction == 2) || (action == 2 && prevAction == 3) )
					action = getAction(action);
				else
					break;

			}
		prevAction = action; 

		doAction(action);

		
		int xX = x;
		int xY = y;
		
		int j_action = 0;
		j_action = getAction(j_action);
		doAction(j_action);
		
		int jX = x;
		int jY = y;

		double alphaK = 0.1;
		double gamma = 0.97;
		double amount = ((1 - alphaK) * scoreBoard[xY][xX]) + (alphaK * (scoreBoard[jY][jX] + (gamma*maxQ(jY,jX))));	

		scoreBoard[jY][jX] = amount;
		
		moveDisplayBoard(xY, xX, j_action);
		x = xX;
		y = xY;
		
	}
	
	void moveDisplayBoard(int y, int x, int action){
		
		if(action == 0)
			displayBoard[y][x] = ">";
		if(action == 1)
			displayBoard[y][x] = "<";
		if(action == 2)
			displayBoard[y][x] = "V";
		if(action == 3)
			displayBoard[y][x] = "^";
			
		
	}
	
	int getAction(int action){
		
		
		boolean[] moves = new boolean[4];
		
		
		//right
		x++;
		moves[0] = x < scoreBoard[0].length && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		x--;
		//left
		x--;
		moves[1] = x >= 0 && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		x++;
		//down
		y++;
		moves[2] = y < scoreBoard.length && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		y--;
		//up
		y--;
		moves[3] = y >= 0 && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		y++;
		
		
		validMoves = new ArrayList<Integer>();
		
		//adds each valid move to an array list, numbering scheme means whichever i we're at coresponds to r-l-d-u
		//just need to check array list value to tell which move is which
		for(int i = 0; i < 4; i++)
			if(moves[i] == true)
				validMoves.add(i);
		
		//gets random valid move
		if(rand.nextDouble() < epsilon){ 
			double amount;
			ArrayList<Double> toRemove = new ArrayList<Double>();
			for(int i = 0; i < validMoves.size(); i++){
			int testX = (int) (x + (amount = validMoves.get(i) == 0 ? 1 : (validMoves.get(i) == 1 ? -1 : 0)));
			int testY = (int) (y + (amount = validMoves.get(i) == 2 ? 1 : (validMoves.get(i) == 3 ? -1 : 0)));
			
			amount = getScoreOfLocation(testY, testX);
			toRemove.add(amount);
			}
			for(int i = 0; i < toRemove.size(); i++){
				if(toRemove.get(i) == penalty)
					validMoves.remove(i);
			}
			action = validMoves.get(rand.nextInt(validMoves.size()));
		}
		else{
			
			double max = -1000;
			double amount;
			action = validMoves.get(0);
			ArrayList<Double> toRemove = new ArrayList<Double>();
			//will loop over all valid moves and get the best move
			for(int i = 0; i < validMoves.size(); i++){
				int testX = (int) (x + (amount = validMoves.get(i) == 0 ? 1 : (validMoves.get(i) == 1 ? -1 : 0)));
				int testY = (int) (y + (amount = validMoves.get(i) == 2 ? 1 : (validMoves.get(i) == 3 ? -1 : 0)));
				
				amount = getScoreOfLocation(testY, testX);
				toRemove.add(amount);
				if(amount > max){
					max = amount;
					action = validMoves.get(i);
				}
			}
			
			//check to make sure we dont just have base tiles
			if(max == baseTileAmount){
				ArrayList<Integer> finalists = new ArrayList<Integer>();
				for(int i = 0; i < toRemove.size(); i++){
					if(toRemove.get(i) >= max)
						finalists.add(validMoves.get(i));
				}
				int chance = finalists.get(rand.nextInt(finalists.size()));
				action = chance;	
			}
			
		}
		
		return action;
	}
	
	
	double getScoreOfLocation(int y, int x){	
		return scoreBoard[y][x];
	}
	
	
	double maxQ(int y, int x) {
	
		double max = 0;
		boolean[] moves = new boolean[4];
		
		//right
		x++;
		moves[0] = x < scoreBoard[0].length && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		x--;
		//left
		x--;
		moves[1] = x >= 0 && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		x++;
		//down
		y++;
		moves[2] = y < scoreBoard.length && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		y--;
		//up
		y--;
		moves[3] = y >= 0 && !(x == 0 && y == scoreBoard.length-1) ? true : false;
		y++;
		
		
		double amount;
		//adds each valid move to an array list, numbering scheme means whichever i we're at coresponds to r-l-u-d
		//just need to check array list value to tell which move is which
		for(int i = 0; i < 4; i++)
			if(moves[i] == true){

				int testX = (int) (x + (amount = i == 0 ? 1 : (i == 1 ? -1 : 0)));
				int testY = (int) (y + (amount = i == 2 ? 1 : (i == 3 ? -1 : 0)));
				
				amount = getScoreOfLocation(testY, testX);
				if(amount > max)
					max = amount;
			}

		return max;
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
	
	public void print(){
		for(int i = 0; i < scoreBoard.length; i++){
			for(int j = 0; j < scoreBoard[0].length; j++){
				System.out.print(displayBoard[i][j]);
			}
			System.out.println();
		}
	}
	
}