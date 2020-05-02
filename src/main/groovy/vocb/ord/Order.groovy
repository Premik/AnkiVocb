package vocb.ord

public class Order {

	SolvingContext ctx

	ConceptExtra[] ord

	@Lazy double freqFitness = {
		assert ctx
		assert ord		
		1-(absSumDetlasFromFreqIdealOrder/ctx.freqWorstOrder.absSumDetlasFromFreqIdealOrder)
	}()

	@Lazy int[] deltasFromFreqIdealOrder = deltasFrom(ctx.freqIdealOrder)
	@Lazy int absSumDetlasFromFreqIdealOrder = deltasFromFreqIdealOrder.inject(0) {int sum, int d->
		sum+= Math.abs(d)
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
	
	

	double getFitness() {
		return freqFitness

	}

	public ConceptExtra getAt(int i) {
		assert ord
		return ord[i]
	}
	
	Order mix() {
		ConceptExtra[] newOrd = ord.findAll() as ConceptExtra[]
		newOrd.shuffle()
 		return new Order(ord:newOrd, ctx:ctx)
	}
	
	Order crossWith(Order o) {
		ConceptExtra[] newOrds = ord.findAll() as ConceptExtra[]
		Order ret = new Order(ord:newOrds, ctx:ctx)
		idx[] = o.ord
		
	}

	@Override
	public String toString() {
		String ord = ord.take(50).collect{"$it.c.firstTerm $it.id  "}
		"$fitness $ord"
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
