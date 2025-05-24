package vocb.data

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper

@EqualsAndHashCode
//@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
@ToString(includePackage=false, ignoreNulls=true, includes=['firstTerm', 'state', 'freq'])
public class Concept  {
	List<Term> terms = []
	//State state

	String state
	String img
	BigDecimal freq
	boolean dirty=true

	public String getFirstTerm() {terms[0]?.term}
	public List<Term> termsByLang(String lng) { terms?.findAll {it.lang == lng }}
	public Term getEnTerm() { termsByLang("en")[0]}
	public Term getCsTerm() { termsByLang("cs")[0]}
	public Term getCsAltTerm() { termsByLang("cs")[1]}
	public void addEnCsTerms(String en, String cs, String csAlt=null) {
		terms[0] = Term.enTerm(en)
		terms[1] = Term.enTerm(cs)
		if (csAlt) { terms[2] = Term.enTerm(csAlt)}
	}
	

	/*public Term getEnTerm() {
	 termsByLang("en").withDefault { .tap {terms[t] = this} }[0]
	 }*/

	public Object getExamples() {assert false : "Depricated" }

	public List<Term> examplesByLang(String lng) { assert false : "Depricated" }

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

	public Term getAt(int i) {
		return terms[i]
	}
	
	void setProperty(String name, Object value) {
		Helper.setAndCheckDirty(this, name, value)
		if (name == "dirty") {
			terms*.dirty = value
		}
		
	}
	
	public boolean isDirty() {
		if (dirty) return true
		if (terms.find { it.dirty }) {
			dirty = true
		}
		return dirty
	}
	
	
	
	
	



}