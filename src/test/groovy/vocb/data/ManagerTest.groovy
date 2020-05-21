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
	Concept c = new Concept(terms: [apple:t1, jablko:t2], state: "state", img:"", freq:1.1d, origins:["o1", "o2"])


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

	@Test
	void testNumberOfStars() {
		assert m.numberOfStarts(0) ==null
		assert m.numberOfStarts(10) == 0
		assert m.numberOfStarts(20*1000) == 1
		assert m.numberOfStarts(200*1000) == 2
		assert m.numberOfStarts(400*1000) == 3

		assert m.numberOfStarts(null) == null
	}

	@Test 
	void mediaLinksBasic() {
		assert m.termd2MediaLink("kočka?", "ext") == "kocka.ext"
		assert m.mediaLinkPath("ml1") == tempDir.resolve('media').resolve("ml1")
		boolean resolveCalled =false
		String mediaLink = m.resolveMedia("term1", "txt")  { Path p->
			resolveCalled = true
			assert p == tempDir.resolve('media').resolve("term1.txt")
		}	
		assert resolveCalled
		assert mediaLink == "term1.txt"
	}
	
	@Test
	void mediaLinksGroup() {		
		assert m.mediaLinkPath("ml2", "grp") == tempDir.resolve('media').resolve("grp").resolve("ml2")
		boolean resolveCalled =false
		String mediaLink = m.resolveMedia("term2", "txt", "grp2")  { Path p->
			resolveCalled = true
			Path dir = tempDir.resolve('media').resolve("grp2")
			assert Files.exists(dir)
			assert p == dir.resolve("term2.txt")
		}
		assert resolveCalled
		assert mediaLink ==  "grp2/term2.txt"
		
	}
}
