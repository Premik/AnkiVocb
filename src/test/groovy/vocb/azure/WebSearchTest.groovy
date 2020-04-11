package vocb.azure

import java.nio.file.Paths

import org.junit.jupiter.api.Test

import groovy.json.JsonSlurper
import vocb.HttpHelper
import vocb.SimpleFileCache

class WebSearchTest {
	
	HttpHelper httpHelper= new HttpHelper(cache: new SimpleFileCache([rootPath : Paths.get('/tmp/test')]))

	BingWebSearch search = new BingWebSearch(httpHelper:httpHelper )
	String imgResultStr = getClass().getResource('/vocb/azure/ImageSearchResponse.json').text
	Object imgRes = new JsonSlurper().parseText(imgResultStr)
	
	@Test
	void thumbnailsFromSearchResultTest() {
		List<URL> lst = search.thumbnailsFromSearchResult(imgRes)
		assert lst
		assert lst.size() > 2
		assert lst[0] as String ==	"https://tse1.mm.bing.net/th?id=OIP.MSGPEQThAr2cHnsmxxBTjQHaFO&pid=Api"
		println lst
			
	
	}

	
}
