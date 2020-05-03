package vocb.ord



import java.nio.file.Path
import java.util.concurrent.ForkJoinPool
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

import groovy.time.TimeCategory
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


	int maxPopsize = 5000
	List<Order> population = new ArrayList(5000)
	//List<Order> population = new LinkedList()
	int genNum = 0
	int maxGens=10000
	int initialSpawn = 100
	int spawnNew = 10
	int crossAtOnce=100


	public List<Order> getBestFirst() {
		population.sort { Order o1, Order o2->
			o2.fitness <=> o1.fitness
		}
	}

	void spawn(int count = 100) {
		population.addAll( (0..count).collect {
			ctx.createInitialOrder(genNum).mix()
		})
	}

	IntStream getRndIndexes() {
		ctx.rnd.ints(0, population.size())
	}

	Stream<Tuple2<Order,Order>> getPairs() {
		rndIndexes.mapToObj { Integer i->
			new Tuple2(population[i], population[ctx.rnd.nextInt(population.size())])
		}
	}

	void crossSome(int count=10) {
		Stream<Order> nextGen = pairs
				.limit(count)
				.parallel()
				.map {Tuple2<Order,Order> p->
					p.v1.crossWith(p.v2, genNum).tap {
						fitness
					}

					//assert o != p.v1
					//println "${p}  --> $o"
				}
		population.addAll(nextGen.collect(Collectors.toList()))
	}

	void removeWeakest(int count=maxPopsize/3) {
		//count = Math.min((int)count, (int)(population.size()/2))
		List<Order> toRemove = bestFirst.reverse().take(count)
		//println "${population.size()}- ${toRemove.size()}"
		population.removeAll(toRemove)
		assert population.size() > 1
	}

	void removeDups() {
		population = (population as Set) as List

	}

	void runEpoch() {

		spawn(initialSpawn)
		println "Best freq fitness:${ctx.freqIdealOrder.freqFitness}. "
		Date start0 = new Date()
		Date start = new Date()
		for (int i=0;i<maxGens;i++) {
			genNum++
			crossSome(crossAtOnce)
			if (population.size() > maxPopsize) {
				removeDups()
				if (population.size() > maxPopsize) {
					removeWeakest()
				}
			}
			
			spawn(spawnNew)
			
			if (i % 100 == 1) {

				
				Date stop = new Date()
				List<Order> b = bestFirst
				if (b[0].fitness > 0.99) break
				printStat(b, TimeCategory.minus( stop, start ).toString())
				start = stop
			}
		}
		println TimeCategory.minus( new Date(), start0 )
	}

	void printStat(List<Order> b ,String timeInfo) {
		
		String fr = "${b[0].fitness.round(3)}..${b[-1].fitness.round(3)}"
		println "$fr gen:($genNum) size:${b.size()} $timeInfo threads:${ForkJoinPool.commonPool().runningThreadCount}"

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
