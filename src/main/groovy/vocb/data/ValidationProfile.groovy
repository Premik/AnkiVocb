package vocb.data;

import groovy.transform.ToString

@ToString(includePackage=false, ignoreNulls=true)
public class ValidationProfile {
	
	public static final ValidationProfile strict = new ValidationProfile (name:"strict")
	public static final ValidationProfile relax = new ValidationProfile (name:"relax", img:false, freq:false, pron:false, termRequiredFields : ["term", "lang"] )
	public static ValidationProfile defaultProfile = relax
	
	String name
	boolean img = true
	boolean freq = true
	boolean pron = true
	boolean validateTerms = true
	List<String> termRequiredFields = ["term", "lang", "tts"]
}