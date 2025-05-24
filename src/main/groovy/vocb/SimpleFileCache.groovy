package vocb;

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

public class SimpleFileCache {


	Path rootPath = Paths.get(System.getProperty("user.home"), ".cache", "ankivocb")

	Path subPathForKey(Object keyObj) {
		assert keyObj != null
				
		String pfx = Integer.toHexString(Math.abs(keyObj.hashCode())%16)
		Path parent= rootPath.resolve(pfx)
		Files.createDirectories(parent)
		String key = Helper.word2Key(keyObj.toString(), true)
		return rootPath.resolve(pfx).resolve(key)
	}

	boolean isCached(Object key) {
		Files.exists( subPathForKey(key))
	}


	static void main(String... args) {
		SimpleFileCache c = new SimpleFileCache()
	}
}
