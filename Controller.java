package homework2;
// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

import javax.swing.Timer;

import java.util.LinkedList;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

class Controller implements MouseListener
{
	Agent agent;
	Model model; // holds all the game data
	View view; // the GUI
	LinkedList<MouseEvent> mouseEvents; // a queue of mouse events

	Controller() {
		this.agent = new Agent();
		this.mouseEvents = new LinkedList<MouseEvent>();
	}

	private void init() throws Exception {
		this.model = new Model(this);
		this.model.initGame();
	}

	boolean update() {
		agent.update(model);
		model.update();
		return true;
	}

	Model getModel() { return model; }

	MouseEvent nextMouseEvent() {
		if(mouseEvents.size() == 0)
			return null;
		return mouseEvents.remove();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getY() < 600) {
			mouseEvents.add(e);
			if(mouseEvents.size() > 20) // discard events if the queue gets big
				mouseEvents.remove();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {    }
	@Override
	public void mouseEntered(MouseEvent e) {    }
	@Override
	public void mouseExited(MouseEvent e) {    }
	@Override
	public void mouseClicked(MouseEvent e) {    }

	static void playGame() throws Exception {
		Controller c = new Controller();
		c.init();
		c.view = new View(c, c.model); // instantiates a JFrame, which spawns another thread to pump events and keeps the whole program running until the JFrame is closed
		new Timer(20, c.view).start(); // creates an ActionEvent at regular intervals, which is handled by View.actionPerformed
	}

}
