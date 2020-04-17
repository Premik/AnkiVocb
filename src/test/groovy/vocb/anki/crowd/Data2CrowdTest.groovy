package vocb.anki.crowd

import org.junit.jupiter.api.Test

class Data2CrowdTest {

	Data2Crowd dc = new Data2Crowd()


	@Test
	void testNumberOfStars() {
		assert dc.numberOfStarts(0) ==null
		assert dc.numberOfStarts(10) == 0
		assert dc.numberOfStarts(16*1000) == 1
		assert dc.numberOfStarts(600*1000) == 2
		assert dc.numberOfStarts(1600*1000) == 3

		assert dc.numberOfStarts(null) == null
	}
}


