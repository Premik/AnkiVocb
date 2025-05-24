package vocb.data

import groovy.yaml.YamlBuilder
import groovy.yaml.YamlSlurper

public class ConceptYamlStorage {

	YamlSlurper slurper = new YamlSlurper()
	YamlBuilder builder = new YamlBuilder()

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
	
	public String toYaml(ConceptDb db) {
		builder(db)		
	}
}
