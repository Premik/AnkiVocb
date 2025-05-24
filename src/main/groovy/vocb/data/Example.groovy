package vocb.data


import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(callSuper=true)
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Example extends TermContainer {

	public static Example empty = new Example()

	boolean asBoolean(){
		if (this==null) return false
		if (empty === this) return false
		return true
	}

	@Override
	public List<String> validate(ValidationProfile vp) {
		//List<String> ret = super.validate(vp)
		List<String> ret = []
		terms.eachWithIndex {Term t, Integer i->
			ret.addAll(t.validate(vp).collect{"t${i}:${it}"} )
		}
		return ret
	}
}