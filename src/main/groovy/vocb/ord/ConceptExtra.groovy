package vocb.ord



import java.util.Map.Entry

import vocb.data.Concept


public class ConceptExtra {

	Concept c
	Object solver

	@Lazy int difficulty = {
		assert c
		assert solver
		solver.dfc.conceptDifficulty(c)
	}()

	@Lazy LinkedHashMap<Concept, Double> similarities=  {
		assert c
		assert solver
		//println "${solver.class.hashCode()} ${vocb.ord.OrderSolver.class.hashCode()}"
			
		solver.initialSelection
		.collectEntries {[it, solver.sm.conceptSimilarityNorm(c, it)]}
		.findAll {Concept c, Double d -> d > 0.15d && d <0.99 }
		.sort {Entry<Concept, Double>  a, Entry<Concept, Double>  b ->
			b.value <=> a.value
		}.take(20)
	}()

	@Override
	public String toString() {
		"$c.firstTerm($difficulty, ${similarities.keySet().take(50).collect{it.firstTerm} })"
	}
}


