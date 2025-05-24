package vocb.ord

import java.nio.file.Paths

import org.apache.groovy.parser.antlr4.GroovyParser.CreatedNameContext
import org.junit.jupiter.api.Test

class OrderSolverTest {

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
		}
	}
	
	@Test
	void orderDeltas() {
		sol.with {
			Order o = ctx.createInitialOrder()			
			assert o == ctx.createInitialOrder()
			ctx.freqIdealOrder.deltasFromFreqIdealOrder.every {it == 0}
			o.deltasFromFreqIdealOrder.any{it != 0} 
		}
	}
	
	@Test
	@Disa
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
}
