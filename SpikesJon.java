import java.util.ArrayList;

import java.util.PriorityQueue;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

class SpikesJon implements IAgent{
	
	float iter;
	int index;
	//0 for left, 1 for right
	float[] direction;
	Model m;
	PlanRoute route;
	MyState which;
	MyState goalState;


	SpikesJon(){

		direction = new float[] {1,1,1};

	}
	public void reset() {
	}

	public void update(Model m) {
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			if(m.getEnergySelf(i) > 0)
				chooseRole(m, i);
		}
	}



	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			if(!getMove(m, i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f))
				m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);

		}
	}

	void beDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Stay between the enemy and my flag
			if(!getMove(m, i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY)))
				m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

			// Throw boms if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS + (Model.MAX_THROW_RADIUS*.90))
				m.throwBomb(i, enemyX, enemyY);
		}
		else {
			// Guard the flag
			if(!getMove(m, i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG))
				m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb, rest
		if(m.getEnergySelf(i) < Model.BOMB_COST)
			if(!getMove(m, i, m.getX(i), m.getY(i)))
				m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}

	void beFlagAttacker(Model m, int i) {
		// Head for the opponent's flag
		if(!getMove(m, i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT))
			m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}

		// Try not to die
		avoidBombs(m, i);
	}

	void beAggressor(Model m, int i) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {

				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				if(!getMove(m, i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON)))
					m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
			else {

				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
					if(!getMove(m, i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)))
						m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
				}
				else {
					if(!getMove(m, i, myX, myY))
						m.setDestination(i, myX, myY); // Rest
				}
			}
		}

		// Try not to die
		avoidBombs(m, i);
	}


	void chooseRole(Model m, int  i){

		if(opponentsAlive(m) > 0)
			beAggressor(m, i);
		else
			beFlagAttacker(m, i);

	}


	int opponentsAlive(Model m){
		int numAlive = 0;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++)
			if(m.getEnergyOpponent(i) > 0)
				numAlive++;
		return numAlive;
	}


	boolean getMove(Model m, int sprite, float x, float y){
			while (true) {
			route = new PlanRoute(m);
			goalState = new MyState(0, null, 0);
			goalState.state.x = x;
			goalState.state.y = y;
			m.setDestination(sprite, goalState.state.x, goalState.state.y);

			MyState current = new MyState(0, null, 0);
			current.state.x = m.getX(sprite);
			current.state.y = m.getY(sprite);
			which = route.astar(current, goalState);
			MyState iterator = new MyState(0, which,0);
			if (iterator != null) {
				if (iterator.parent != null) {
					while (iterator.parent.parent != null) {
						iterator = iterator.parent;
					}
				}
			}

			m.setDestination(sprite, iterator.state.x, iterator.state.y);
			break;
		}

		if(which == null)
			return false;
		else 
			return true;
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
	boolean completed;
	ArrayList<MyState> toDraw;
	PriorityQueue<MyState> frontier;

	PlanRoute(Model link) {

		this.link = link;
		completed = false;

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

			if (((int) (s.state.x / 10) <= (int) (goalState.state.x / 10)
					&& (int) (s.state.x / 10) >= (int) (goalState.state.x / 10)
					&& (int) (s.state.y / 10) <= (int) (goalState.state.y / 10)
					&& (int) (s.state.y / 10) >= (int) (goalState.state.y / 10))) {

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
		return null;
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
