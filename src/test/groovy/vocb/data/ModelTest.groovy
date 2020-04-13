package vocb.data

import java.nio.file.Files
import java.nio.file.Path

import org.junit.jupiter.api.Test

import vocb.TestUtils

class ModelTest {
	
	Term t1 = new Term("en1", "en")
	Term t2 = new Term("cs1", "cs")
	Term t3 = new Term("cs2", "cs")
	Concept c = new Concept(terms: [en1:t1, cs1:t2, cs2:t3])
	
	

		
	@Test
	void resaveBlank() {
		
	}
	

}
