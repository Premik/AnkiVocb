package vocb.conf

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import groovy.transform.CompileStatic
import vocb.Ansi
import vocb.pck.Pack


@CompileStatic
class TreeConfTest {
	
	Path tempDir = Files.createTempDirectory("ankivocbPackTest")
		

	@Lazy static Path pkgRoot = {
		assert TreeConfTest.getResource('/testPck/readme.md')
		URI ru = TreeConfTest.getResource('/testPck/readme.md').toURI()
		return Paths.get(ru).parent
	}()
	
	
	@Test
	void testList() {
		
		TreeConf tc = new TreeConf(path:pkgRoot)
		List<CharSequence >val = tc.validate()
		if (val) val.each {
			println Ansi.color(it.toString(), Ansi.RED)
		}
		assert !val
		assert tc.children.size() == 2
		//assert pack.allPackagePaths.collect{it.fileName.toString()}.toSet() == ["flatPack", "child1", "child2", "grandChild"] as Set
		
	}
}
