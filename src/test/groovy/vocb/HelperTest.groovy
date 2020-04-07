package vocb

import org.junit.jupiter.api.Test

import vocb.Helper

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
}
