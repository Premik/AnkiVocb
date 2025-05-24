package vocb.azure

import java.nio.charset.StandardCharsets

import groovy.json.JsonSlurper
import vocb.Helper
import vocb.HttpHelper
import vocb.SearchData

public class BingWebSearch {

	// http -v  https://api.cognitive.microsoft.com/bing/v7.0/images/search\?q\=sailing+dinghies\&mkt\=en-us\&answerCount\=3 Ocp-Apim-Subscription-Key:$AZURE_KEY

	String AZURE_KEY_ENV="AZURE_KEY"
	String baseUrl = "https://api.cognitive.microsoft.com/bing/v7.0/images/search"
	String lastClientId

	HttpHelper httpHelper

	//https://docs.microsoft.com/en-us/rest/api/cognitiveservices-bingsearch/bing-web-api-v7-reference
	URL buildSearchUrl(SearchData s)  {
		String utf8=StandardCharsets.UTF_8.toString()
		def u = { String key, Object val, String prefx="&"->
			if (val == null || val == "") return ""
			return "$prefx$key=${URLEncoder.encode(val.toString(), utf8)}"
		}
		return "$baseUrl${u 'q', s.q, '?'}${u 'count', s.count}${u 'imageType', s.imageType}${u 'license', s.license}".toURL()
	}

	Map<String,String> getHttpHeaders() {
		String key = System.getenv(AZURE_KEY_ENV)
		assert key: "Please export env variable $AZURE_KEY_ENV with the api key"

		Map<String,String> ret = ["Ocp-Apim-Subscription-Key": key]
		if (lastClientId) ret += ["X-MSEdge-ClientID": lastClientId]
		return ret
	}

	Object search(URL url) {
		assert httpHelper
		assert url
		Object ret
		println "Searching $url"
		httpHelper.withDownloadResponse(httpHeaders, url) { BufferedInputStream res->
			ret = new JsonSlurper().parse(res)
		}
		return ret
	}



	List<URL> thumbnailsFromSearchResult(Object searchResult) {
		searchResult.value
				.collect { it.thumbnailUrl}
				.collect { new URL(it) }
	}

	SearchData thumbnailSearch(SearchData s) {
		assert s
		assert s.q
		URL sUrl = buildSearchUrl(s)
		Object resulJson = search(sUrl)
		List<URL> urls = thumbnailsFromSearchResult(resulJson)
		s.results = urls
		return s
	}
	
	SearchData thumbnailSearch(String q, int count=3) {
		return thumbnailSearch(new SearchData(q:q, count:count))
		
	}


	static void main(String... args) {
		BingWebSearch bs = new BingWebSearch(httpHelper: new HttpHelper())
		//println System.getenv(bs.AZURE_KEY_ENV)
		//println bs.buildSearchUrl("test")
		//println bs.getHttpHeaders()
		//Object res= bs.search(bs.buildSearchUrl("test"))
		println bs.thumbnailSearch(new SearchData(q:"test"))

	}
}
