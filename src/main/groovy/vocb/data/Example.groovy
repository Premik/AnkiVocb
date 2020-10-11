package vocb.data

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Example  {
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