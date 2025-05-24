package vocb.data

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
	
	 ValidationProfile vp = ValidationProfile.defaultProfile



	public ConceptDb parseDb(Reader r) {
		parseDbMap(slurper.parse(r))
	}

	public ConceptDb parseDbMap(Map doc) {
		ConceptDb db = new ConceptDb(version: doc.version)

		db.concepts= doc.concepts.collect(this.&parseConcept)
		db.examples = doc.examples.collect(this.&parseExample)
		return db
	}

	public Concept parseConcept(Map cjs) {
		assert cjs
		Concept c = new Concept(state:cjs.state, img:cjs.img, freq:cjs.freq)
		cjs.terms.each {
			Term t = parseTerm(it)
			c.terms.add(t)
		}

		/*cjs.examples.each {			
			Term t = parseTerm(it)
			c.examples.put(t.term, t)
		}*/
		assert !cjs.examples : "Depricated example found on $c"
		return c
	}

	public Example parseExample(Map exList) {
		assert exList
		new Example().tap{ Example e->
			e.terms.addAll( exList.terms.collect(this.&parseTerm))
		}
	}


	public Term parseTerm(Map term) {
		new Term(term)
	}

	public String yamlHash(String key, String value) {
		if (!key || !value) return ""
		boolean reserved = value in ['true', 'false', "Yes", "No", "yes", "no", "off", "on"]
		boolean specialChar = value[0] in  ('''!@#$%^&*("')''' as List)
		boolean middleChar = false
		if (value.readLines().size() ==1) {
			middleChar = value.contains(':')
		}

		if (reserved || specialChar ||middleChar) { return """$key: "$value"\n"""}
		return "$key: $value\n"
	}


	public StringBuilder appendYamlHash(String key, Object value, StringBuilder sb=new StringBuilder()) {
		sb.append(yamlHash(key, value?.toString()))
	}

	public String dbToYaml(ConceptDb db, Closure<Boolean> termFilter= {true}) {
		assert db
		String concepts=listToYaml(db.concepts.findAll(termFilter).collect(this.&conceptToYaml))
		String samples=listToYaml(db.examples.findAll(termFilter).collect(this.&exampleToYaml))
		"version: $db.version\n${yamlHash('concepts', concepts)}\n${yamlHash('examples', samples)}"
	}
	
	

	void appendBanner(String label, String warnings, StringBuilder sb, int width=70) {		
		if (!warnings) return
		if (!label) return
			sb.append("##  ")
		sb.append(label)
		sb.append('   ' + '#'*(70-label.length()))
		if (warnings) sb.append(" "  +warnings)
		
		sb.append("\n")
	}

	public String exampleToYaml(Example e) {
		assert e
		StringBuilder sb = new StringBuilder()

		String examples=listToYaml(e.terms.collect(this.&termToYaml))
		String ft = e.firstTerm
		assert ft : "Term list is blank for a sample $e"

		String v = e.validate(vp).join("|")
		if (v) appendBanner(ft, v, sb)
		appendYamlHash("terms", examples, sb)
		return sb.toString()
	}

	public String conceptToYaml(Concept c) {
		assert c


		StringBuilder sb = new StringBuilder()
		//sb.append("terms: ").append(Helper.indentNextLines(terms,2))
		//sb.append("#"*16 + "\n")


		String terms=listToYaml(c.terms.collect(this.&termToYaml))
		
		//String examples=listToYaml(c.examples.values().collect(this.&termToYaml))
		String ft = c.firstTerm
		assert ft : "Term list is blank for a conpcept $c"
		appendBanner(ft, c.validate(vp).join("|"), sb)
		appendYamlHash("terms", terms, sb)
		//appendYamlHash("examples", examples, sb)
		/*sb.append("terms: ")
		 if (terms.length() > 0) {
		 sb.append("\n")
		 }
		 sb.append(terms)*/

		appendYamlHash("state", c.state, sb)
		appendYamlHash("img", c.img,sb)
		appendYamlHash("freq", Helper.roundDecimal(c.freq, 5), sb)
		//sb.append("origins: ").append(origins)
	
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
		if (st.size() <5) { //Short list
			if ( !st.any {it?.contains('\n') || it?.length()> 80}) {
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
		appendYamlHash("pron", t.pron, sb)
		return sb.toString()
	}


}
