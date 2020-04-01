package vocb

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import org.junit.jupiter.api.Test

class SimpleCacheTest {

	Random rnd = new Random()

	SimpleFileCache cache = new SimpleFileCache([rootPath : Paths.get('/tmp/test')])

	@Test
	void baisc() {
		assert cache.subPathForKey("foobar") as String == "/tmp/test/3/foobar"
	}

	@Test
	void miss() {
		String key =Long.toHexString(rnd.nextLong())
		assert !cache.isCached(key)
	}

	@Test
	void create() {
		String key =Long.toHexString(rnd.nextLong())
		assert !cache.isCached(key)
		cache.subPathForKey(key).withPrintWriter { PrintWriter p ->
			p.print("test: $key")
		}
		assert cache.isCached(key)
		assert cache.subPathForKey(key).text.contains(key)
		
	}

}
