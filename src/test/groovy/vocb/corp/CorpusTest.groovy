package vocb.corp

import java.util.stream.Collectors

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class CorpusTest {
	
	Corpus c = Corpus.buildDef(2000)

	@Test
	void simple() {
		["apple", "door", "you", "are"].each {
			assert c.wordFreq[it.toLowerCase()] == c[it]
		}
	}
	
	@Test
	void phrase() {
		assert c["you are"]
		assert c["you are"] < c["you"]
		assert c["you are"] < c["are"]
	}
	
	@Test
	void bracket() {		
		assert c["you're (you are)"] == c["you are"]
		
	}

}
