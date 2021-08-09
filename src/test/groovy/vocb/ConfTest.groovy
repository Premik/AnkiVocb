package vocb;


import org.junit.jupiter.api.Test


class ConfTest {


	@Test 
	void resolvePlainCpRes() {	 
		assert new ConfHelper().resolveResExactName("Plaintext.txt")?.text == "test"
	}
	
	//twoFilesArchive
}
