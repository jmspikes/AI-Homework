import java.util.ArrayList;

import java.util.PriorityQueue;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

	PlanRoute route;
	MyState which;
	MyState goalState;
	Graphics g = null;
	boolean UCS = true;
	

	Agent() {

	}

	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		if (goalState != null) {
			MyState iterator = new MyState(0, which,0);
			if (iterator != null) {
				if (iterator.parent != null) {
					while (iterator.parent.parent != null) {
						int x = (int)iterator.state.x;
						int y = (int)iterator.state.y;
						iterator = iterator.parent;
						g.drawLine(x, y,(int)iterator.state.x, (int) iterator.state.y);

					}
				}
			}
			
		}
		g.setColor(Color.YELLOW);
		if (route.frontier != null) {
			for (MyState d : route.frontier) {
				g.fillOval((int) d.state.x, (int) d.state.y, 10, 10);
			}
		}
	}

	void update(Model m) {
		Controller c = m.getController();

		while (true) {
			route = new PlanRoute(m, c);
			MouseEvent e = c.nextMouseEvent();
			if (e == null && goalState == null)
				break;
			if (e != null) {
				if (SwingUtilities.isRightMouseButton(e)) {
					UCS = false;
				} else {
					UCS = true;
				}
				goalState = new MyState(0, null, 0);
				goalState.state.x = e.getX();
				goalState.state.y = e.getY();
				g = c.view.getGraphics();
				g.setColor(Color.yellow);
			}
			m.setDestination(goalState.state.x, goalState.state.y);

			MyState current = new MyState(0, null, 0);
			current.state.x = m.getX();
			current.state.y = m.getY();
			if(UCS)
				which = route.uniformCost(current, goalState);
			else
				which = route.astar(current, goalState);
			MyState iterator = new MyState(0, which,0);
			if (iterator != null) {
				if (iterator.parent != null) {
					while (iterator.parent.parent != null) {
						iterator = iterator.parent;
					}
				}
			}

			m.setDestination(iterator.state.x, iterator.state.y);
			break;
		}

	

	}

	public static void main(String[] args) throws Exception {
		Controller.playGame();
	}
}

class MyState {
	double cost;
	MyState parent;
	State state;
	int direction;

	MyState(double cost, MyState parent, int direction) {
		this.cost = cost;
		this.parent = parent;
		this.direction = direction;
		state = new State(0, 0);

		if (parent != null) {
			// handles initializing the next state, 0 direciton reserved for init states
			if (direction == 0) {
				state.x = parent.state.x;
				state.y = parent.state.y;
				return;
			}
			// up-right-down-left
			if (direction == 1) {
				state.x = parent.state.x;
				state.y = parent.state.y - 10;
				return;
			}
			if (direction == 2) {
				state.x = parent.state.x + 10;
				state.y = parent.state.y;
				return;
			}
			if (direction == 3) {
				state.x = parent.state.x;
				state.y = parent.state.y + 10;
				return;
			}
			if (direction == 4) {
				state.x = parent.state.x - 10;
				state.y = parent.state.y;
				return;
			}
			// top right - bottom right - bottom left - top left
			if (direction == 5) {
				state.x = parent.state.x + 10;
				state.y = parent.state.y - 10;
				return;
			}
			if (direction == 6) {
				state.x = parent.state.x + 10;
				state.y = parent.state.y + 10;
				return;
			}
			if (direction == 7) {
				state.x = parent.state.x - 10;
				state.y = parent.state.y + 10;
				return;
			}
			if (direction == 8) {
				state.x = parent.state.x - 10;
				state.y = parent.state.y - 10;
				return;
			}
		}
	}

}

class PlanRoute {

	Model link;
	Graphics g;
	boolean completed;
	ArrayList<MyState> toDraw;
	PriorityQueue<MyState> frontier;

	PlanRoute(Model link, Controller vLink) {

		this.link = link;
		g = vLink.view.getGraphics();
		g.setColor(Color.yellow);
		completed = false;

	}

