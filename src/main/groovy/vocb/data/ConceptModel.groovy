package vocb.data

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper


@Canonical
@ToString(excludes='concepts', includePackage=false)
public class ConceptDb {
	String version = "0.0.1"
	List<Concept> concepts = []
	LinkedHashSet<Example> examples = []

	public List<Term> examplesByLang(String lng) {
		examples.collectMany {it.byLang(lng) }
	}
	
	public List<Term> conceptsByLang(String lng) {
		concepts.findAll {it.state != "ignore"}.collectMany {it.termsByLang(lng)}
	}


	public List<String> validate() {
		List<String> ret = []
		(concepts+examples).each { o->
			List<String> innerVal = o.validate()
			if (innerVal) {
				ret.addAll("$o.firstTerm: ${innerVal.join('|')}")
			}
		}
		return ret
	}
}

@Canonical
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Example {
	List<Term> terms = []
	public String getFirstTerm() {
		terms[0]?.term
	}
	
	
	public Term getAt(int i) {
		return terms[i]
	}

	public List<Term> byLang(String lng) {
		terms.findAll {it.lang == lng }
	}

	public List<String> validate() {
		List<String> ret = []
		terms.eachWithIndex {Term t, Integer i->
			ret.addAll(t.validate().collect{"t${i}:${it}"} )
		}
		return ret
	}
}

@EqualsAndHashCode
//@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
@ToString(includePackage=false, ignoreNulls=true, includes=['firstTerm', 'state', 'freq'])
public class Concept {
	List<Term> terms = []
	//State state

	String state
	String img
	BigDecimal freq

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




}

@Canonical
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Term {
	String term
	String lang
	String tts
	String pron

	public List<String> validate() {
		properties
				.subMap (["term", "lang", "tts"])
				.findAll{k, v->!v}
				.collect {String k, v -> "$k:missing" }
	}

	public static Term csTerm(String t) {new Term(t, "cs")}
	public static Term enTerm(String t) {new Term(t, "en")}


}
