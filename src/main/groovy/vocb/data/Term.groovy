package vocb.data

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper


@Canonical
@ToString(includePackage=false, ignoreNulls=true)
@AutoClone
@EqualsAndHashCode
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
