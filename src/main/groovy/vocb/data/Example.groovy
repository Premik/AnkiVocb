package vocb.data

import org.apache.groovy.parser.antlr4.GroovyParser.SuperPrmrAltContext

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode(callSuper=true)
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
public class Example extends TermContainer {
	
	public static Example empty = new Example()
	
	@Override
	public List<String> validate() {
		List<String> ret = super.validate()
		terms.eachWithIndex {Term t, Integer i->
			ret.addAll(t.validate().collect{"t${i}:${it}"} )
		}
		return ret
	}
}