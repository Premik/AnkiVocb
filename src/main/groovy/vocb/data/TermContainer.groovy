package vocb.data

import groovy.transform.AutoClone
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import vocb.Helper

@EqualsAndHashCode(excludes=["location"])
@AutoClone
@CompileStatic
public abstract class TermContainer  {
	final List<Term> terms = []
	protected boolean dirty=true
	DataLocation location

	
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

	public Term getAt(int i) {
		return terms[i]
	}
	
	void setProperty(String name, Object value) {
		Helper.setAndCheckDirty(this, name, value)
		if (name == "dirty") {
			terms*.dirty = value as boolean
		}	
	}
	
	public boolean isDirty() {
		if (dirty) return true
		if (terms.find { it.dirty }) {
			dirty = true
		}
		return dirty
	}
	
	public void setDirty(boolean v) {
		dirty = v
	}
	
	public void updateDataLocationDirty() {
		assert location
		if (location.dirty) return
		if (isDirty()) location.dirty = true
	}
	
	public List<String> validate(ValidationProfile vp) {
		List<String> ret = []
		//if (!location) ret.add("No data location set")
		return ret
	}
	
	void setLocation(DataLocation l) {
		if (location) location.dirty = true
		if (l) l.dirty = true
		location = l
		
	}

}