package vocb.data

import org.junit.jupiter.api.Test

import groovy.yaml.YamlSlurper

class ModelTest {

	Term t1 = new Term("en1", "en")
	Term t2 = new Term("cs1", "cs", "tts")
	Term t3 = new Term("cs2", "cs")
	Concept c = new Concept(terms: [en1:t1, cs1:t2, cs2:t3])
	
	@Test constrHelper() {
		assert t1 == t1
		assert t1 == Term.csTerm("en1")
		new Concept().tap {
			addEnCsTerms("enWord", "csWord")
			assert terms.size() == 2
			assert firstTerm == "enWord"
		}
		
	}

	@Test
	void termCompletness() {
		assert t1.validate().size() == 1
		assert t1.validate()[0] == "tts:missing"
						
	}

	@Test
	void conceptCompletness() {
		assert new Concept().validate().contains('no img')
		assert c.validate().contains('no img')
		//(0..10).each {println  "$it: ${Helper.progressBar(it/10)}" }
	}

	
	

	@Test
	void complt() {
		def j = new YamlSlurper().parseText( '''\
			terms:
			- term: their
			  lang: en
			  tts: their.mp3
			- term: jejich
			  lang: cs
			  tts: jejich.mp3
			- term: své			
			state: ignoreImage
			freq: 1951647.00000
			origins: ["corpus"]'''.stripIndent())			
		assert j

		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			assert c.validate() == ["t2:lang:missing", "t2:tts:missing"]
			
		}
	}

	


	@Test
	void notComplFull2() {
		def j = new YamlSlurper().parseText( '''\
	  terms: 
	  - term: already
	    lang: en
	    tts: already.mp3
	  - term: již
	    lang: cs
	    tts: jiz.mp3
	  - term: už
	    lang: cs
	    tts: uz.mp3	 
	  state: ignoreImage
	  freq: 189941.00000
	  origins: ["corpus"]'''.stripIndent())

		assert j

		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			println c
			assert c.validate() == []
		}
	}
}
