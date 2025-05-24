package vocb.ord

import java.nio.file.Path

import vocb.Helper
import vocb.data.Concept

public class Order {

	static int maxDist = 20
	static double[] rateDis = (0..maxDist).collect{
		1-(it/40)
	}
	// [1, 0.975, 0.95, 0.925, 0.9, 0.875, 0.85, 0.825, 0.8, 0.775, 0.75, 0.725, 0.7, 0.675, 0.65, 0.625, 0.6, 0.575, 0.55, 0.525, 0.5]

	SolvingContext ctx

	List<ConceptExtra> ord
	int genNum




	@Lazy double freqFitness = {
		assert ctx
		assert ord
		double d = ctx.freqWorstOrder.absSumDetlasFromFreqIdealOrder
		if (!d) return 0
		1-(absSumDetlasFromFreqIdealOrder/d)
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

	@Lazy double similarityFitness = {
		assert ctx
		assert ord

		double sum = 0
		for (int i=0;i<ord.size()-1;i++) {
			sum += similarityFitnessAt(i)
		}
		return sum
	}()

	double similarityFitnessAt(int indx) {
		ConceptExtra a = ord[indx]
		assert a
		double sum = 0
		//More similar words should have bigger distance
		for (int dist=1;dist<maxDist;dist++) {
			ConceptExtra b = ord[indx+dist]
			if (!b) break
			Double sim = a.similarities[b.c]
			if (!sim) continue
			sum+= 1-sim*rateDis[dist]
		}

		return sum/maxDist
	}


	double getFitness() {
		(freqFitness +
		similarityFitness*9)/10
	}

	public void finalizeOrder() {
		ord = ord.asImmutable()
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
		return new Order(ord:new ArrayList(ord), ctx:ctx)
	}

	public void lerpToPosition(int from, int to, double r) {
		int newOtherPos = (to -from)*r+from
		Helper.cutPaste(from, newOtherPos, ord)
		indexMapCached = null
	}

	public void lerpConcepttPosition(int pos, Order other, double r) {
		ConceptExtra a = ord[pos]
		assert a
		int otherPos = other.indexMap[a]
		//println "[$pos]*${r.round(2)} -> [$otherPos]"
		if (pos == otherPos) {
			return
		}
		lerpToPosition(pos, otherPos, r)
	}

	void lerpCross(Order ret, Order o) {
		int to = ctx.rndConceptIndex
		int from = ctx.rndConceptIndex
		if (to == from) return
		if (from >= to) {
			int t = to
			from = to
			to = t
		}
		
		for (int i=from;i<to;i++) {
			ret.lerpConcepttPosition(i, o, ctx.rndRate)
		}
	}

	Order crossWith(Order o,int genNum=0) {
		assert ord.size() ==  o.ord.size()
		Order ret = clone()
		ret.genNum = genNum
		int mutCount = Math.max(1d, ctx.rndConceptIndex/10)
		//int mutCount =1
		for (int i=0;i<mutCount;i++) {
			lerpCross(ret, o)
		}

		ret.finalizeOrder()
		return ret

	}

	@Override
	public String toString() {
		String ord = ord.findAll().take(4).collect{"$it.c.firstTerm:$it.id"}
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

	public void toRootedYaml(PrintWriter pw) {
		ord.each {
			pw.println "- ${it.c.firstTerm}"
		}
	}

	public void save(Path p) {
		p.getParent().toFile().mkdirs()
		p.withPrintWriter("UTF-8") {
			toRootedYaml(it)
		}
	}

	public void fromRootedYaml(Reader r) {
		LinkedHashSet<String> newOrd = parseOrderYaml(r)
		
		List<Order> leftOver = new LinkedList(ord?:[])
		int origSize = leftOver.size()

		ord = newOrd.collect { String trm->
			ConceptExtra ce = ctx.byFirstTerm[trm]
			if (leftOver.remove(ce)) {
				return ce
			}  else {
				return null
			}

		}.findAll()
		ord.addAll(leftOver)

		assert origSize == 0 || ord.findAll().size() == origSize
	}

	public void loadYaml(Path p) {
		p.withReader("UTF-8") {			
			fromRootedYaml(it)
		}
	}
	
	

	
	
	
	public static Set<String> parseOrderYaml(Reader r) {
		LinkedHashSet<String> ret = [] as LinkedHashSet
		r.splitEachLine(/\s*-\s+/) {
			assert it[1]
			ret.add(it[1])
		}
		return ret
	}
	
	
	
}
