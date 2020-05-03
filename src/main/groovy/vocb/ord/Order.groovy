package vocb.ord

import vocb.Helper

public class Order {

	SolvingContext ctx

	List<ConceptExtra> ord
	int genNum

	@Lazy double freqFitness = {
		assert ctx
		assert ord
		1-(absSumDetlasFromFreqIdealOrder/ctx.freqWorstOrder.absSumDetlasFromFreqIdealOrder)
	}()

	@Lazy int[] deltasFromFreqIdealOrder = deltasFrom(ctx.freqIdealOrder)
	@Lazy int absSumDetlasFromFreqIdealOrder = deltasFromFreqIdealOrder.inject(0) {int sum, int d->
		sum+= Math.abs(d)
	}

	private Map<ConceptExtra, Integer> indexMapCached

	Map<ConceptExtra, Integer> getIndexMap() {
		if (indexMapCached == null) {
			indexMapCached =  (ord as List).withIndex().collectEntries{ ConceptExtra ce, int i ->
				[ce, i]
			}
		}
		return indexMapCached
	}

	int[] deltasFrom(Order o) {
		assert o?.ord
		assert ord.size() == o.ord.size()
		int[] ret = new int[ord.size()]
		for (int i =0;i<ord.size();i++) {
			ret[i] = ord[i].id - o.ord[i].id
		}
		return ret
	}
	
	public void finalizeOrder() {
		ord = ord.asImmutable()
	}



	double getFitness() {
		return freqFitness
	}

	public ConceptExtra getAt(int i) {
		assert ord
		return ord[i]
	}

	Order mix() {
		clone().tap {
			ord.shuffle(ctx.rnd)
		}
	}

	Order clone() {
		return new Order(ord:ord.findAll(), ctx:ctx)
	}
	
	public void lerpToPosition(int from, int to, double r) {		
		int newOtherPos = (to -from)*r+from
		Helper.cutPaste(from, newOtherPos, ord)
		indexMapCached = null
	}

	public void lerpConcextPosition(int pos, Order other, double r) {		
		ConceptExtra a = ord[pos]
		assert a
		int otherPos = other.indexMap[a]
		//println "[$pos]*${r.round(2)} -> [$otherPos]"
		if (pos == otherPos) {
			return
		}
		lerpToPosition(pos, otherPos, r)
	}

	Order crossWith(Order o,int genNum=0) {
		Order ret = clone()
		ret.genNum = genNum
		int mutCount = Math.max(10d, ctx.rndConceptIndex/4)
		
		for (int i=0;i<mutCount;i++) {
			//println ctx.rndRate
			ret.lerpConcextPosition(ctx.rndConceptIndex, o, ctx.rndRate)
		}
		ret.finalizeOrder()
		return ret
		
	}

	@Override
	public String toString() {
		String ord = ord.take(4).collect{"$it.c.firstTerm:$it.id"}
		"${fitness.round(2)} ($genNum) $ord"
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) return true
		if (!(obj instanceof Order)) return false
		return ord == ((Order)obj).ord
	}

	@Override
	public int hashCode() {
		return ord.hashCode()
	}
}
