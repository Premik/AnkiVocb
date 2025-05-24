package vocb.data

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper

@EqualsAndHashCode(callSuper=true)
//@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
@ToString(includePackage=false, ignoreNulls=true, includes=['firstTerm', 'state', 'freq'])
public class Concept extends TermContainer  {


	String state
	String profileName
	String img
	BigDecimal freq



	/*public Term getEnTerm() {
	 termsByLang("en").withDefault { .tap {terms[t] = this} }[0]
	 }*/

	public ValidationProfile getValidationProfile() {
		if (!profileName) return ValidationProfile.currentDefaultProfile
		assert ValidationProfile.PredefinedProfiles[profileName]
		return ValidationProfile.PredefinedProfiles[profileName]
	}

	public boolean isIgnore() {
		validationProfile.name == "ignore"
	}

	public boolean isNoImg() {
		!validationProfile.img
	}

	@Override
	public List<String> validate(ValidationProfile vp ) {
		if (!validationProfile.isDefaultProfile()) {
			//Local profile overrides the provided one, unless the local one is the default
			vp = validationProfile
		}
		List<String> ret = super.validate(vp)
		if (ignore) return ret
		if (!terms) {
			ret.add("No terms")
		}
		else {
			if (terms.size() < 2) ret.add("single term only")
			if (terms.size() > 3) ret.add("${terms.size()} terms. Up to 3 supported only")
		}
		if (vp.img && !img) ret.add("no img")
		if (vp.freq && freq == null) ret.add("no freq")
		if (!termsByLang("en")) ret.add("no en term")
		if (!termsByLang("cs")) ret.add("no cs term")
		if (vp.validateTerms) {
			terms.eachWithIndex {Term t, Integer i->
				ret.addAll(t.validate(vp).collect{"t${i}:${it}"} )
				if (vp.pron && !t.pron && t.lang=="en") ret.add("t${i}:pron:missing")
			}
		}
		return ret
	}
}