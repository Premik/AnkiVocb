package vocb.data

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.ToString

@Canonical
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Example extends TermContainer {

	@Override
	public List<String> validate() {
		List<String> ret = []
		terms.eachWithIndex {Term t, Integer i->
			ret.addAll(t.validate().collect{"t${i}:${it}"} )
		}
		return ret
	}
}