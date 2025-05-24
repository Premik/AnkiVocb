package vocb.corp

import vocb.data.Concept
import vocb.data.Manager


public class Difficulty {


	int conceptDifficulty(Concept c) {
		int d = 0
		if (!c.img) d+=3
		if (c.csAltTerm) d+=1
		int len = c.firstTerm.length()
		if (len <= 3)  {
			if (c.freq > 5*1000*1000) d+=4
			if (c.freq > 1000*1000) d+=2
		}
		if (len >= 5) d+=(len-4)/3

		return d
	}


	static void main(String... args) {
		Manager dbMan = new Manager().tap {
			load()
		}

		Difficulty n = new Difficulty().tap {
			println dbMan.db.concepts.sort { Concept a, Concept b->
				conceptDifficulty(b) <=> conceptDifficulty(a)
			}.take(200).collect {
				"${it.firstTerm} ${conceptDifficulty(it)}"
			}
		}
	}
}
