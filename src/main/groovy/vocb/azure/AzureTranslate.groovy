package vocb.azure

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import vocb.Helper
import vocb.HttpHelper
import vocb.SearchData
import static vocb.Helper.utf8

public class AzureTranslate {

	// http -v  https://api.cognitive.microsoft.com/bing/v7.0/images/search\?q\=sailing+dinghies\&mkt\=en-us\&answerCount\=3 Ocp-Apim-Subscription-Key:$AZURE_KEY

	AzureEnv azEnv = new AzureEnv()
	HttpHelper httpHelper
	JsonSlurper jsonSlurper = new JsonSlurper()
	JsonBuilder jsonBuilder = new JsonBuilder()

	Map trnJsonrunTrn(String text, String srcLang="en", String destLang="cs") {
		//https://docs.microsoft.com/en-us/azure/cognitive-services/translator/reference/v3-0-dictionary-lookup
		//http -v post "https://api.cognitive.microsofttranslator.com/dictionary/lookup?api-version=3.0&from=en&to=es" Ocp-Apim-Subscription-Key:$AZURE_KEY  <<< '[{"Text":"fly"}]'
		//http -v post "https://api.cognitive.microsofttranslator.com/dictionary/lookup?api-version=3.0&from=en&to=es" Ocp-Apim-Subscription-Key:$AZURE_KEY  <<< '[{"Text":"fly"}]'
		List ret

		JsonBuilder jsonBuilder = new JsonBuilder()
		def rq= jsonBuilder ([[ "Text": text]])

		URL trnUrl = azEnv.translateUrl(srcLang, destLang)
		httpHelper.withUrlPostResponse(azEnv.dictLookupHttpHeaders, trnUrl, Helper.jsonToString(rq)) {InputStream inp ->
			ret = jsonSlurper.parse(inp, utf8)

		}
		assert ret?.size() : "Blank response"
		return ret[0]
	}

	public List<String> extractTopTrns(Map lookupResponse, double minConfidence=0.08, int maxCount=4) {
		assert lookupResponse
		lookupResponse.translations
				.findAll {it.confidence > minConfidence}
				.sort {b,a -> a.confidence <=> b.confidence }
				.take(maxCount)
				.collect {it.normalizedTarget}
	}


	static void main(String... args) {
		AzureTranslate bs = new AzureTranslate(httpHelper: new HttpHelper() )

		println Helper.jsonToString(bs.trnJsonrunTrn("prefix"))
		//println Helper.jsonToString(bs.trnJsonrunTrn("Well done"))

	}
}
