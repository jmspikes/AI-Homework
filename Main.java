import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Controller c = new Controller();
        c.start();
    }
}

class Controller{

    void start(){
        WinCheck win = new WinCheck();
        String[] board = new String[9];
        String player = "X";

        for (int i = 0; i < 9; i++)
            board[i] = Integer.toString(i + 1);
        win.updateBoard(board);
        Input input = new Input();
        while (true) {
            if(player.equals("X")) {
                win.printBoard();
                int move = input.givePromptReturnInput() - 1;
                while ((!(move >= 0) || (move > 8))) {
                    System.out.println("Invalid move!");
                    move = input.givePromptReturnInput() - 1;
                }
                board[move] = "X";
                win.updateBoard(board);
                player = "O";
            }
            else {
                Computer computer = new Computer(win);
                computer.bestMove(board, "O");
                player = "X";
            }
            if(win.checkDraw() || win.checkBoard("X") || win.checkBoard("O")) {
                if(win.checkBoard("X"))
                    win.winner = "X";
                if(win.checkBoard("O"))
                    win.winner = "O";
                break;
            }
        }
        win.printBoard();
        if(win.checkDraw())
            System.out.println("Draw!");
        else {
            if (win.winner.equals("X"))
                System.out.println("Player wins!");
            else
                System.out.println("Computer wins!");
        }
    }
}

class Computer{

    WinCheck win;
    String player;
    Computer(WinCheck win) {
        this.win = win;
    }
    void bestMove(String[] board, String player){
        Score score;

        this.player = player;
        if(player.equals("X"))
            score = maxTurn(board, 0);
        else
            score = minTurn(board, 0);
        Move best = score.move;
        board[best.location] = player;
        win.updateBoard(board);
        }

        Score maxTurn(String[] board, int depth) {
            if (win.checkDraw() || win.checkBoard("X") || win.checkBoard("O"))
                return new Score(total(board, depth), null);

            Score max = new Score(Integer.MIN_VALUE, new Move(-1));

            for (int i = 0; i < 9; i++) {
                if (board[i].equals(Integer.toString(i + 1))) {
                    board[i] = "X";
                    win.updateBoard(board);
                    Score current = minTurn(board, depth + 1);
                    if (current.score > max.score) {
                        max.score = current.score;
                        max.move.location = i;
                    }
                    board[i] = Integer.toString(i + 1);
                    win.updateBoard(board);
                }
            }

            return max;
        }

        Score minTurn(String[] board, int depth){
            if (win.checkDraw() || win.checkBoard("X") || win.checkBoard("O"))
                return new Score(total(board, depth), null);

            Score min = new Score(Integer.MAX_VALUE, new Move(-1));
            for(int i = 0; i < 9; i++){
                if(board[i].equals(Integer.toString(i+1))){
                    board[i] = "O";
                    win.updateBoard(board);
                    Score current = maxTurn(board, depth + 1);
                    if(current.score < min.score){
                        min.score = current.score;
                        min.move.location = i;
                    }

                    board[i] = Integer.toString(i+1);
                    win.updateBoard(board);
                }
            }

            return min;
        }

        int total(String[] board, int depth){
            if(win.checkBoard("X")){
                return 10-depth;
            }
            else if(win.checkBoard("O"))
                return depth-10;
            return 0;
        }
}

class Score{
    int score;
    Move move;
    Score(int score, Move move){
        this.score = score;
        this.move = move;
    }
}

class Move{
    int location;
    Move(int location){
        this.location = location;
    }
}


class WinCheck{
    String[] board;
    Integer score = null;
    String winner = null;
    void updateBoard(String[] board){
        if(this.board == null)
            this.board = new String[9];
        for(int i = 0; i < 9; i++)
            this.board[i] = board[i];
    }

    void updateScore(){
        if(checkBoard("X")) {
            score = 10;
            winner = "X";
        }
        if(checkBoard("O")) {
            score = -10;
            winner = "O";
        }
        else
            score = 0;
    }

    boolean checkBoard(String player) {

        if(checkHorizontal(player))
            return true;
        if(checkVertical(player))
            return true;
        if(checkDiagonal(player))
            return true;
        return false;
    }
    boolean checkHorizontal(String player) {
        if((board[0].equals(player) && board[1].equals(player) && board[2].equals(player)) ||
                (board[3].equals(player) && board[4].equals(player) && board[5].equals(player)) ||
                (board[6].equals(player) && board[7].equals(player) && board[8].equals(player)))
            return true;
        return false;
    }

    boolean checkVertical(String player) {
        if((board[0].equals(player) && board[3].equals(player) && board[6].equals(player)) ||
                (board[1].equals(player) && board[4].equals(player) && board[7].equals(player)) ||
                (board[2].equals(player) && board[5].equals(player) && board[8].equals(player)))
            return true;
        return false;
    }

    boolean checkDiagonal(String player) {
        if((board[0].equals(player) && board[4].equals(player) && board[8].equals(player)) ||
                (board[2].equals(player) && board[4].equals(player) && board[6].equals(player)))
            return true;
        return false;
    }

    boolean checkDraw() {
        for(int i = 0; i < 9; i++)
            if(board[i] != "X" && board[i] != "O")
                return false;
        return true;
    }

    void printBoard() {
        for(int i = 0; i < 9; i+=3){
            System.out.println(" "+(board[i])+" |"+" "+(board[i+1])+" | "+(board[i+2]));
            System.out.println("---+---+---");
        }
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
