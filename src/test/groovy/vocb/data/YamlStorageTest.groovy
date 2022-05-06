package vocb.data

import org.junit.jupiter.api.Test

import groovy.yaml.YamlSlurper
import vocb.TestUtils

class YamlStorageTest {

	ConceptYamlStorage st = new ConceptYamlStorage()
	YamlSlurper slurper = new YamlSlurper()

	URL testConceptsUrl = getClass().getResource('/vocb/data/testConcepts.yaml')

	def simpleTermGen = { int i=0->
		slurper.parseText("""\
		term: term$i
		lang: lang$i
		tts: tts$i
		""".stripIndent())
	}

	def simpleTerm1 = simpleTermGen(1)
	def simpleTerm2 = simpleTermGen(2)




	void assertSimpleTerm(Term t, int i) {
		assert t?.term == "term$i"
		assert t?.lang == "lang$i"
		assert t?.tts == "tts$i"
	}


	@Test
	void parseTerm() {
		assertSimpleTerm(st.parseTerm(simpleTerm1, 0), 1)
		assertSimpleTerm(st.parseTerm(simpleTerm2, 1), 2)
	}

	@Test
	void parseConcept() {
		String conceptStr= """\
		  terms: 
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
			assert terms
			assert terms.size() == 2
			assertSimpleTerm(terms[0], 1)
			assertSimpleTerm(terms[1], 2)
		}
	}

	@Test
	void stringListToYaml() {
		String y="""
		- aa
		- bb
		- cc
		- dd
		- ee
		- ff""".stripIndent()

		assert  st.listToYaml([
			"aa",
			"bb",
			"cc",
			"dd",
			"ee",
			"ff"
		]) == y
	}

	@Test
	void singleStringListToYaml() {
		assert  st.listToYaml(["aa"]) == '["aa"]'
	}



	@Test
	void objListToYaml() {
		String y="""
		- aa: 1
		  bb: 2
		- aa: 3
		  bb: 4""".stripIndent()

		def a = """\
		  aa: 1
		  bb: 2""".stripIndent()

		def b = """\
		  aa: 3
		  bb: 4""".stripIndent()

		//println st.listToYaml([a,b])
		assert  st.listToYaml([a, b]) == y
	}

	@Test
	void parseDb() {
		testConceptsUrl.withReader {
			ConceptDb db = st.parseDb(it)
			assert db.version == "0.0.1"
		}
	}

	@Test
	void termToYaml() {
		String y="""\
		term: term
		lang: lang
		tts: tts""".stripIndent()
		String y2 = st.termToYaml(new Term("term", "lang", "tts"), 0)
		TestUtils.compareString(y2,y)
	}

	@Test
	void conceptToYaml() {
		String y="""\
        ##  apple   ################################################################# no img|no freq|t0:tts:missing|t1:tts:missing
		terms: 
		- term: apple
		- term: jablko
		profileName: testProfile""".stripIndent()
		Term t1 = new Term("apple", "en")
		Term t2 = new Term("jablko", "cs")
		Concept c = new Concept(img:"", freq:null, profileName: "testProfile")
		c.terms.addAll([t1, t2])
		//println st.conceptToYaml(c)

		TestUtils.compareString(st.conceptToYaml(c),y)
	}

	@Test
	void conceptToYamlRelaxValidation() {
		String y="""\
		##  apple   ################################################################# no freq
		terms: 
		- term: apple
		- term: jablko
		profileName: relax""".stripIndent()
		Term t1 = new Term("apple", "en")
		Term t2 = new Term("jablko", "cs")
		Concept c = new Concept(img:"", freq:null, profileName:"relax" )
		c.terms.addAll([t1, t2])
		//println st.conceptToYaml(c)

		TestUtils.compareString(st.conceptToYaml(c),y)
	}

	@Test
	void fullParse() {
		ConceptDb db
		testConceptsUrl.withReader {
			db = st.parseDb(it)
		}
		//new File("/tmp/work/db.yaml") << st.dbToYaml(db)
		//println st.dbToYaml(db)
		TestUtils.compareString(st.dbToYaml(db), testConceptsUrl.text)

		//Concept c = db.firstTermIndex["apple"]
		//assert c
		//assert c.terms[0] == "apple"
	}

	@Test
	void fullParseWithExamples() {
		URL ys = getClass().getResource('/vocb/data/withExample.yaml')
		assert ys
		ConceptDb db
		ys.withReader {
			db = st.parseDb(it)
		}
		//new File("/tmp/work/db.yaml") << st.dbToYaml(db)
		//println st.dbToYaml(db)
		TestUtils.compareString(st.dbToYaml(db), ys.text)

		//Concept c = db.firstTermIndex["apple"]
		//assert c
		//assert c.terms[0] == "apple"
	}
}
