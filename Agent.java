package search;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Map;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Color;

class Agent {

	Integer x = null;
	Integer y = null;
	PlanRoute route;
	//ArrayList<MyState> r = new ArrayList<MyState>();
	MyState which;

	Agent(){
		
	}
	
	void drawPlan(Graphics g, Model m) {
		g.setColor(Color.red);
		g.drawLine((int)m.getX(), (int)m.getY(), (int)m.getDestinationX(), (int)m.getDestinationY());
	}

	void update(Model m)
	{
		Controller c = m.getController();
		while(true)
		{
			route = new PlanRoute(m, c);	
			MouseEvent e = c.nextMouseEvent();
			if(e == null && x == null)
				break;
			if(e != null) {
				x = e.getX();
				y = e.getY();
			}
			m.setDestination(x, y);
			
			//MyState goal = new MyState(0, null, 0);
			//goal.state.x = m.getDestinationX();
			//goal.state.y = m.getDestinationY();
			MyState current = new MyState(0,route.frontier.peek(),0);
			current.state.x = m.getX();
			current.state.y = m.getY();
			which = route.uniformCost(current);
			
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
			MyState select = null;
				select = which;
				if(select.parent != null) {
					while(select.parent.parent != null) {
						select = select.parent;
					}
				}
				//System.out.println("("+select.state.x+","+select.state.y+")");
				m.setDestination(select.state.x, select.state.y);
				break;
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

	PriorityQueue<MyState> frontier;
	TreeSet<MyState> beenThere;
	StateComparator compare; 
	CompareCost compareCost;
	Model link;
	Graphics g;
	boolean completed;
	
	PlanRoute(Model link, Controller vLink){
		
		compare = new StateComparator();
		compareCost = new CompareCost();
		frontier = new PriorityQueue<MyState>(compareCost);
		beenThere = new TreeSet<MyState>(compare);
		this.link = link;
		g = vLink.view.getGraphics();
		g.setColor(Color.yellow);
		completed = false;

	}

	public MyState uniformCost(MyState startState){

		beenThere.add(startState);
		frontier.add(startState);
		int a = (int) link.getDestinationX();
		int b = (int) link.getDestinationY();
		int j = 0;
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
			if(((int)(s.state.x/10) <= (int)(link.getDestinationX()/10)+10 && 
				(int)(s.state.x/10) >= (int)(link.getDestinationX()/10)    && 
				(int)(s.state.y/10) <= (int)(link.getDestinationY()/10)+10 &&
				(int)(s.state.y/10) >= (int)(link.getDestinationY()/10))) {
					return s;
				}
			for(int i = 1; i < 9; i++){
				MyState child = new MyState(0, s, i);
				g.fillOval((int)child.state.x, (int)child.state.y, 10, 10);

				float acost = calculateCost(s, child);
				if(beenThere.contains(child)) {
					MyState oldChild = beenThere.floor(child);
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
		
		
		return null;
	}
	
	 

	public float calculateCost(MyState current, MyState direction){
		
		float cost;
		if(current.state.x >= link.XMAX)
			return cost = 100;
		if(current.state.y >= link.YMAX)
			return cost = 100;
		if(current.state.x < 0)
			return cost = 100;
		if(current.state.y < 0)
			return cost = 100;
		
		return cost = 1.0f / link.getTravelSpeed(current.state.x, current.state.y);
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
		else if(a.state.x > b.state.x || b.state.y > b.state.y)
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
