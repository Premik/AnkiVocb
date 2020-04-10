package vocb.azure

import groovy.json.JsonSlurper
import vocb.HttpHelper
import vocb.SearchData

public class BingWebSearch {

	// http -v  https://api.cognitive.microsoft.com/bing/v7.0/images/search\?q\=sailing+dinghies\&mkt\=en-us\&answerCount\=3 Ocp-Apim-Subscription-Key:$AZURE_KEY

	AzureEnv azEnv = new AzureEnv()
	@Lazy(soft=true) HttpHelper httpHelper
	@Lazy JsonSlurper jsonSlurper
	
	
		Object search(URL url) {
		assert httpHelper
		assert url
		Object ret
		println "Searching $url"
		httpHelper.withDownloadResponse(azEnv.imgSearchHttpHeaders, url) { BufferedInputStream res->
			ret = jsonSlurper.parse(res)
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
		URL sUrl = azEnv.imageSearchUrl(s.q, s.count, s.imageType, s.license)
		Object resulJson = search(sUrl)
		List<URL> urls = thumbnailsFromSearchResult(resulJson)
		s.results = urls
		return s
	}
	
	SearchData thumbnailSearch(String q, int count=3) {
		return thumbnailSearch(new SearchData(q:q, count:count))		
	}


	static void main(String... args) {
		BingWebSearch bs = new BingWebSearch()
		bs.httpHelper = new HttpHelper()
		//println System.getenv(bs.AZURE_KEY_ENV)
		//println bs.buildSearchUrl("test")
		//println bs.getHttpHeaders()
		//Object res= bs.search(bs.buildSearchUrl("test"))
		println bs.thumbnailSearch(new SearchData(q:"test4"))

	}
}
