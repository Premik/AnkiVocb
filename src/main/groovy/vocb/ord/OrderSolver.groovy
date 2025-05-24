package vocb.ord



import java.nio.file.Path
import java.util.stream.IntStream
import java.util.stream.Stream

import vocb.data.Manager


public class OrderSolver {

	Path dbStoragePath
	String dbConceptFilename

	@Lazy SolvingContext ctx = {
		new SolvingContext(dbMan:new Manager().tap {
			if (dbStoragePath) storagePath =dbStoragePath
			if (dbConceptFilename) conceptFilename= dbConceptFilename
			load()
		})
	}()

	List<Order> gen = []
	int genNum = 0

	public List<Order> getBestFirst() {
		gen.sort {
			Order o1, Order o2->
			o2.fitness <=> o1.fitness
		}
	}

	void spawn(int count = 100) {
		gen.addAll( (0..count).collect {
			ctx.createInitialOrder(genNum).mix()
		})		
	}

	IntStream getRndIndexes() {
		ctx.rnd.ints(0, gen.size())
	}

	Stream<Tuple2<Order,Order>> getPairs() {
		rndIndexes.mapToObj {
			Integer i->
			new Tuple2(gen[i], gen[ctx.rnd.nextInt(gen.size())])
		}
	}

	void crossSome(int count=10) {
		pairs.limit(count).forEach {
			Tuple2<Order,Order> p->
			println "${p}"
			Order o = p.v1.crossWith(p.v2, genNum)
			println "${p}  --> $o"
			gen.add(o)
		}
	}

	void removeWeaks(int count=10) {
		
	}
	
	void runEpoch() {
		
		spawn(100)
		for (int i=0;i<100;i++) {
			genNum++
			crossSome()
			if (i % 10 == 0) {
				printStat()
			}
		}
	}
	
	void printStat() {
		List<Order> b = bestFirst
		String fr = "${b[0].fitness.round(2)}..${b[-1].fitness.round(2)}"
		println "$fr gen:($genNum) size:${b.size()}"
		
	}

	//
	static void main(String... args) {
		//println "${vocb.ord.OrderSolver.class.hashCode()}"
		new OrderSolver().tap {
			runEpoch()
			//spawn()
			//gen.each {println "${it}"}
			//crossSome()

		}


	}


}
