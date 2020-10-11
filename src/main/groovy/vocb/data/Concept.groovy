package vocb.data

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper

@EqualsAndHashCode
//@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
@ToString(includePackage=false, ignoreNulls=true, includes=['firstTerm', 'state', 'freq'])
public class Concept extends TermContainer  {
	

	String state
	String img
	BigDecimal freq
	

	
	/*public Term getEnTerm() {
	 termsByLang("en").withDefault { .tap {terms[t] = this} }[0]
	 }*/

	public List<String> validate(boolean appendTermWarnings=true ) {
		List<String> ret = []
		if (state == "ignore") return ret
		if (!terms) {
			ret.add("No terms")
		}
		else {
			if (terms.size() < 2) ret.add("single term only")
			if (terms.size() > 3) ret.add("${terms.size()} terms. Up to 3 supported only")
		}
		if (state != "ignoreImage" && !img) ret.add("no img")
		if (freq == null) ret.add("no freq")
		if (!termsByLang("en")) ret.add("no en term")
		if (!termsByLang("cs")) ret.add("no cs term")
		if (appendTermWarnings) {
			terms.eachWithIndex {Term t, Integer i->
				ret.addAll(t.validate().collect{"t${i}:${it}"} )
				if (!t.pron && t.lang=="en") ret.add("t${i}:pron:missing")
			}
		}
		return ret
	}

	

}