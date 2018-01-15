package ca.ipredict.predictor.profile;

public class SNAKEProfile extends Profile {

	@Override
	public void Apply() {
		
		//Global parameters
		//Pre-processing
		parameters.put("sequenceMinSize", "7");
		parameters.put("sequenceMaxSize", "999");
		parameters.put("removeDuplicatesMethod", "1");
		parameters.put("consequentSize", "2"); 
		parameters.put("windowSize", "5"); 

		///////////////
		//CPT parameters
		//Training
		parameters.put("splitMethod", "1");
		parameters.put("splitLength", "0.9f");
		parameters.put("minSup", "0.05"); //SEI compression, minSup to remove low supporting items
		
		//Prediction
		parameters.put("recursiveDividerMin", "1"); //should be >= 0 and < recursiveDividerMax 
		parameters.put("recursiveDividerMax", "5"); //should be > recusiveDividerMax and < windowSize
		parameters.put("minPredictionRatio", "10f");
		parameters.put("noiseRatio", "0.8f");

	}

}
