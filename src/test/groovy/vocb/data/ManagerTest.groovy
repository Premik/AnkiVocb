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

	Term t1En = new Term("apple", "en")
	Term t1Cs = new Term("jablko", "cs")
	Term s1En = new Term("Apple helps.", "en")
	Term s1Cs = new Term("Jablko pomáhá", "cs")
	Concept c1 = new Concept(state: "state", img:"", freq:1.1d, location: m.defaultConceptsLocation).tap {
		terms.addAll([t1En, t1Cs])
	}

	Example e1= new Example().tap {
		terms.addAll([s1En, s1Cs])
	}

	Term t2En = new Term("I'm (I am)", "en")
	Term t2Cs = new Term("Já jsem (zkráceně)", "cs")
	Concept c2 = new Concept(location: m.defaultConceptsLocation).tap {
		terms.addAll([t2En, t2Cs])
	}




	@Test
	void addData() {
		m.db.concepts.addAll([c1, c2])
		m.save()
		//m.load()
		Path cp1 = m.defaultConceptsLocation.storagePath
		Path tempFile2 =  Files.createTempDirectory("ankivocbTestReload")
		m.defaultConceptsLocation.storageRootPath = tempFile2
		m.save()
		Path cp2 = m.defaultConceptsLocation.storagePath

		TestUtils.compareFiles(cp1.toFile(), cp2.toFile())
		//assert saved1 == saved2
	}

	@Test
	void testNumberOfStarsCorners() {
		assert m.numberOfStarsFreq(0) ==null
		assert m.numberOfStarsFreq(10) == 0
		assert m.numberOfStarsFreq(null) == null
	}

	@Test
	void testNumberOfStars() {
		Manager mr = new Manager().tap {load()}	
		Closure<Integer> ns = { String w->
			mr.numberOfStarsFreq(mr.conceptByFirstTerm[w]?.freq)
		}
		assert ns("and") == 5
		assert ns("was") == 5
		assert ns("make") == 4
		assert ns("some") == 4
		assert ns("back") == 3
		assert ns("gave") == 3
		assert ns("open") == 2
		assert ns("kind") == 2
		assert ns("everyone") == 1
		assert ns("rule") == 1
		assert ns("planetary") == 0
		assert ns("jingle") == 0
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
		m.db.concepts.addAll([c1, c2])
		m.db.examples.add(e1)
		m.reindex()
		assert m.conceptByFirstTerm["apple"] == c1
		assert m.conceptsByEnWordsInSample
		assert m.conceptsByEnWordsInSample["apple"].contains(c1)
	}
}
