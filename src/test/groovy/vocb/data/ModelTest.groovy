package vocb.data

import org.junit.jupiter.api.Test

import vocb.Helper

class ModelTest {
	
	Term t1 = new Term("en1", "en")
	Term t2 = new Term("cs1", "cs", "tts")
	Term t3 = new Term("cs2", "cs")
	Concept c = new Concept(terms: [en1:t1, cs1:t2, cs2:t3])
	
	

		
	@Test
	void termCompletness() {
		assert t1.completeness > 0.5
		assert t1.completeness < 0.9
		assert t2.completeness > 0.9
		assert new Term().completeness < 0.01		
	}
	
	@Test
	void conceptCompletness() {
		assert new Concept().completeness < 0.01
		assert c.completeness > 0.1
		//(0..10).each {println  "$it: ${Helper.progressBar(it/10)}" }
		
		
	}
	

}
