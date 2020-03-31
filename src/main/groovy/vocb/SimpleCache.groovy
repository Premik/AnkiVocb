package vocb;

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

public class SimpleCache {


	Path rootPath = Paths.get(System.getProperty("user.home"), ".cache", "ankivocb")

	Path subPathForKey(String key) {
		assert key
		assert key.length() > 2
		return rootPath.resolve(key[0]).resolve(key)
	}

	boolean isCached(String key) {
		Files.exists( subPathForKey(key))
	}


	static void main(String... args) {
		SimpleCache c = new SimpleCache()
	}
}
