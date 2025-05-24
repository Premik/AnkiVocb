package vocb.data

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import groovy.yaml.YamlSlurper

class ModelTest {
	
	ValidationProfile vp = ValidationProfile.strict

	Term t1 = new Term("en1", "en")
	Term t2 = new Term("cs1", "cs", "tts")
	Term t3 = new Term("cs2", "cs")
	Concept c = new Concept().tap {
		 terms.addAll([t1, t2, t3])
	}
	
	@Test validationProfileBasic() {
		assert ValidationProfile.currentDefaultProfile.isDefaultProfile()
		assert !ValidationProfile.strict.isDefaultProfile() || !ValidationProfile.relax.isDefaultProfile()
		
	}
	
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
		assert t1.validate(vp).size() == 1
		assert t1.validate(vp)[0] == "tts:missing"
						
	}

	@Test
	void conceptCompletness() {
		assert new Concept().validate(vp).contains('no img')
		assert c.validate(vp).contains('no img')
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
			assert c.validate(vp) == ["t0:pron:missing", "t2:lang:missing", "t2:tts:missing"]
			
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
			assert c.validate(vp).collect{it.toString()}.contains("t0:pron:missing")
		}
	}
	
	@Test
	void testTermDirty() {
		assert t2.dirty
		t2.dirty = false
		assert !t2.dirty
		t2.lang="changed"
		assert t2.lang == "changed"
		assert t2.dirty
		
	}
	
	@Test
	void conceptDirtyAtomic() {
		assert c.dirty
		c.dirty = false
		c.img="changed"
		assert c.img == "changed"
		assert c.dirty
	}
	
	@Test
	void conceptDirtyComposite() {		
		assert c.enTerm.dirty
		c.dirty = false
		assert c.enTerm
		assert !c.enTerm.dirty
		c.enTerm.pron="changed"
		assert c.dirty
		c.dirty = false
		assert c.enTerm
		c.enTerm.pron="changed"
		assert !c.dirty		
	}
	
	@Test
	@Disabled
	void setTermsTest() {
		Concept c = new Concept()
		c.img = "img"
		c.terms = [t1]
		assert c.img == "img"
		assert c.terms
		Example e = new Example()
		e.terms = [t1,t2,t3]
		assert e.terms
	}
	
	@Test
	void exampleDirtyComposite() {
		Example e = new Example()
		e.terms.addAll([t1,t2,t3])
		 
		assert e.dirty
		e.dirty = false		
		e.enTerm.pron="changed"
		assert e.dirty				
		assert e.enTerm.pron =="changed"
	}
}
