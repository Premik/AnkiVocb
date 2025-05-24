package vocb.anki.crowd

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.junit.rules.TemporaryFolder

import java.nio.file.Path

//@ExtendWith(TemporaryFolder)
class MediaManTest {

	MediaMan mm


	@BeforeEach
	void init(@TempDir Path tempDir) {
		mm =new MediaMan(tempDir.toFile())
	}



	@Test
	void wordName() {
		String word="Příšerně žlutý !@#"
		String fn = mm.fileForWord(word)
		assert fn.contains("Priser")
		assert !fn.contains("!@#")
	}
}
