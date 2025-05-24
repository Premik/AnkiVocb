package vocb.azure

import org.junit.jupiter.api.Test

import vocb.Helper

class AzureTrnTest {

	String clipRps = getClass().getResource('/vocb/azure/DictLookupResponse-clip.json').text
	
	String kittyEx = getClass().getResource('/vocb/azure/ExampleDictResponse-kitty.json').text


	AzureTranslate trn = new AzureTranslate()

	@Test
	void thumbnailsFromSearchResultTest() {
		List<String> tl =trn.extractTopTrns(Helper.parseJson(clipRps))
		assert tl
		assert tl == ["klip", "sponka", "svorku", "spona", "galerie"]
	}
	
	@Test
	void extractExamples() {
		assert kittyEx
		List<Tuple2<String, String>> tl =trn.extractExamples(Helper.parseJson(kittyEx))
		assert tl
		assert tl.size() == 5
		assert tl[0][0] == "Kitty needs some attention."
	}
}
