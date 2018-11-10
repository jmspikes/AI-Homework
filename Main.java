import java.util.ArrayList;
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
		String fn = "C:\\Users\\Jon\\Desktop\\AI Homework\\Homework6\\data\\" + challenge;
		Matrix trainFeatures = new Matrix();
		trainFeatures.loadARFF(fn + "_train_feat.arff");
		Matrix trainLabels = new Matrix();
		trainLabels.loadARFF(fn + "_train_lab.arff");

		// Train the model
		learner.train(trainFeatures, trainLabels);

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
		testLearner(new RandomForest(50));
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
	ArrayList<Node> holder;
	double[] mode;
	RandomForest(int amount){
		this.amount = amount;
		randy = new Random(1500);
		holder = new ArrayList<Node>();
		
	}
	
	String name() {
		return "Random Forest";
	}

	void train(Matrix features, Matrix labels) {

		holder = new ArrayList<Node>();
		DecisionTree d;

		tf = new Matrix();
		tl = new Matrix();
		tf.copyMetaData(features);
		tl.copyMetaData(labels);
		Matrix fc = new Matrix();
		fc.copy(features);
		Matrix lc = new Matrix();
		lc.copy(labels);
		for(int i = 0; i < amount; i++){
		d = new DecisionTree();
		generateTrainingData(fc, lc);
		d.train(tf, tl);
		holder.add(d.root);
		}	
	}

	void predict(double[] in, double[] out) {
			InteriorNode node;
			LeafNode o;
			ArrayList<double[]> results = new ArrayList<double[]>();
			double[] returnArray = null;
			
			for(int i = 0; i < holder.size(); i++){
				Node n = holder.get(i);
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
						results.add(o.label);
						break;
					}
				}	
			}
			returnArray = new double[results.get(0).length];
			
			Matrix parseL = new Matrix();
			parseL.copyMetaData(tl);
			
			for(int i = 0; i < results.size(); i++)
				parseL.takeRow(results.get(i));
			
			for(int i = 0; i < parseL.cols(); i++)
			{
				if(parseL.valueCount(i) == 0)
					returnArray[i] = parseL.columnMean(i);
				else
					returnArray[i] = parseL.mostCommonValue(i);
			}
			Vec.copy(out, returnArray);		
	}
	
	void generateTrainingData(Matrix features, Matrix labels){		
		for(int f = 0; f < features.rows(); f++){
			int offset = randy.nextInt(features.rows()-1);
			tf.takeRow(features.row(offset));
			tl.takeRow(labels.row(offset));
			
		}		
	}
}


class DecisionTree extends SupervisedLearner{
	Node root;
	Random rand;
	int columns;
	
	DecisionTree(){
		columns = 0;
		rand = new Random(800);
	}
	
	
	
	Node buildTree(Matrix features, Matrix labels){
		
		if(features.rows() != labels.rows())
			throw new RuntimeException("Mismatching features and labels!");
		if(features.rows() < 10 || labels.rows()< 10 || sameLabels(labels)){
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
		
		for(int patients = 8; patients > 0; patients--){
			
			
			for(int i = 0; i < features.rows(); i++){
			
				if(which == 0){
					
					if(features.row(i)[dividingColumn] < val){
						featA.takeRow(features.removeRow(i));
						labA.takeRow(labels.removeRow(i));
						
					} else{
						featB.takeRow(features.removeRow(i));
						labB.takeRow(labels.removeRow(i));
					}
					i--;
					
				} else{
				
					if(features.row(i)[dividingColumn] == val){
						featA.takeRow(features.removeRow(i));
						labA.takeRow(labels.removeRow(i));
					} else{
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

	void train(Matrix features, Matrix labels) {

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
				} else{
					
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
