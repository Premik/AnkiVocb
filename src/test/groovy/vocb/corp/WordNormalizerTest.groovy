package vocb.corp

import java.util.stream.Collectors

import org.junit.jupiter.api.Test

import vocb.corp.WordNormalizer

class WordNormalizerTest {

	WordNormalizer norm = new WordNormalizer()

	@Test
	void simple() {
		List<String> t =  norm.tokens("123 aaa bbb fooBar a123 mm_word").collect(Collectors.toList())
		assert t == ["aaa", "bbb", "foobar", "mm", "word"]
	}


	@Test
	void stringSteam() {
		List<String> res = norm.tokens(["aaa bbb", "ccc ddd"].stream()).collect(Collectors.toList())
		assert res == ["aaa", "bbb", "ccc", "ddd"]
	}
}
