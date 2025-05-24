package vocb

import java.nio.file.Paths

import org.junit.jupiter.api.Test

class SimpleCacheTest {

	Random rnd = new Random()

	SimpleCache cache = new SimpleCache([rootPath : Paths.get('/tmp/test')])

	@Test
	void baisc() {
		assert cache.subPathForKey("foobar") as String == "/tmp/test/f/foobar"
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
		cache.subPathForKey(key).withPrintWriter {

		}
	}

}
