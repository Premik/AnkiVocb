package vocb.data

import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

public enum State {
	populate, done, ignore
}
@Canonical
@ToString(excludes='concepts')
public class ConceptDb {
	String version = "0.0.1"
	List<Concept> concepts = []
}

@EqualsAndHashCode
@ToString
public class Concept {
	LinkedHashMap<String, Term> terms = [:]
	//State state
	String state
	String img
	BigDecimal freq
	Set<String> origins = [] as HashSet

	public String getFirstTerm() {terms.values()[0]?.term}
	public List<Term> termsByLang(String lng) { terms?.values()?.findAll {it.lang == lng }}
}

@Canonical
public class Term {
	String term
	String lang
	String tts
}
