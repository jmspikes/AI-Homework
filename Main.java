package Homework6;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------

class Main
{
	static void test(SupervisedLearner learner, String challenge)
	{
		// Load the training data
		String fn = "C:\\Users\\Jon\\Desktop\\java\\data\\" + challenge;
		Matrix trainFeatures = new Matrix();
		trainFeatures.loadARFF(fn + "_train_feat.arff");
		Matrix trainLabels = new Matrix();
		trainLabels.loadARFF(fn + "_train_lab.arff");

		// Train the model
		learner.train(trainFeatures, trainLabels, fn);

		// Load the test data
		Matrix testFeatures = new Matrix();
		testFeatures.loadARFF(fn + "_test_feat.arff");
		Matrix testLabels = new Matrix();
		testLabels.loadARFF(fn + "_test_lab.arff");

		// Measure and report accuracy
		int misclassifications = learner.countMisclassifications(testFeatures, testLabels);
		System.out.println("Misclassifications by " + learner.name() + " at " + challenge + " = " + Integer.toString(misclassifications) + "/" + Integer.toString(testFeatures.rows()));
	}

	public static void testLearner(SupervisedLearner learner)
	{
		test(learner, "hep");
		test(learner, "vow");
		test(learner, "soy");
	}

	public static void main(String[] args)
	{
		testLearner(new BaselineLearner());
		testLearner(new DecisionTree());
		testLearner(new RandomForest(30));
	}
	
}


abstract class Node{
	abstract boolean isLeaf();
	abstract boolean isInterior();
}

class LeafNode extends Node{
	
	double[] label;
	
	LeafNode(Matrix lables){
		
		label = new double[lables.cols()];
		for(int i = 0; i < lables.cols(); i++)
		{
			if(lables.valueCount(i) == 0)
				label[i] = lables.columnMean(i);
			else
				label[i] = lables.mostCommonValue(i);
		}
		
	}
	
	boolean isLeaf() {return true;}
	
	boolean isInterior() {return false;}
}

class InteriorNode extends Node{
	
	int attribute;
	int feature;
	double pivot;
	Node a;
	Node b;
	
	InteriorNode(int attribute, double pivot, int feature, Node a, Node b){
		this.attribute = attribute;
		this.pivot = pivot;
		this.feature = feature;
		this.a = a;
		this.b = b;
	}
	
	boolean isLeaf() {return false;}
	boolean isInterior() {return true;}
}


class RandomForest extends SupervisedLearner{

	int amount;
	Matrix tf;
	Matrix tl;
	Random randy;
	Node root;
	RandomForest(int amount){
		this.amount = amount;
		randy = new Random(1500);
		
		
	}
	
	String name() {
		return "Random Forest";
	}

	

	void train(Matrix features, Matrix labels, String fn) {

		ArrayList<Integer> scores = new ArrayList<Integer>();
		ArrayList<Node> holder = new ArrayList<Node>();
		DecisionTree d;

		tf = new Matrix();
		tl = new Matrix();
		tf.copyMetaData(features);
		tl.copyMetaData(labels);
		
		for(int i = 0; i < amount; i++){
		// Load the test data
		Matrix testFeatures = new Matrix();
		testFeatures.loadARFF(fn + "_test_feat.arff");
		Matrix testLabels = new Matrix();
		testLabels.loadARFF(fn + "_test_lab.arff");
		d = new DecisionTree();
		Matrix copyf = new Matrix();
		copyf.copy(features);
		Matrix copyl = new Matrix();
		copyl.copy(labels);
		generateTrainingData(copyf, copyl);
		d.train(tf, tl, fn);
		root = d.root;
		//get amount of misclassifications
		int misclassifications = d.countMisclassifications(testFeatures, testLabels);
		holder.add(d.root);
		scores.add(misclassifications);
		}
		
		int minLoc = 0;
		int min = Integer.MAX_VALUE;
		for(int i = 0; i < scores.size(); i++){
			if(scores.get(i) < min){
				min = scores.get(i);
				minLoc = i;
			}
		}
		//DecisionTree f = new DecisionTree();
		//root = f.buildTree(matrixesF.get(minLoc), matrixesL.get(minLoc));
		root = holder.get(minLoc);
		holder.clear();
		
	}

