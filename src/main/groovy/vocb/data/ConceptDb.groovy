package vocb.data

import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(excludes='concepts', includePackage=false)
public class ConceptDb {
	String version = "0.0.1"
	List<Concept> concepts = []
	LinkedHashSet<Example> examples = []

	public List<Term> examplesByLang(String lng) {
		examples.collectMany {it.termsByLang(lng) }
	}

	public List<Term> conceptsByLang(String lng) {
		concepts.findAll {it.state != "ignore"}.collectMany {it.termsByLang(lng)}
	}


	public List<String> validate() {
		List<String> ret = []
		(concepts+examples).each { o->
			List<String> innerVal = o.validate()
			if (innerVal) {
				ret.add("$o.firstTerm: ${innerVal.join('|')}")
			}
		}
		return ret
	}
}