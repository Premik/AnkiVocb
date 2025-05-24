package vocb.azure;

import java.nio.charset.StandardCharsets

import groovy.json.JsonSlurper
import vocb.Helper

public class BingWebSearch {

	// http -v  https://api.cognitive.microsoft.com/bing/v7.0/images/search\?q\=sailing+dinghies\&mkt\=en-us\&answerCount\=3 Ocp-Apim-Subscription-Key:$AZURE_KEY

	String AZURE_KEY_ENV="AZURE_KEY"
	String baseUrl = "https://api.cognitive.microsoft.com/bing/v7.0/images/search"
	String lastClientId

	//https://docs.microsoft.com/en-us/rest/api/cognitiveservices-bingsearch/bing-web-api-v7-reference
	URL buildSearchUrl(String q, int count=4, String imageType="Clipart", String license="Public") {
		String utf8=StandardCharsets.UTF_8.toString()
		def u = { String key, Object val, String prefx="&"->
			if (val == null || val == "") return ""
			return "$prefx$key=${URLEncoder.encode(val.toString(), utf8)}"
		}
		return "$baseUrl${u 'q', q, '?'}${u 'count', count}${u 'imageType', imageType}${u 'license', license}".toURL()
	}

	Map<String,String> getHttpHeaders() {
		String key = System.getenv(AZURE_KEY_ENV)
		assert key: "Please export env variable $AZURE_KEY_ENV with the api key"

		Map<String,String> ret = ["Ocp-Apim-Subscription-Key": key]
		if (lastClientId) ret += ["X-MSEdge-ClientID": lastClientId]
		return ret
	}

	Object search(URL url) {
		assert url
		Object ret
		Helper.withUrlGetResponse(httpHeaders, url) { BufferedInputStream res->
			ret = new JsonSlurper().parse(res)
		}
		return ret
	}


	static void main(String... args) {
		BingWebSearch bs = new BingWebSearch()
		println System.getenv(bs.AZURE_KEY_ENV)
		println bs.buildSearchUrl("test")
		println bs.getHttpHeaders()
		Object res= bs.search(bs.buildSearchUrl("test"))
		println Helper.jsonToString(res)


	}
}
