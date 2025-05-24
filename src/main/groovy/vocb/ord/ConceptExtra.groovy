package vocb.ord



import java.util.Map.Entry

import vocb.data.Concept


public class ConceptExtra {

	Concept c
	SolvingContext ctx
	int id
	int hsh =0

	@Lazy int difficulty = {
		assert c
		assert ctx
		ctx.dfc.conceptDifficulty(c)
	}()

	@Lazy LinkedHashMap<Concept, Double> similarities=  {
		assert c
		assert ctx
		assert ctx.initialSelection
		//println "${solver.class.hashCode()} ${vocb.ord.OrderSolver.class.hashCode()}"

		ctx.initialSelection
				.collectEntries {[it, ctx.sm.conceptSimilarityNorm(c, it)]}
				.findAll {Concept c, Double d -> d > 0.15d && d <0.99 }
				.sort {Entry<Concept, Double>  a, Entry<Concept, Double>  b ->
					b.value <=> a.value
				}.take(20)
	}()

	@Override
	public String toString() {
		"${c?.firstTerm}"
	}

	
	@Override
	public int hashCode() {
		if (hsh == 0) {
			hsh =c.firstTerm.hashCode()
		}
		return hsh
	}
}


