package vocb.azure

import java.nio.charset.StandardCharsets

import groovy.json.JsonSlurper
import vocb.Helper
import vocb.SimpleFileCache

public class BingWebSearch {

	// http -v  https://api.cognitive.microsoft.com/bing/v7.0/images/search\?q\=sailing+dinghies\&mkt\=en-us\&answerCount\=3 Ocp-Apim-Subscription-Key:$AZURE_KEY

	String AZURE_KEY_ENV="AZURE_KEY"
	String baseUrl = "https://api.cognitive.microsoft.com/bing/v7.0/images/search"
	String lastClientId

	SimpleFileCache cache = new SimpleFileCache()

	//https://docs.microsoft.com/en-us/rest/api/cognitiveservices-bingsearch/bing-web-api-v7-reference
	URL buildSearchUrl(String q, int count=3, String imageType="Clipart", String license="Public") {
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
		println "Searching $url"
		Helper.withUrlGetResponse(httpHeaders, url) { BufferedInputStream res->
			ret = new JsonSlurper().parse(res)
		}
		return ret
	}

	Object searchTryCache(URL url) {
		String key = url.toString()
		Object searchResult
		if (cache.isCached(key)) {
			println "Cache hit for $key"
			cache.subPathForKey(key).withInputStream { InputStream istr ->
				searchResult = new JsonSlurper().parse(istr)
			}
		} else {
			searchResult = search(url)
			cache.subPathForKey(key) << Helper.jsonToString(searchResult)
		}
		return searchResult
	}

	void downloadTryCacheWithInputStream(URL url, Closure c) {
		assert url
		String key = url.toString()
		if (cache.isCached(key)) {
			println "Cache hit for $key"
			cache.subPathForKey(key).withInputStream { BufferedInputStream imgStr->
				c(imgStr, url)
			}
			return
		}
		Helper.withUrlGetResponse(url) {BufferedInputStream res->
			println url
			cache.subPathForKey(key) << res
		}
		cache.subPathForKey(key).withInputStream {BufferedInputStream imgStr->
			c(imgStr, url)			
		}
	}

	List<URL> thumbnailsFromSearchResult(Object searchResult) {
		searchResult.value
				.collect { it.thumbnailUrl}
				.collect { new URL(it) }
	}

	void withEachThumbnailStream(String q, int count=3, String imageType="Clipart", String license="Public", Closure c) {
		URL sUrl = buildSearchUrl(q, count, imageType,license)
		Object resulJson = searchTryCache(sUrl)
		List<URL> urls = thumbnailsFromSearchResult(resulJson)
		urls.each {
			downloadTryCacheWithInputStream(it, c)			
		}
	}




	static void main(String... args) {
		BingWebSearch bs = new BingWebSearch()
		//println System.getenv(bs.AZURE_KEY_ENV)
		//println bs.buildSearchUrl("test")
		//println bs.getHttpHeaders()
		//Object res= bs.search(bs.buildSearchUrl("test"))
		bs.withEachThumbnailStream("test", 5) {BufferedInputStream bis, URL url ->
			println "Got stream for $url"
			
		}
		
	}
}
