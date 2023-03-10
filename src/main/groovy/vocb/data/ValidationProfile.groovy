package vocb.data;

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString

@ToString(includePackage=false, ignoreNulls=true, includes=['name'])
@AutoClone
@CompileStatic
public class ValidationProfile {

	public static final ValidationProfile strict = new ValidationProfile (name:"strict")
	
	@Lazy public static final ValidationProfile noImg = currentDefaultProfile.clone().tap {
		name="strictNoImg"
		img=false
	}
	public static final ValidationProfile relax = new ValidationProfile (name:"relax", img:false, freq:true, pron:false, termRequiredFields : ["term", "lang"] )
	public static final ValidationProfile ignore = relax.clone().tap {
		name ="ignore"
		requiredNativeTerm = false
		validateTerms = false		
	}
	
	public static ValidationProfile currentDefaultProfile =  strict
	public static ValidationProfile testDefaultProfile =  currentDefaultProfile.clone().tap {name="testProfile" }

	public static final Map<String, ValidationProfile> PredefinedProfiles = [strict, noImg, relax, ignore, testDefaultProfile].collectEntries {
		[(it.name): it]
	}

	String name
	boolean img = true
	boolean freq = true
	boolean pron = false
	boolean requiredNativeTerm = true //Concept with only foreign term is effectively ignored 
	boolean validateTerms = true
	List<String> termRequiredFields = ["term", "lang", "tts"]
	List<String> termsDefaultLanguages = ["en", "cs"].withDefault { "cs" } //Firs term en, rest cs
	
	public boolean isDefaultProfile() {
		return this == currentDefaultProfile
	}
	
	
		
}