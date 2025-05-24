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
		assert t1.completeness > 0.5
		assert t1.completeness < 0.9
		assert t2.completeness > 0.9
		assert new Term().completeness < 0.01
	}

	@Test
	void conceptCompletness() {
		assert new Concept().completeness < 0.1
		assert c.completeness > 0.1
		//(0..10).each {println  "$it: ${Helper.progressBar(it/10)}" }
	}

	@Test
	void compltNear() {
		def j = new YamlSlurper().parseText '''\
		  terms: 
		  - term: be
		    lang: en
		  - term: být
		    lang: cs
		  examples: 
		  - term: There must be a piano here.
		    lang: en
		  - term: Musí tu někdo být piáno.
		    lang: cs
		  state: ignoreImage
		  freq: 9104176.00000
		  origins: ["corpus"]'''.stripIndent()
		assert j
		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			assert c.completeness > 0.7
			assert c.completeness < 0.9
		}
	}

	@Test
	void compltNoThere() {
		def j = new YamlSlurper().parseText '''\
		  terms: 
		  - term: or
		    lang: en
		  - term: nebo
		    lang: cs
		    tts: nebo.mp3
		  examples: 
		  - term: A cup of tea or coffee.
		    lang: en
		  - term: Šálek čaje nebo kávy.
		    lang: cs
		    tts: Salek caje nebo kavy.mp3
		  img: or.jpeg
		  freq: 2803803.50000
		  origins: ["corpus"]'''.stripIndent()
		assert j
		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			assert c.completeness < 0.99
			assert c.completeness > 0.40
		}
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
			examples:
			- term: But it is not their fault.
			  lang: en
			  tts: But it is not their fault.mp3
			- term: Ale není to jejich vina.
			  lang: cs
			  tts: Ale neni to jejich vina.mp3
			state: ignoreImage
			freq: 1951647.00000
			origins: ["corpus"]'''.stripIndent())

		assert j

		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			println c
			assert c.completeness > 0.5
			assert c.completeness < 0.99
		}
	}

	@Test
	void notCompl() {
		def j = new YamlSlurper().parseText( '''\
			terms:
			- term: than
			  lang: en
			  tts: than.mp3
			- term: než
			  lang: cs
			  tts: nez.mp3
			examples:
			- term: This is better than steak.
			  lang: en
			- term: Je to lepší než steak.
			  lang: cs
			img: than.jpeg
			freq: 782249.00000
			origins: ["corpus"]'''.stripIndent())

		assert j

		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			println c
			assert c.completeness > 0.5
			assert c.completeness < 0.99
		}
	}

	@Test
	void notComplFull() {
		def j = new YamlSlurper().parseText( '''\
			terms:
			- term: kitty
			  lang: en
			  tts: kitty.mp3
			- term: koťátko
			  lang: cs
			  tts: kotatko.mp3
			- term: kočička
			  lang: cs
			  tts: kocicka.mp3
			examples:
			- term: Kitty needs some attention.
			  lang: en
			  tts: Kitty needs some attention.mp3
			- term: Koťátko potřebuje trochu pozornosti.
			  lang: cs
			  tts: Kotatko potrebuje trochu pozornosti.mp3
			img: kitty.jpeg
			freq: 278.47900'''.stripIndent())

		assert j

		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			println c
			assert c.completeness > 0.98
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
	  examples: 
	  - term: I already regret it.
	    lang: en
	    tts: I already regret it.mp3
	  - term: Už teď toho lituji.
	  state: ignoreImage
	  freq: 189941.00000
	  origins: ["corpus"]'''.stripIndent())

		assert j

		new ConceptYamlStorage().tap {
			Concept c = parseConcept(j)
			println c
			assert c.completeness > 0.94
		}
	}
}
