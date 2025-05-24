package vocb.data

import java.nio.file.Files
import java.nio.file.Path

import org.junit.jupiter.api.Test

import vocb.TestUtils

class ManagerTest {

	
	
	Path tempDir = Files.createTempDirectory("ankivocbTest")
	Manager m = new Manager(storagePath: tempDir )
	
	Term t1 = new Term("apple", "en")
	Term t2 = new Term("jablko", "cs")
	Concept c = new Concept(terms: [t1, t2], state: "state", img:"", freq:1.1d, origins:["o1", "o2"])


	void createBlank() {
		m.save()
		m.load()
	}
	
	
	@Test
	void resaveBlank() {
		createBlank()
		m.save()
	}
	
	@Test
	void addData() {
		createBlank()
		m.db.concepts.add(c)
		String saved1 =  m.save()
		m.load()
		Path tempFile2 = Files.createTempFile("tempfiles", ".yaml")
		String saved2 =  m.save(tempFile2)
		TestUtils.compareFiles(m.conceptsPath.toFile(), tempFile2.toFile())
		
		//assert saved1 == saved2
		
	}
}
