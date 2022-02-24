package vocb.data

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper

@EqualsAndHashCode(callSuper=true)
//@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
@ToString(includePackage=false, ignoreNulls=true, includes=['firstTerm', 'state', 'freq'])
public class Concept extends TermContainer  {


	String state
	String img
	BigDecimal freq



	/*public Term getEnTerm() {
	 termsByLang("en").withDefault { .tap {terms[t] = this} }[0]
	 }*/

	@Override
	public List<String> validate(ValidationProfile vp ) {
		List<String> ret = super.validate()
		if (state == "ignore") return ret
		if (!terms) {
			ret.add("No terms")
		}
		else {
			if (terms.size() < 2) ret.add("single term only")
			if (terms.size() > 3) ret.add("${terms.size()} terms. Up to 3 supported only")
		}
		if (state != "ignoreImage" && vp.img && !img) ret.add("no img")
		if (freq == null) ret.add("no freq")
		if (!termsByLang("en")) ret.add("no en term")
		if (!termsByLang("cs")) ret.add("no cs term")
		if (vp.validateTerms) {
			terms.eachWithIndex {Term t, Integer i->
				ret.addAll(t.validate(vp).collect{"t${i}:${it}"} )
				if (!t.pron && t.lang=="en") ret.add("t${i}:pron:missing")
			}
		}
		return ret
	}



}