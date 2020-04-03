package vocb

import java.text.Normalizer

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

public class HttpHelper {

	SimpleFileCache cache = new SimpleFileCache()

	Map connectionProps = [
		/**/connectTimeout: 5000,
		/**/readTimeout: 10000		
	]


	public void withUrlGetResponse(Map hdrs=[:], URL u, Closure<BufferedInputStream> c) {
		Map requestProperties = ['Connection': 'close'] + hdrs
		BufferedInputStream b = u.newInputStream(connectionProps + [requestProperties: requestProperties])
		b.withCloseable(c)
	}

	void withDownloadResponse(Map hdrs=[:],URL url, Closure c) {
		assert url
		String key = url.toString()
		if (cache.isCached(key)) {
			println "Cache hit for $key"
			cache.subPathForKey(key).withInputStream { BufferedInputStream rsp->
				c(rsp)
			}
			return
		}
		withUrlGetResponse(hdrs, url) {BufferedInputStream res->
			println url
			cache.subPathForKey(key) << res
		}
		cache.subPathForKey(key).withInputStream {BufferedInputStream imgStr->
			c(imgStr)
		}
	}
}