	void predict(double[] in, double[] out) {

			Node n = root;
			InteriorNode node;
			LeafNode o;
			
			while(true){
				if(!n.isLeaf()){
					node =  (InteriorNode) n;
					if(node.feature == 0){
						
						if(in[node.attribute] < node.pivot)
							n =  node.a;
						else
							n =  node.b;
					}
					else{
						
						if(in[node.attribute] == node.pivot)
							n = node.a;
						else
							n = node.b;
					}
					
				}
					
				else{
					o = (LeafNode) n;
					n = (LeafNode) n;
					Vec.copy(out, o.label);
					break;
				}
			
			
		}
	}
	
	void generateTrainingData(Matrix features, Matrix labels){
		
		for(int f = 0; f < features.rows(); f++){
			
			tf.takeRow(features.removeRow(randy.nextInt(features.rows())));
		}
		
		for(int l = 0; l < labels.rows(); l++){
			tl.takeRow(labels.removeRow(randy.nextInt(labels.rows())));
		}
		
		
	}
		//create new data for each forest
		/*
		for(int i = 0; i < amount; i++){
			
			Matrix forestF = new Matrix();
			Matrix forestL = new Matrix();
			
			forestF.copyMetaData(features);
			forestL.copyMetaData(labels);
			
			for(int f = 0; f < features.rows(); f++){
			
				forestF.takeRow(features.row(randy.nextInt(features.rows())));
				
			}
			
			for(int l = 0; l < labels.rows(); l++){
				forestL.takeRow(labels.row(randy.nextInt(labels.rows())));
			}
			
			matrixesF.add(forestF);
			matrixesL.add(forestL);
			
		}
		
		
	}
	*/
	
	
}


class DecisionTree extends SupervisedLearner{
	Node root;
	Random rand;
	int columns;
	
	DecisionTree(){
		columns = 0;
		rand = new Random(1500);
	}
	
	
	
	Node buildTree(Matrix features, Matrix labels){
		
		if(features.rows() != labels.rows())
			throw new RuntimeException("Mismatching features and labels!");
		if(features.rows() < 5 || sameLabels(labels)){
			return new LeafNode(labels);
		}
		Matrix featA = new Matrix();
		Matrix featB = new Matrix();
		Matrix labA = new Matrix();
		Matrix labB = new Matrix();
		
		
		featA.copyMetaData(features);
		featB.copyMetaData(features);
		labA.copyMetaData(labels);
		labB.copyMetaData(labels);
		
		int dividingColumn = rand.nextInt(features.cols());
		int which = features.valueCount(dividingColumn);
		double[] row =  features.row(rand.nextInt(features.rows()));
		double val = row[dividingColumn];
		
		for(int iterations = 8; iterations > 0; iterations--){
			
			
			for(int i = 0; i < features.rows(); i++){
			
				if(which == 0){
					
					if(features.row(i)[dividingColumn] < val){
						featA.takeRow(features.removeRow(i));
						labA.takeRow(labels.removeRow(i));
						
					}
					else{
						featB.takeRow(features.removeRow(i));
						labB.takeRow(labels.removeRow(i));
					}
					i--;
					
				} else{
				
					if(features.row(i)[dividingColumn] == val){
						featA.takeRow(features.removeRow(i));
						labA.takeRow(labels.removeRow(i));
					}
					else{
						featB.takeRow(features.removeRow(i));
						labB.takeRow(labels.removeRow(i));
					}
					i--;
				}
			}
			if(featA.rows() != 0 || featB.rows() !=0)
				break;
		}
		
		Node a = buildTree(featA, labA);
		Node b = buildTree(featB, labB);
		
		return new InteriorNode(dividingColumn, val, which, a, b);
		
	}
	
	boolean sameLabels(Matrix labels){
		
		for(int i = 0; i < labels.rows(); i++){
			if(!labels.row(0).equals(labels.row(i)))
				return false;
		}
		
		return true;
	}
	
	String name() {

		return "DecisionTree";
	}

	void train(Matrix features, Matrix labels, String fn) {

		root = buildTree(features, labels);
		
	}

	void predict(double[] in, double[] out) {
		
		Node n = root;
		InteriorNode node;
		LeafNode o;
		
		while(true){
			if(!n.isLeaf()){
				node =  (InteriorNode) n;
				if(node.feature == 0){
					
					if(in[node.attribute] < node.pivot)
						n =  node.a;
					else
						n =  node.b;
				}
				else{
					
					if(in[node.attribute] == node.pivot)
						n = node.a;
					else
						n = node.b;
				}
				
			}
				
			else{
				o = (LeafNode) n;
				n = (LeafNode) n;
				Vec.copy(out, o.label);
				break;
			}
			

		}
	
	}
}

