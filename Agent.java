package homework2;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

	PlanRoute route;
	//ArrayList<MyState> r = new ArrayList<MyState>();
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
				goalState = new MyState(0, null, 0);
				goalState.state.x = e.getX();
				goalState.state.y = e.getY();
				g = c.view.getGraphics();	
				g.setColor(Color.yellow);
			}
			m.setDestination(goalState.state.x, goalState.state.y);
			//MyState goal = new MyState(0, null, 0);
			//goal.state.x = m.getDestinationX();
			//goal.state.y = m.getDestinationY();
			MyState current = new MyState(0,null,0);
			current.state.x = m.getX();
			current.state.y = m.getY();
			which = route.uniformCost(current, goalState);
			drawFrontier();
			/*
			MyState iterator = which.parent;
			//crawl back up the parent chain to find next move, stop at first move
			//this is a hack but it should work I guess
			if(iterator != null) {
				if(iterator.parent != null) {
					while(iterator.parent.parent != null) {
							iterator = iterator.parent;
					}
				}
			}*/
			//ArrayList<Float> xW = new ArrayList<Float>();
			//ArrayList<Float> yW = new ArrayList<Float>();
			if(which != null){
				if(which.parent!=null){
					while(which.parent.parent != null){
						which = which.parent;
					}
				}
			}
				
			//System.out.println("("+select.state.x+","+select.state.y+")");
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
	
	PlanRoute(Model link, Controller vLink){
	
		this.link = link;
		g = vLink.view.getGraphics();
		g.setColor(Color.yellow);
		completed = false;

	}

	public MyState uniformCost(MyState startState, MyState goalState){
		
		StateComparator compare = new StateComparator();
		CompareCost compareCost = new CompareCost();
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
			//since we can click on an x,y that isnt a value of 10 need to make a rang
			/*if((s.state.x <= link.getDestinationX() + 10 && 
			    s.state.x >= link.getDestinationX() - 10) && 
			   (s.state.y <= link.getDestinationY() + 10 &&
				s.state.y >= link.getDestinationY() - 10)) {
				this.complete = true;
				return s;
			}*/
			if(((int)(s.state.x) <= (int)(link.getDestinationX()+10) && 
				(int)(s.state.x) >= (int)(link.getDestinationX())    && 
				(int)(s.state.y) <= (int)(link.getDestinationY()+10) &&
				(int)(s.state.y) >= (int)(link.getDestinationY()))) {
				//	g.fillOval((int)child.state.x, (int)child.state.y, 10, 10);

					
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
				//	g.fillOval((int)child.state.x, (int)child.state.y, 10, 10);

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
		/*
		float cost = 0;
		//gets travel speed of the current tile
		float travelSpeed = link.getTravelSpeed(current.state.x, current.state.y);
		float distanceX = direction.state.x - current.state.x;
		float distanceY = direction.state.y - current.state.y;
		//float distanceX = link.getDestinationX() - link.getX();
		//float distanceY = link.getDestinationY() - link.getY();
		float dist = (float) Math.sqrt(distanceX*distanceX+distanceY*distanceY);
//		cost = travelSpeed / Math.max(travelSpeed, dist);
		cost = (float) (travelSpeed / link.getDistanceToDestination(0));
		
		/*
		//1-4 are non diagonal moves, > 4 need to calc diagonal
		if(direction.direction <=4) {
		cost += Math.abs(direction.state.x-current.state.x)*travelSpeed;
		cost += Math.abs(direction.state.y-current.state.y)*travelSpeed;
		}
		else {
			float xS = ((direction.state.x - current.state.x)*(direction.state.x - current.state.x));
			float yS = ((direction.state.y - current.state.y)*(direction.state.y - current.state.y));
			cost = (float)(Math.sqrt(xS+yS)*travelSpeed);
		}*/
		
		/*
		float speed = link.getTravelSpeed(current.state.x, current.state.y);
		float dx = direction.state.x - current.state.x;
		float dy = direction.state.y - current.state.y;
		float dist = (float) Math.sqrt(dx*dx + dy*dy);
		cost = speed / Math.max(speed, dist);*/
		
		
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
	public int compare(MyState a, MyState b)
	{

		if(a.cost < b.cost)
			return -1;
		else if(a.cost > b.cost)
			return 1;

		return 0;
	}

} 
