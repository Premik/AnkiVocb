package vocb.pkc

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import groovy.transform.CompileStatic
import vocb.pck.Pack


@CompileStatic
class PackTest {
	
	Path tempDir = Files.createTempDirectory("ankivocbPackTest")
		

	@Lazy static Path pkgRoot = {
		assert PackTest.getResource('/testPck/readme.md')
		URI ru = PackTest.getResource('/testPck/readme.md').toURI()
		return Paths.get(ru).parent
	}()
	
	//@Lazy Pack pack = new Pack(packageRootPath: pkgRoot, destRootFolder: tempDir)


	@Test
	void testList() {
		//assert pack.allPackagePaths.collect{it.fileName.toString()}.toSet() == ["flatPack", "child1", "child2", "grandChild"] as Set
		
	}
}
