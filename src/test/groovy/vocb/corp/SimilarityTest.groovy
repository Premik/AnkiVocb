package vocb.corp

import java.util.stream.Collectors

import org.junit.jupiter.api.Test

import vocb.corp.WordNormalizer

class SimilarityTest {

	Similarity norm = new Similarity()

	@Test
	void allSubstrings() {		
		assert norm.allSubstringsWithLen("abbcde", 2) ==['ab', 'bb', 'bc', 'cd', 'de']
		assert norm.allSubstringsOf("abc") ==['abc', 'ab', 'bc', 'a', 'b', 'c']
		assert norm.allSubstringsWithLen("ab", 5) ==[]
		
	}
	
	@Test
	void distanceMatches() {
		def a = ["a", "b", "c"]
		def b= ["a","x", "b", 'y'] 
		assert norm.distanceMatches(a, b) ==[0, 1, -1]
		assert norm.distanceMatches(b, a) ==[0, -1, 1, -1]
		assert norm.distanceMatches(a, []) ==[-1, -1, -1]
		assert norm.distanceMatches([], []) ==[]
	}
	
	@Test
	void distancesWithLen() {
		assert norm.similarWithLen("abc", "xaby", 2) == 0.5
		assert norm.similarWithLen("abc", "xyz", 2) == 0
	
	}

	
	@Test
	void similarSubstrings() {
		norm.with {
			String s = "abc"
			assert similarSubstrings(s, "xabc") > similarSubstrings(s, "xaybc") 			
			assert similarSubstrings(s, "abc") > similarSubstrings(s, "ab")
			assert similarSubstrings(s, "abc") > similarSubstrings(s, "ab")
		}
		
		
	
	}

	
}
