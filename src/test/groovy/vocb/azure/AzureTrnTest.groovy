package vocb.azure

import org.junit.jupiter.api.Test

import vocb.Helper

class AzureTrnTest {

	String clipRps = getClass().getResource('/vocb/azure/DictLookupResponse-clip.json').text


	AzureTranslate trn = new AzureTranslate()

	@Test
	void thumbnailsFromSearchResultTest() {
		List<String> tl =trn.extractTopTrns(Helper.parseJson(clipRps))
		assert tl
		assert tl == ["klip", "sponka", "svorku", "spona", "galerie"]
	}
}
