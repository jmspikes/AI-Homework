import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;


class Board{

	boolean[][] area;
	byte[] state;
	Main prev;
	int counter;
	int drawnBlocks;
	boolean goalReached = false;

	Board(Main p){
		//populates board with black spaces
		prev = p;
		area = new boolean[10][10];
		state = new byte[22];
		counter = 0;
		drawnBlocks = 0;
	}

	public void b(int x, int y){
		if(this.area[x][y] == false)
			this.counter++;
		this.area[x][y] = true;
		this.drawnBlocks++;
	}

		// Draw a 3-block piece
	public void shape(int id, int x1, int y1, int x2, int y2, int x3, int y3)
	{
		b(state[2 * id] + x1, state[2 * id + 1] + y1);
		b(state[2 * id] + x2, state[2 * id + 1] + y2);
		b(state[2 * id] + x3, state[2 * id + 1] + y3);
		//reset places we're no longer at to false
	}

	// Draw a 4-block piece
	public void shape(int id, int x1, int y1, int x2, int y2,
		int x3, int y3, int x4, int y4)
	{
	
		shape(id, x1, y1, x2, y2, x3, y3);
		b(state[2 * id] + x4, state[2 * id + 1] + y4);
	}

	public void tryMove(int id, int direction){

		Board alternate = new Board(this.prev);
		for(int i = 0; i < alternate.state.length; i++){
				alternate.state[i] = this.state[i];
		}
		alternate.area = new boolean[10][10];

		alternate.state[id]+=direction;
		alternate.generate();
		if(alternate.counter == this.drawnBlocks){
			System.out.println("State valid");
			this.state[id]+=direction;
			if(this.state[1] == -2){
				System.out.println("Goal reached");
				goalReached = true;
			}
			else
				System.out.println("Goal not reached");
		}
		else
			System.out.println("Invalid state");

	}
	
	public int generateMove(){
		
		
		return 0;
	}

	void generate(){

		for(int i = 0; i < 10; i++) { this.b(i, 0); this.b(i, 9); }
		for(int i = 1; i < 9; i++) { this.b(0, i); this.b(9, i); }
		b(1, 1); b(1, 2); b(2, 1);
		b(7, 1); b(8, 1); b(8, 2);
		b(1, 7); b(1, 8); b(2, 8);
		b(8, 7); b(7, 8); b(8, 8);
		b(3, 4); b(4, 4); b(4, 3);

		
		//red block
		//game.area[3][1] = game.area[3][2] = game.area[4][1] = game.area[4][2] = true;
		//red block
		shape(0, 1, 3, 2, 3, 1, 4, 2, 4);/*
		shape(1, 1, 5, 1, 6, 2, 6);
		game.shape(2,2, 5, 3, 5, 3, 6);
		game.shape(3, 3, 7, 3, 8, 4, 8);
		game.shape(4, 4, 7, 5, 7, 5, 8);
		game.shape(5, 6, 7, 7, 7, 6, 8);
		game.shape(6, 5, 4, 5, 5, 5, 6, 4, 5);
		game.shape(7, 6, 4, 6, 5, 6, 6, 7, 5);
		game.shape(8, 8, 5, 8, 6, 7, 6);
		game.shape(9, 6, 2, 6, 3, 5, 3);
		game.shape(10, 5, 1, 6, 1, 5, 2);*/
		printBoard();
	}


	void printBoard(){

		for(int i = 0; i < area.length; i++){
			for(int j = 0; j < area.length; j++){
				if(area[j][i]){
					System.out.print("[-]");
				}
				else
					System.out.print("   ");
			}
			System.out.println();
		}
	System.out.println("-----------------------------------------------------------");
	}

}

class StateComparator implements Comparator<Board>{
	public int compare(Board a, Board b)
	{
		for(int i = 0; i < 22; i++)
		{
			if(a.state[i] < b.state[i])
				return -1;
			else if(a.state[i] > b.state[i])
				return 1;
		}
		return 0;
	}
}



class Main{
	
	Board game = new Board(this);
	TreeSet<Board> set;
	StateComparator comp;

	public Main(){

		game = new Board(this);
		StateComparator comp = new StateComparator();
		set = new TreeSet<Board>(comp);
		driver();
	}
	
	public void driver(){
		game.generate();
		game.tryMove(1, 1);
		game.tryMove(1,1);
		game.tryMove(0, 1);
		game.tryMove(0, 1);
		game.tryMove(0, 1);
		game.tryMove(0, 1);
		game.tryMove(1, -1);
		game.tryMove(1, -1);
		game.tryMove(1, -1);
		game.tryMove(1, -1);
	}

	public static void main(String[] args){

		new Main();
	}


}
