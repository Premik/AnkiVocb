package vocb.data

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import vocb.TestUtils

class ManagerTest {



	Path tempDir = Files.createTempDirectory("ankivocbTest")
	
	Manager m = new Manager(defaultStoragePath: tempDir )

	Term t1 = new Term("apple", "en")
	Term t2 = new Term("jablko", "cs")
	Term s1 = new Term("Apple helps.", "en")
	Term s2 = new Term("Jablko pomáhá", "cs")
	Concept c = new Concept(state: "state", img:"", freq:1.1d, location: m.defaultConceptsLocation).tap {
		terms.addAll([t1, t2])
	}
	

	Example e= new Example().tap {
		terms.addAll([s1,s2])		
	}
	

	@Test
	void addData() {
		m.db.concepts.add(c)
		m.save()
		m.load()
		Path cp1 = m.defaultConceptsLocation.storagePath
		Path tempFile2 =  Files.createTempDirectory("ankivocbTestReload")
		m.defaultConceptsLocation.storageRootPath = tempFile2		
		m.save()
		Path cp2 = m.defaultConceptsLocation.storagePath
		
		TestUtils.compareFiles(cp1.toFile(), cp2.toFile())
		//assert saved1 == saved2
	}

	@Test
	void testNumberOfStars() {
		assert m.numberOfStarsFreq(0) ==null
		assert m.numberOfStarsFreq(10) == 0
		assert m.numberOfStarsFreq(20*1000) == 1
		assert m.numberOfStarsFreq(60*1000) == 2
		assert m.numberOfStarsFreq(200*1000) == 3

		assert m.numberOfStarsFreq(null) == null
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

	@Test
	void indexTest() {
		m.db.concepts.add(c)
		m.db.examples.add(e)
		m.reindex()
		assert m.conceptByFirstTerm["apple"] == c
		assert m.conceptsByEnWordsInSample
		assert m.conceptsByEnWordsInSample["apple"].contains(c)
	}
}
