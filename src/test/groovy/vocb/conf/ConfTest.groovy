package vocb.conf;


import org.junit.jupiter.api.Test

import vocb.conf.ConfHelper


class ConfTest {


	@Test 
	void resolvePlainCpRes() {	 
		assert new ConfHelper().resolveResExactName("Plaintext.txt")?.text == "test"
	}
	
	//twoFilesArchive
}
