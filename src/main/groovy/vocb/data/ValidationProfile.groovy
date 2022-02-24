package vocb.data;

public class ValidationProfile {
	
	public static ValidationProfile defaultProfile = new ValidationProfile() 
	
	boolean img = true
	boolean validateTerms = true
	List<String> termRequiredFields = ["term", "lang", "tts"]
}