	public MyState uniformCost(MyState startState, MyState goalState) {

		StateComparator compare = new StateComparator();
		UCSCost ucsCost = new UCSCost();


		frontier = new PriorityQueue<MyState>(ucsCost);
		TreeSet<MyState> beenThere = new TreeSet<MyState>(compare);

		startState.cost = 0.0;
		startState.parent = null;
		beenThere.add(startState);
		frontier.add(startState);
		while (frontier.size() > 0) {
			MyState s = frontier.poll();

			if (s.state.x > link.XMAX)
				continue;
			if (s.state.x < 0)
				continue;
			if (s.state.y > link.YMAX)
				continue;
			if (s.state.y < 0)
				continue;

			if (((int) (s.state.x / 10) <= (int) (link.getDestinationX() / 10)
					&& (int) (s.state.x / 10) >= (int) (link.getDestinationX() / 10)
					&& (int) (s.state.y / 10) <= (int) (link.getDestinationY() / 10)
					&& (int) (s.state.y / 10) >= (int) (link.getDestinationY() / 10))) {

				return s;
			}

			for (int i = 1; i < 9; i++) {
				MyState child = new MyState(0, s, i);
				float acost = calculateCost(s, child);
					child.cost = acost;
				if (beenThere.contains(child)) {
					MyState oldChild = new MyState(acost, beenThere.floor(child), 0);
					if (s.cost + acost < oldChild.cost) {
						oldChild.cost = s.cost + acost;
						oldChild.parent = s;
					}
				} else {
					child.cost = s.cost + acost;
					child.parent = s;
					frontier.add(child);
					beenThere.add(child);
				}

			}

		}
		throw new RuntimeException("There is no path to the goal");
	}

	public MyState astar(MyState startState, MyState goalState) {

		StateComparator compare = new StateComparator();
		StarCost starCost = new StarCost();
		frontier = new PriorityQueue<MyState>(starCost);
		
		
		TreeSet<MyState> beenThere = new TreeSet<MyState>(compare);

		startState.cost = 0.0;
		startState.parent = null;
		beenThere.add(startState);
		frontier.add(startState);
		while (frontier.size() > 0) {
			MyState s = frontier.poll();
			if (s.state.x > link.XMAX)
				continue;
			if (s.state.x < 0)
				continue;
			if (s.state.y > link.YMAX)
				continue;
			if (s.state.y < 0)
				continue;

			if (((int) (s.state.x / 10) <= (int) (link.getDestinationX() / 10)
					&& (int) (s.state.x / 10) >= (int) (link.getDestinationX() / 10)
					&& (int) (s.state.y / 10) <= (int) (link.getDestinationY() / 10)
					&& (int) (s.state.y / 10) >= (int) (link.getDestinationY() / 10))) {

				return s;
			}

			for (int i = 1; i < 9; i++) {
				MyState child = new MyState(0, s, i);
				float acost = calculateCost(s, child);
					child.cost = acost;
				if (beenThere.contains(child)) {
					MyState oldChild = new MyState(acost, beenThere.floor(child), 0);
					if (s.cost + acost < oldChild.cost) {
						oldChild.cost = s.cost + acost;
						oldChild.state.heuristic = calcHeuristic(s, goalState);
						oldChild.parent = s;
					}
				} else {
					child.cost = s.cost + acost;
					child.state.heuristic = calcHeuristic(s, goalState);
					child.parent = s;
					frontier.add(child);
					beenThere.add(child);
				}

			}

		}
		throw new RuntimeException("There is no path to the goal");
	}
	
	public float calculateCost(MyState current, MyState direction) {

		float cost;
		if (direction.state.x >= link.XMAX)
			return cost = 0;
		if (direction.state.y >= link.YMAX)
			return cost = 0;
		if (direction.state.x < 0)
			return cost = 0;
		if (direction.state.y < 0)
			return cost = 0;

		cost = 1.0f / link.getTravelSpeed(direction.state.x, direction.state.y);
		return cost;
	}

