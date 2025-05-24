package vocb.data;

public class ValidationProfile {
	
	public static ValidationProfile strict = new ValidationProfile ()
	public static ValidationProfile relax = new ValidationProfile (img:false, freq:false, pron:false, termRequiredFields : ["term", "lang"] )
	public static ValidationProfile defaultProfile = relax
	
	boolean img = true
	boolean freq = true
	boolean pron = true
	boolean validateTerms = true
	List<String> termRequiredFields = ["term", "lang", "tts"]
}