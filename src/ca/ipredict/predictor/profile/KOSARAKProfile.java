package ca.ipredict.predictor.profile;

public class KOSARAKProfile extends Profile {

	@Override
	public void Apply() {

		//Global parameters
		//Pre-processing
		parameters.put("sequenceMinSize", "8");
		parameters.put("sequenceMaxSize", "999");
		parameters.put("removeDuplicatesMethod", "1");
		parameters.put("consequentSize", "3"); 
		parameters.put("windowSize", "5"); 

		///////////////
		//CPT parameters
		//Training
		parameters.put("splitMethod", "1");
		parameters.put("splitLength", "0.9f");
		parameters.put("minSup", "0.0005"); //SEI compression, minSup to remove low supporting items
		
		//CCF compression
		parameters.put("CCFmin", "2");
		parameters.put("CCFmax", "4");
		parameters.put("CCFsup", "2");
		
		//Prediction
		parameters.put("recursiveDividerMin", "3"); //should be >= 0 and < recursiveDividerMax 
		parameters.put("recursiveDividerMax", "99"); //should be > recusiveDividerMax and < windowSize
		parameters.put("minPredictionRatio", "10f");
		parameters.put("noiseRatio", "0.8f");

	}

}
