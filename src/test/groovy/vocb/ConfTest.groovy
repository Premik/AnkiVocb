package vocb;


import org.junit.jupiter.api.Test


class ConfTest {


	@Test void resolvePlainCpRes() {	 
		assert ConfHelper.instance.resolveResExactName("Plaintext.txt")?.text == "test"
	}
	
	//twoFilesArchive
}
