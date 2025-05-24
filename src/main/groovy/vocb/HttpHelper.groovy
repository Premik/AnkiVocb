package vocb

import java.text.Normalizer

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import static vocb.Helper.utf8

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

	public void withUrlPostResponse(Map hdrs=[:], URL u, String payload, Closure<InputStream> c) {
		assert u
		HttpURLConnection conn = u.openConnection()
		conn.with {
			requestMethod = "POST"
			doOutput = true
			connectTimeout = connectionProps.connectTimeout
			readTimeout = connectionProps.readTimeout
			hdrs.each { String k, v->
				setRequestProperty(k, v.toString())
				//println "$k: $v"
			}
			println """\
				$u
				$payload
			""".stripIndent()

			outputStream.write(payload.getBytes(utf8))
			//println inputStream.text
			assert responseCode >=200 && responseCode< 300 : "Http error $responseCode $errorStream.text"
		}

		c(conn.inputStream)
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
		withUrlGetResponse(hdrs, url) { BufferedInputStream res->
			println url
			cache.subPathForKey(key).toFile() << res
		}
		cache.subPathForKey(key).withInputStream { BufferedInputStream imgStr->
			c(imgStr)
		}
	}
}
