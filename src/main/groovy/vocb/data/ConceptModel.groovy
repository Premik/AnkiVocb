package vocb.data

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper


@Canonical
@ToString(excludes='concepts', includePackage=false)
public class ConceptDb {
	String version = "0.0.1"
	List<Concept> concepts = []
}

@EqualsAndHashCode
@ToString(includePackage=false, ignoreNulls=true, excludes=['completeness', 'terms', 'examples'])
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
	public BigDecimal getCompleteness( ) {
		if (state == "ignore") return 1
		BigDecimal termsCp = terms.values()*.completeness*.div(terms.size()).sum()?:0
		BigDecimal exCp = examples.values()*.completeness*.div(terms.size()).sum()?:0
		BigDecimal[] grp =	[
			Math.min(terms.size(), 2)/2,
			0.5+ Math.min(examples.size(), 2)/2,
			0.1+termsCp,
			0.5+exCp,
		].collect( Helper.&clamp01 )
		BigDecimal grpSum = grp.inject(1.0) {prod, v->prod*v }
		BigDecimal fldSum = [state =="ignoreImage" ? "1" : img, freq].findAll().size()/2.0
		(grpSum + fldSum)/2
	}
}

@Canonical
@ToString(includePackage=false, ignoreNulls=true, excludes='completeness')
public class Term {
	String term
	String lang
	String tts

	public double getCompleteness() {
		[term, lang, tts].findAll().size()/3.0
	}


}
