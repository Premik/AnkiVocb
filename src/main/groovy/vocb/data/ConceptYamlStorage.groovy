package vocb.data

import javax.xml.stream.events.Characters

import org.apache.commons.collections.functors.InstanceofPredicate

import groovy.json.JsonGenerator
import groovy.yaml.YamlSlurper
import vocb.Helper

public class ConceptYamlStorage {

	YamlSlurper slurper = new YamlSlurper()
	JsonGenerator generator = new JsonGenerator.Options()
	.excludeNulls()  // Do not include fields with value null.
	//.excludeFieldsByName('password')  // Exclude fields with given name(s).
	.disableUnicodeEscaping()  // Do not escape UNICODE.
	//.addConverter(Optional) { value -> value.orElse('UNKNOWN') } // Custom converter for given type defined as Closure.
	.build()



	public ConceptDb parseDb(Reader r) {
		parseDbMap(slurper.parse(r))
	}

	public ConceptDb parseDbMap(Map doc) {
		ConceptDb db = new ConceptDb(version: doc.version)

		db.concepts= doc.concepts.collect(this.&parseConcept)
		return db
	}

	public Concept parseConcept(Map concept) {
		Concept c = new Concept(concept)
		c.terms =  concept.terms.collect(this.&parseTerm)
		return c
	}

	public Term parseTerm(Map term) {
		new Term(term)
	}

	public String yamlHash(String key, String value) {
		if (!key || !value) return ""
		return "$key: $value\n"
	}


	public StringBuilder appendYamlHash(String key, Object value, StringBuilder sb=new StringBuilder()) {
		sb.append(yamlHash(key, value?.toString()))
	}

	public String dbToYaml(ConceptDb db) {
		assert db
		String concepts=listToYaml(db.concepts.collect(this.&conceptToYaml))

		"version: $db.version\n" +
				"concepts: " +concepts
	}

	public String conceptToYaml(Concept c) {
		assert c



		StringBuilder sb = new StringBuilder()
		//sb.append("terms: ").append(Helper.indentNextLines(terms,2))
		//sb.append("#"*16 + "\n")

		String terms=listToYaml(c.terms.collect(this.&termToYaml))
		String ft = c.firstTerm
		sb.append("##  ").append(ft).append('   ' + '#'*(70-ft.length())).append("\n")
		appendYamlHash("terms", terms, sb)
		/*sb.append("terms: ")
		 if (terms.length() > 0) {
		 sb.append("\n")
		 }
		 sb.append(terms)*/

		appendYamlHash("state", c.state, sb)
		appendYamlHash("img", c.img,sb)
		appendYamlHash("freq", Helper.roundDecimal(c.freq, 5), sb)
		//sb.append("origins: ").append(origins)
		appendYamlHash("origins", listToYaml(c.origins), sb)
		return sb.toString()

		/*"""\
		 terms: ${Helper.indentNextLines(terms,2)}
		 state: $c.state
		 img: $c.img
		 freq: $c.freq
		 origins: ${Helper.indentNextLines(origins,2)}""".stripIndent()*/
	}

	public CharSequence listToYaml(List<CharSequence> st) {
		//if (!st) return "[]"
		if (!st) return null
		if (st.size() <2) { //Short list
			if ( !st.any {it?.contains('\n') || it?.length()> 20}) {
				//Put short simple lists on one line
				String ml = st.collect().collect{/"$it"/}join(', ')
				//println "!! $ml ${ml.contains('\n')} !!"
				return "[$ml]"
			}
		}
		"\n" + st.collect(this.&toYamlListItem).join("\n")
	}

	public CharSequence toYamlListItem(CharSequence yaml) {
		String indt= Helper.indent(yaml, 2)
		boolean found = false
		indt.split("\n")
				.collect {new StringBuilder(it)}
				.each { StringBuilder sb->
					if (found) return
						if (sb.length() && sb[0].allWhitespace) { //Found first non-comment line
							found = true
							sb.setCharAt(0, '-' as char)
						}
				}.join("\n")


	}

	public CharSequence termToYaml(Term t) {
		assert t
		StringBuilder sb = new StringBuilder()
		appendYamlHash("term", t.term, sb)
		appendYamlHash("lang", t.lang,sb)
		appendYamlHash("tts", t.tts, sb)
		return sb.toString()
	}

	
}
