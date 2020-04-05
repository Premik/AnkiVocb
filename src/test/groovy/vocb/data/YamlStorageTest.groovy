package vocb.data

import org.junit.jupiter.api.Test

import groovy.yaml.YamlSlurper

class YamlStorageTest {

	ConceptYamlStorage st = new ConceptYamlStorage()
	YamlSlurper slurper = new YamlSlurper()

	URL testConceptsUrl = getClass().getResource('/anki/data/testConcepts.yaml')

	def simpleTermGen = {int i=0->
		slurper.parseText("""\
		term: term$i
		lang: lang$i
		tts: tts$i
		""".stripIndent())
	}

	def simpleTerm1 = simpleTermGen(1)
	def simpleTerm2 = simpleTermGen(2)




	void assertSimpleTerm(Term t, int i) {
		assert t.term == "term$i"
		assert t.lang == "lang$i"
		assert t.tts == "tts$i"
	}


	@Test
	void parseTerm() {
		assertSimpleTerm(st.parseTerm(simpleTerm1), 1)
		assertSimpleTerm(st.parseTerm(simpleTerm2), 2)
	}

	@Test
	void parseConcept() {
		String conceptStr= """\
		- terms: 
		  - term: term1
		    lang: lang1
		    tts: tts1
		  - term: term2
		    lang: lang2
		    tts: tts2
		  state: state
		  img: img
		  freq: 1  
		  origins:
		  - org1 
		""".stripIndent()
		def y = slurper.parseText(conceptStr)
		Concept c = st.parseConcept(y)
		
		c.with {
			assert img == "img"
			assert origins
			assert origins[0] == "org1"
			assert terms
			assert terms.size() == 2
			assertSimpleTerm(terms[0], 1)
			assertSimpleTerm(terms[1], 2)
		}
	}

	@Test
	void parseDb() {
		testConceptsUrl.withReader {
			ConceptDb db = st.parseDb(it)
			assert db.version == "0.0.1"
		}
	}

	@Test
	void toYaml() {
		ConceptDb db
		testConceptsUrl.withReader {
			db = st.parseDb(it)
		}
		println st.toYaml(db)
	}
}
