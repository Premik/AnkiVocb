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
	List<Example> examples = []
}

@Canonical
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Example {
	List<Term> terms = []
	public String getFirstTerm() {terms[0]?.term}
	
	public Term getAt(int i) {
		return terms[i]
	}
	
	public List<Term> byLang(String lng) { terms.findAll {it.lang == lng }}

}

@EqualsAndHashCode
//@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
@ToString(includePackage=false, ignoreNulls=true, includes=['firstTerm', 'state', 'freq'])
public class Concept {
	LinkedHashMap<String, Term> terms = [:]
	//State state
	LinkedHashMap<String, Term> examples = [:]
	String state
	String img
	BigDecimal freq
	Set<String> origins = [] as HashSet

	public String getFirstTerm() {terms.values()[0]?.term}
	public List<Term> termsByLang(String lng) { terms?.values()?.findAll {it.lang == lng }}
	public Term getEnTerm() { termsByLang("en")[0]}
	public Term getCsTerm() { termsByLang("cs")[0]}
	public Term getCsAltTerm() { termsByLang("cs")[1]}
	public void addEnCsTerms(String en, String cs, String csAlt=null) {
		terms[en] = Term.enTerm(en)
		terms[cs] = Term.enTerm(cs)
		if (csAlt) { terms[csAlt] = Term.enTerm(csAlt)}
	}
	
	/*public Term getEnTerm() {
		termsByLang("en").withDefault { .tap {terms[t] = this} }[0]
	}*/
	
	public List<Term> examplesByLang(String lng) { examples?.values()?.findAll {it.lang == lng }}
	public BigDecimal getCompleteness( ) {
		if (state == "ignore") return 1
		BigDecimal termsCp = terms.values()*.completeness*.div(terms.size()).sum()?:0
		BigDecimal exCp = examples.values()*.completeness*.div(examples.size()).sum()?:0
		BigDecimal[] grp =	[
			Math.min(terms.size(), 2)/2,
			0.01+ Math.min(examples.size(), 2)/2,
			0.01+termsCp,
			0.01+exCp,
		].collect( Helper.&clamp01 )
		//BigDecimal grpSum = grp.inject(1.0) {prod, v->prod*v }
		BigDecimal grpSum = grp.sum() / grp.size()
		BigDecimal fldSum = [state =="ignoreImage" ? "1" : img, freq].findAll().size()/2.0
		(grpSum*2 + fldSum)/3
	}
	
	public Term getAt(int i) {
		return terms[i]
	}




}

@Canonical
@ToString(includePackage=false, ignoreNulls=true, excludes='completeness')
@AutoClone
public class Term {
	String term
	String lang
	String tts
	String pron

	public double getCompleteness() {
		[term, lang, tts].findAll().size()/3.0
	}

	public static Term csTerm(String t) {new Term(t, "cs")}
	public static Term enTerm(String t) {new Term(t, "en")}


}
