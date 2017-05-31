package ca.ipredict.predictor.profile;

/**
 * Apply a parameter profile by name
 */
public class ProfileManager {	
	public static void loadProfileByName(String name) {
		Profile profile = null;
		try {
			Class<?> classI;
			if (name.contains("SPICE") ) classI = Class.forName("ca.ipredict.predictor.profile."+ "SPICE" + "Profile");
			else classI = Class.forName("ca.ipredict.predictor.profile."+ name + "Profile");
			profile = (Profile) classI.newInstance();
		} catch (Exception e) {
			profile = new DefaultProfile();
		}
		
		profile.Apply();
	}
}
