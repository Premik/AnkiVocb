package vocb.conf

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import groovy.transform.CompileDynamic
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
		assert tc.root
		
		List<CharSequence >val = tc.validate()
		if (val) val.each {
			println Ansi.color(it.toString(), Ansi.RED)
		}
		assert !val
		assert tc.children.size() == 2
		TreeConf chtf = tc.children.find().children[0]
		assert chtf
		assert !chtf.isRoot
		assert chtf.root == tc
		
		//assert pack.allPackagePaths.collect{it.fileName.toString()}.toSet() == ["flatPack", "child1", "child2", "grandChild"] as Set		
	}
	
	@Test
	@CompileDynamic
	void testConf() {		
		TreeConf tc = new TreeConf(path:pkgRoot)
		TreeConf child1 = tc.findByName("child1")
		assert child1
		assert child1.confPath
		assert child1.parent.confPath
		
		assert child1.thisConf.child1confKey == "child1confKey"
		assert child1.thisConf.confkey1 == "child1"		
		assert child1.parent.thisConf.parentConfKey == "parentConfKey"
		assert !child1.thisConf.parentConfKey
		
		assert child1.conf.child1confKey == "child1confKey"
		assert child1.conf.parentConfKey == "parentConfKey"
		 
	}
	
	@Test
	void testFiles() {
		
	
	}
}
