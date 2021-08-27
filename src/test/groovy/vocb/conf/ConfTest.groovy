package vocb.conf;


import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import vocb.conf.ConfHelper


class ConfTest {


	@Test 
	void resolvePlainCpRes() {	 
		assert new ConfHelper().resolveResExactName("Plaintext.txt")?.text == "test"
	}
	
	
	@Test
	void loadCustomConfigCp() {
		
		assert !ConfHelper.cfg.testString
		ConfHelper.instance.loadAndMergeConfig("test")
		assert ConfHelper.cfg
		assert ConfHelper.cfg.testString == "This is test config"
	}

	
	@BeforeEach
	void before() {
		println "Cleanup"
		ConfHelper.@$instance = null
		ConfHelper.@$cfg = null
	}
	
	//twoFilesArchive
}