	float calcHeuristic(MyState a, MyState b){
		float heuristic = Math.abs(a.state.x - b.state.x) + Math.abs(a.state.y - b.state.y);
		heuristic *= 0.02f;
		return heuristic;
	}
}

class State {

	float x;
	float y;
	float heuristic;
	
	State(float x, float y) {
		this.x = x;
		this.y = y;
		this.heuristic = 0;
	}
}

class StateComparator implements Comparator<MyState> {
	public int compare(MyState a, MyState b) {

		if ((int)a.state.x /10 < (int)b.state.x /10)
			return -1;
		if ((int)a.state.x /10 > (int)b.state.x / 10)
			return 1;
		if((int)a.state.y /10 < (int)b.state.y / 10)
			return -1;
		if((int)a.state.y / 10 > (int)b.state.y / 10)
			return 1;

		return 0;
	}

}

class UCSCost implements Comparator<MyState> {
	
	public int compare(MyState a, MyState b) {

		if (a.cost < b.cost)
			return -1;
		if (a.cost > b.cost)
			return 1;
	return 0;
	}

}

class StarCost implements Comparator<MyState> {
	
	public int compare(MyState a, MyState b) {

		if (a.cost + a.state.heuristic < b.cost + b.state.heuristic){
			return -1;
		}
		if (a.cost + a.state.heuristic > b.cost + b.state.heuristic){
			return 1;
		}
	return 0;
	}

}

