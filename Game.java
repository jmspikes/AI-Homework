import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javafx.scene.shape.Path;

class Game
{

	static double[] evolveWeights()
	{
		// Create a random initial population
		Random r = new Random();
		Matrix population = new Matrix(100, 291);
		for(int i = 0; i < 100; i++)
		{
			double[] chromosome = population.row(i);
			for(int j = 0; j < chromosome.length; j++)
				chromosome[j] = 0.03 * r.nextGaussian();
		}
		// Evolve the population
		// todo: YOUR CODE WILL START HERE.
		//       Please write some code to evolve this population.
		//       (For tournament selection, you will need to call Controller.doBattleNoGui(agent1, agent2).)
		
		int fCounter = 0;
		int iterations = 0;

		

		//for(int simulate = 0; simulate < 1000; simulate++){
		while(true) {
		iterations++;
		if(iterations % 20 == 0) 
			System.out.println(getCurrentFitness(population));
		
		int decide = r.nextInt(4);
		ArrayList<Integer> winners = new ArrayList<Integer>();
		switch(decide){
			case 0:
				//System.out.println("mutated");
				for(int i = 0; i < population.rows(); i++){
					
					double probability = r.nextDouble();
					//introduce mutation
					if(probability > 0.70){
						double[] mutate = population.row(i);
		
						double m = 1 * r.nextGaussian();
						int randElement = r.nextInt(mutate.length);
						mutate[randElement] +=m;				
					}	
				}			
				break;
			
				
	
			case 1:
				//System.out.println("Fighting");
				try {
		
					int who = 0;			
					for(int i = 0; i < 10; i++){
						int a = r.nextInt(population.rows()-1);
						int b = r.nextInt(population.rows()-1);
						who = Controller.doBattleNoGui(new NeuralAgent(population.row(a)), new NeuralAgent(population.row(b)));
			
						double probability = r.nextDouble();
								
						//winner lives
						if(probability >= 0.48){
							//red won, delete blue
							if(who == 1){
								population.removeRow(a);
								winners.add(b);
							}
							//blue won, delete red
							else if (who == -1){
								population.removeRow(b);
								winners.add(a);
							}
						}
						else{
							//loser lives
							//red won, delete red
							if(who == 1){
								population.removeRow(b);
								winners.add(a);
							}
							//blue won, delete blue
							else if(who == -1){
								population.removeRow(a);
								winners.add(b);
							}
						}
					}	
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			
			case 2:
				//System.out.println("Replenish");
				while(population.rows() < 100){
				//parent to mate
				int firstP = r.nextInt(population.rows());
				double[] mate = population.row(firstP);
				double mScore = 0;
				for(int i = 0; i < mate.length; i++)
					mScore += mate[i];
				//scores winners to find compatible crossover
				ArrayList<Double> scores = new ArrayList<Double>();
				for(int i = 0; i < winners.size(); i++){
					double score = 0;
					double[] item = null;
					if(winners.get(i) < population.rows()){
						item = population.row(winners.get(i));
						for(int j = 0; j < item.length; j++)
							score += item[j];
						scores.add(score);
					}
				}
				
				double closest = 100;
				double[] candidate = null;
				//try to mate with winners, else just pick someone random
				for(int i = 0; i < scores.size(); i++){
					
					//first iteration to initialize closest value
					//second iteration ie mscore = 5 closest = 2 scores.get(i) == 4 
					//scores will be closer to mscore
					if(Math.abs(scores.get(i)-mScore) < Math.abs(closest - mScore)){
						if(winners.get(i) < population.rows()){
						closest = scores.get(i);
						candidate = population.row(winners.get(i));
						}
					}		
				}
		
				if(candidate == null)
					candidate = population.row(r.nextInt(population.rows()-1));
				double[] child = new double[291];
				for(int i = 0; i < child.length; i++){
					int which = r.nextInt(2);
					
					child[i] = which == 0 ? mate[i] : candidate[i];
				}
				population.takeRow(child);
			}
				
			break;
			
		
			case 3:
				try {
					//System.out.println("Fighting reflex agent");
					int result = 0;
					int a = r.nextInt(population.rows());
					result = Controller.doBattleNoGui(new ReflexAgent(), new NeuralAgent(population.row(a)));
					if(result == -1) {
						return population.row(a);
						//beat= a;
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
		}
		}
		// Return an arbitrary member from the population
		//return population.row(beat);
	}

	static double getCurrentFitness(Matrix m) {
		double biggusFitus = -1000;
		ArrayList<Double> fitness = new ArrayList<Double>();
		double cs = 0.0;
		for(int i = 0; i < m.rows(); i++) {
			double[] current = m.row(i);
			for(int j = 0; j < current.length; j++)
				cs += current[j];
			fitness.add(cs);
		}
	
		for(int i = 0; i < fitness.size(); i++) {
			if(fitness.get(i) > biggusFitus)
				biggusFitus = fitness.get(i);
		}
		
		return biggusFitus;
	}

	public static void main(String[] args) throws Exception
	{
		//double[] w = evolveWeights();
		
		
		FileInputStream fis = new FileInputStream("output.txt");
		DataInputStream dis = new DataInputStream(fis);
		double[] w = new double[291];
		for(int i = 0; i < w.length; i++) {
			w[i] = dis.readDouble();
		}
		/*FileOutputStream fos = new FileOutputStream("output.txt");
		DataOutputStream dos = new DataOutputStream(fos);
		
		for(int i = 0; i < w.length; i++)
			dos.writeDouble(w[i]);*/
		
		
		Controller.doBattle(new ReflexAgent(), new NeuralAgent(w));
		System.out.println();
	}

}