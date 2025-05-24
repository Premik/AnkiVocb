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
		String origins = listToYaml(c.origins)
		String terms=listToYaml(c.terms.collect(this.&termToYaml))
		
		StringBuilder sb = new StringBuilder()
		//sb.append("terms: ").append(Helper.indentNextLines(terms,2))
		sb.append("terms: ").append(terms)
		
		appendYamlHash("state", c.state, sb)
		appendYamlHash("img", c.img,sb)
		appendYamlHash("freq", c.freq, sb)
		//sb.append("origins: ").append(Helper.indentNextLines(origins,2))
		sb.append("origins: ").append(origins)
		return sb.toString()

		/*"""\
		terms: ${Helper.indentNextLines(terms,2)}
		state: $c.state
		img: $c.img
		freq: $c.freq
		origins: ${Helper.indentNextLines(origins,2)}""".stripIndent()*/
	}

	public CharSequence listToYaml(List<CharSequence> st) {
		if (!st) return "[]"
		"\n" + st.collect(this.&toYamlListItem).join("\n")
	}

	public CharSequence toYamlListItem(CharSequence yaml) {
		StringBuilder sb = new StringBuilder(Helper.indent(yaml, 2))
		sb.setCharAt(0, '-' as char)
		return sb

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
