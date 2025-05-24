package vocb.ord

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test

import vocb.Helper
import vocb.data.Concept

class OrderTest {

	URL dbEntry = getClass().getResource('/vocb/data/orderingTest.yaml')



	@Lazy OrderSolver sol = {
		assert dbEntry
		File dbFile = new File(dbEntry.toURI()).tap {
			assert exists() : absolutePath
			assert isFile() : absolutePath
		}

		new OrderSolver(
				dbConceptFilename: dbFile.name,
				dbStoragePath:  Paths.get(dbFile.parentFile.toURI() )
				)
	}()

	@Test
	void setupTest() {
		sol.with {
			assert ctx.initialSelection
			assert ctx.concepts
			Order o = ctx.createInitialOrder()
			assert o == ctx.createInitialOrder()
			assert o == o.clone()
			assert o !== o.clone()
			assert o.ord == o.clone().ord
			assert o.ord !== o.clone().ord
			assert o.mix() != o
		}
	}

	@Test
	void orderDeltas() {
		sol.with {
			Order o = ctx.createInitialOrder()
			ctx.freqIdealOrder.deltasFromFreqIdealOrder.every {it == 0}
			o.deltasFromFreqIdealOrder.any{it != 0}
		}
	}

	@Test
	@Disabled
	void orderDeltaFinding() {
		sol.with {
			Order o = ctx.createInitialOrder()
			Order o2 = ctx.freqIdealOrder
			List<Integer> Δ = o2.deltasFrom(o)
			println "${o}"
			println "${Δ}"
			println "${o2}"
			Δ.withIndex().each {int d, int i->
				assert o[i] == o2[i+d]
			}
		}
	}

	@Test
	void freqOrder() {
		sol.with {
			Order o = ctx.createInitialOrder()
			assert o.ctx.freqIdealOrder.freqFitness == 1
			assert o.ctx.freqWorstOrder.freqFitness < 0.01
			assert o.freqFitness < ctx.freqIdealOrder.freqFitness
			assert o.freqFitness > ctx.freqWorstOrder.freqFitness
		}
	}

	@Test
	void indexMap() {
		sol.with {
			Order o = ctx.createInitialOrder()
			ConceptExtra ce1 = o.ord[0]
			ConceptExtra ce2 = o.ord[1]
			assert o.indexMap
			assert o.indexMap[ce1] == 0
			assert o.indexMap[ce2] == 1
		}
	}

	@Test
	@RepeatedTest(100)
	void cross() {

		Order o1 = sol.ctx.freqIdealOrder
		Order o2 = sol.ctx.createInitialOrder().mix()
		assert o2.freqFitness < o1.freqFitness
		Order o3 = o2.crossWith(o1).crossWith(o1).crossWith(o1).crossWith(o1)
		assert o3 != o2
		assert o3 != o1
		assert o3.freqFitness > o2.freqFitness
	}

	@Test
	void lerpPosBasic() {
		sol.with {
			Order o = ctx.createInitialOrder()
			ConceptExtra c0 = o.ord[0]
			ConceptExtra c_1 = o.ord[-1]
			assert c0
			assert c_1
			assert c0 != c_1
			Order no = o.clone()
			assert no[0] == c0
			assert no[-1] == c_1
			assert no == o
			assert no !==o
		}
	}

	@Test
	void lerpStaticPos() {
		sol.with {
			Order o = ctx.createInitialOrder()
			Order no = o.clone()
			no.lerpConcextPosition(0, o, 0d) //ration 0 -> no swap
			assert no == o
			no.lerpConcextPosition(0, o, 1d)
			assert no == o
		}
	}

	@Test
	void lerpExtremes() {

		Order o = sol.ctx.createInitialOrder()
		Order no = o.clone()
		Helper.cutPaste(0, no.ord.size()-1, no.ord)
		assert no != o
		assert no[0] == o[1]
		assert o[0] == no[-1]
		Order no1 = no.clone()
		no.lerpConcextPosition(0, o, 0d) //ration 0 -> no swap
		assert no1 == no

		assert no.indexMap[o[0]] == no.ord.size()-1
		no.lerpConcextPosition(no.ord.size()-1, o, 1d)
		assert no != no1
		assert no == o
	}

	@Test
	void lerpImmu() {
		Order o = sol.ctx.createInitialOrder()
		Order o2 = o.clone()
		Random rnd = new Random(123)
		for (int i=0;i<100;i++) {
			o2.lerpConcextPosition(rnd.nextInt(o.ord.size()), o, rnd.nextDouble() )
			assert o2 == o
		}
	}

	@Test
	void rnd() {
		double cnt = 1000
		double[] rnds = (0..cnt).collect{ sol.ctx.rndRate}
		double avg = rnds.average()
		assert   avg > 0.4 && avg < 0.6
		int zeros = rnds.findAll {it <0.001}.size()
		int ones = rnds.findAll {it >0.99}.size()
		//println "$zeros $ones $avg ${rnds[0..10]}"
		assert zeros > cnt/100
		assert Math.abs(zeros - ones) < 10
		assert zeros/cnt > 0.3
		assert ones/cnt > 0.3
	}

	@Test
	@Disabled
	void solveSort() {
		sol.with {
			maxGens = 5000
			initialSpawn = 50
			crossAtOnce = 50
			maxPopsize = 500
		}
		sol.runEpoch()
		println sol.ctx.freqIdealOrder
		println sol.bestFirst[0]
	}

	@Test
	void yamlStor() {
		Order o= sol.ctx.createInitialOrder().mix()
		Path tempFile = Files.createTempFile("vobctest", ".yaml")
		println tempFile
		o.save(tempFile)
		
		Order o2= sol.ctx.createInitialOrder().mix()
		
		
		assert o2 != o
		o2.load(tempFile)
		
		println o2
		assert o2 == o
		o2.ord.removeLast()
		assert o2 != o
		tempFile.withReader("UTF-8") {
			o2.fromRootedYaml(it)
		}
		assert o2 == o		
	}
	
	@Test
	void simFitnessSingle() {
		Order o= sol.ctx.createInitialOrder()
		assert o[2].c.firstTerm == "of"
		assert o[4].c.firstTerm == "to"
		Order farther = o.clone()
		farther.lerpToPosition(2, 0, 1d)
		assert 	farther[0].c.firstTerm == 'of'
		assert o.similarityFitness < farther.similarityFitness
		/*o.ord.each {
			String sim = it.similarities.take(5).collect{k,v-> "${v.round(2)} $k.firstTerm"}
			println "${it} $sim  "
		}*/
	}
	
	@Test
	void simFitness() {
		Order o= sol.ctx.createInitialOrder()
		
		/*o.ord.each {
			String sim = it.similarities.take(5).collect{k,v-> "${v.round(2)} $k.firstTerm"}
			println "${it} $sim  "
		}*/
		
	}
}