/*
class Agent {

	PlanRoute route;
	MyState which;
	MyState goalState;
	Graphics g = null;
	Agent(){
		
	}
	
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		if(goalState!=null)
			g.drawLine((int)m.getX(), (int)m.getY(), (int)goalState.state.x, (int)goalState.state.y);
	}

	void update(Model m)
	{
		Controller c = m.getController();

		while(true)
		{
			route = new PlanRoute(m, c);	
			MouseEvent e = c.nextMouseEvent();
			if(e == null && goalState == null)
				break;
			if(e != null) {
				if(SwingUtilities.isRightMouseButton(e)){
					route.UCS = false;
				}
				else{
					route.UCS = true;
				}
				goalState = new MyState(0, null, 0);
				goalState.state.x = e.getX();
				goalState.state.y = e.getY();
				g = c.view.getGraphics();	
				g.setColor(Color.yellow);
			}
			m.setDestination(goalState.state.x, goalState.state.y);
			
			MyState current = new MyState(0,null,0);
			current.state.x = m.getX();
			current.state.y = m.getY();
			which = route.uniformCost(current, goalState);
			drawFrontier();
			
			if(which != null){
				if(which.parent!=null){
					while(which.parent.parent != null){
						which = which.parent;
					}
				}
			}
				
			m.setDestination(which.state.x, which.state.y);
			break;
		}
		
	}
	
	void drawFrontier(){
		if(route.frontier != null){
			for(MyState d : route.frontier)
					g.fillOval((int)d.state.x, (int)d.state.y, 10, 10);
		}
	}


	public static void main(String[] args) throws Exception
	{
		Controller.playGame();
	}
}

class MyState{
	double cost;
	MyState parent; 
	State state;
	int direction;

	MyState(double cost, MyState parent, int direction){
		this.cost = cost;
		this.parent = parent;
		this.direction = direction;
		state = new State(0, 0);
		
		if(parent != null) {
			//handles initializing the next state, 0 direciton reserved for init states
			if(direction == 0) {state.x = parent.state.x;    state.y = parent.state.y;    return;}
			//up-right-down-left
			if(direction == 1) {state.x = parent.state.x;    state.y = parent.state.y-10; return;}
			if(direction == 2) {state.x = parent.state.x+10; state.y = parent.state.y;    return;}
			if(direction == 3) {state.x = parent.state.x;    state.y = parent.state.y+10; return;}
			if(direction == 4) {state.x = parent.state.x-10; state.y = parent.state.y;    return;}
			//top right - bottom right - bottom left - top left
			if(direction == 5) {state.x = parent.state.x+10; state.y = parent.state.y-10; return;}
			if(direction == 6) {state.x = parent.state.x+10; state.y = parent.state.y+10; return;}
			if(direction == 7) {state.x = parent.state.x-10; state.y = parent.state.y+10; return;}
			if(direction == 8) {state.x = parent.state.x-10; state.y = parent.state.y-10; return;}
		}
	}

}

class PlanRoute{


	Model link;
	Graphics g;
	boolean completed;
	ArrayList<MyState> toDraw;
	PriorityQueue<MyState> frontier;
	boolean UCS = true;
	
	PlanRoute(Model link, Controller vLink){
	
		this.link = link;
		g = vLink.view.getGraphics();
		g.setColor(Color.yellow);
		completed = false;

	}

	public MyState uniformCost(MyState startState, MyState goalState){
		
		StateComparator compare = new StateComparator();
		CompareCost compareCost = new CompareCost();
		if(!UCS)
			compareCost.UCS = false;
		else
			compareCost.UCS = true;
		frontier = new PriorityQueue<MyState>(compareCost);
		TreeSet<MyState> beenThere = new TreeSet<MyState>(compare);
		
		startState.cost = 0.0;
		startState.parent = null;
		beenThere.add(startState);
		frontier.add(startState);
		while(frontier.size() > 0){
			MyState s = frontier.poll();
			
			if(s.state.x > link.XMAX) 
				continue;
			if(s.state.x < 0)
				continue;
			if(s.state.y > link.YMAX)
				continue;
			if(s.state.y < 0)
				continue;

			if(((int)(s.state.x/10) <= (int)(link.getDestinationX()/10) && 
				(int)(s.state.x/10) >= (int)(link.getDestinationX()/10)    && 
				(int)(s.state.y/10) <= (int)(link.getDestinationY()/10) &&
				(int)(s.state.y/10) >= (int)(link.getDestinationY()/10))) {			
					return s;
				}
			
			for(int i = 1; i < 9; i++){
				MyState child = new MyState(0, s, i);
				float acost = calculateCost(s, child);
					child.cost = acost;
				if(beenThere.contains(child)) {
					MyState oldChild = new MyState(acost, beenThere.floor(child), 0);
					if(s.cost + acost < oldChild.cost){
						oldChild.cost = s.cost + acost;
						oldChild.parent = s;
					}
				}
				else{
					child.cost = s.cost + acost;
					child.parent = s;
					frontier.add(child);
					beenThere.add(child);
				}

			}

		}		
		throw new RuntimeException("There is no path to the goal");
	}
	
	 

	public float calculateCost(MyState current, MyState direction){
		
		float cost;
		if(direction.state.x >= link.XMAX)
			return cost = 1;
		if(direction.state.y >= link.YMAX)
			return cost = 1;
		if(direction.state.x < 0)
			return cost = 1;
		if(direction.state.y < 0)
			return cost = 1;
		
		cost = 1.0f / link.getTravelSpeed(direction.state.x, direction.state.y);
		return cost;
	}
	
}

class State{

	float x;
	float y;
	
	State(float x, float y){
		this.x = x;
		this.y = y;
	}
}

class StateComparator implements Comparator<MyState>
{
	public int compare(MyState a, MyState b)
	{

		if(a.state.x < b.state.x || a.state.y < b.state.y)
			return -1;
		else if(a.state.x > b.state.x || a.state.y > b.state.y)
			return 1;

		return 0;
	}

} 

class CompareCost implements Comparator<MyState>
{
	int goalX;
	int goalY;
	boolean UCS = true;
	
	public int compare(MyState a, MyState b)
	{

		if(a.cost < b.cost)
			return -1;
		if(a.cost > b.cost)
			return 1;

		return 0;
	}

} */
