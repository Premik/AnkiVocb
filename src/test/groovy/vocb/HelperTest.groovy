package vocb

import java.nio.file.Paths

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class HelperTest {


	@Test
	void jsonClone() {
		def j = Helper.parseJson('''{
          "items": [
           {"a":1,"b":2},
			{"a":3,"b":4}
           ]
        }
		''')
		assert j
		def items = j.items
		assert items.size == 2
		assert items[0].a == 1
		def aClone = Helper.cloneJson(items[0])
		assert aClone.a
		assert aClone.a == items[0].a
		assert aClone.b == items[0].b
		j.items+= aClone
		println Helper.jsonToString(j)
		assert j.items.size == 3
		assert j.items[2]
		assert j.items[2].a
		assert j.items[2].a == aClone.a
	}

	@Test
	void padList() {
		assert Helper.padList([1, 2, 3], 0, 1) == [1, 2, 3]
		assert Helper.padList([1, 2, 3], 0, 5) == [1, 2, 3, 0, 0]
		assert Helper.padList([1, 2, 3], 0, 1, true) == [1]
	}

	@Test
	void indentNextLines() {
		String s ="""\
        line1
        line2""".stripIndent()
		String s2 ="""\
        line1
          line2""".stripIndent()

		assert Helper.indentNextLines(s, 2) == s2
	}



	@Test
	void testSplitBy() {
		assert Helper.splitBy("abc", "b") == ["a", "b", "c"] as Tuple3
		assert Helper.splitBy("aabbcc", "bb") == ["aa", "bb", "cc"] as Tuple3
		assert Helper.splitBy("a b c", "b") == ["a ", "b", " c"] as Tuple3
		def s = Helper.splitBy("The quick brown fox jumps over the lazy dog.", "brown fox")
		assert s == ["The quick ", "brown fox", " jumps over the lazy dog."] as Tuple3
	}

	@Test
	void testSplitByCorner() {
		assert Helper.splitBy("a", "a") == ["", "a", ""] as Tuple3
		assert Helper.splitBy("...a", "a") == ["...", "a", ""] as Tuple3
		assert Helper.splitByWord("Hello world.", "hello") == ["", "Hello", " world."] as Tuple3
		assert Helper.splitByWord("Who does not like noodles?", "who") == ["", "Who", " does not like noodles?"] as Tuple3
	}


	@Test
	void testRx() {
		assert Helper.splitByRex("Hello world.", ~"world") == ["Hello ", "world", "."] as Tuple3
		assert Helper.splitByRex("Hello world.", ~/\s*world[\.]*/) == ["Hello", " world.", ""] as Tuple3
	}

	@Test
	void splitByWord() {
		assert Helper.splitByWord("Hello world.", "world") == ["Hello ", "world", "."] as Tuple3
		assert Helper.splitByWord("in class as usual.", "as") == ["in class ", "as", " usual."] as Tuple3
	}

	@Test
	@Disabled
	void splitByWordStrange() {
		def (a,b,c) =  Helper.splitByWord("Film was shot using hand-held camera.", "held")
		assert b == "held"
	}

	@Test
	void splitEachPair() {
		String ret = ""
		Helper.withEachPairInDistance([1, 2, 3], 1) { Integer i, Integer a, Integer b ->
			ret+= "$i: $a-$b "
		}
		assert ret == "0: 1-2 1: 2-3 "
	}

	@Test
	void withEachPairInDistance() {
		def pairs = []
		Helper.withAllPairs(["a", "b", "c"]) {Integer i, String a, String b ->
			pairs.add( [a, b])
		}
		assert pairs ==  [['a', 'b'], ['b', 'c']]
	}


	@Test
	void cutPaste() {
		assert Helper.cutPaste(0,0, [1]) == [1]
		assert Helper.cutPaste(0,1, [1,2]) == [2,1]
		assert Helper.cutPaste(1,0, [1,2]) == [2,1]
		assert Helper.cutPaste(1,0, [1,2,3,4,5]) == [2,1,3,4,5]
		assert Helper.cutPaste(0,4, [1,2,3,4,5]) == [2,3,4,5,1]
		assert Helper.cutPaste(4,0, [1,2,3,4,5]) == [5,1,2,3,4]
		assert Helper.cutPaste(1,3, [1,2,3,4,5]) == [1,3,4,2,5]
		
	}
	
	@Test
	@Disabled
	void eachMatchingFile() {
		new File("/tmp/work/1.txt") << "test"
		assert Helper.matchingFiles([Paths.get("/tmp")], "1.txt")
		
	}
}
