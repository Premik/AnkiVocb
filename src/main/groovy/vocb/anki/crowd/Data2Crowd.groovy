package vocb.anki.crowd

import java.nio.file.Path
import java.nio.file.Paths

import javax.naming.LimitExceededException

import groovy.transform.Memoized
import vocb.Helper
import vocb.corp.Similarity
import vocb.data.Concept
import vocb.data.Manager
import vocb.data.Term

public class Data2Crowd {

	Path dataPath= Paths.get("/data/src/AnkiVocb/db/")
	String destCrowdRootFolder = "/tmp/work/test"
	Similarity sim = new Similarity()

	BigDecimal[] freqRanges = [
		0,
		11000,
		151000,
		1511000,
		2121000,
		2811000,
		new BigDecimal("10e10")
	]

	Integer numberOfStarts(BigDecimal freq) {
		if (!freq) return null
		freqRanges.findIndexOf { freq < it} -1
	}


	@Lazy Manager dbMan =  {
		assert dataPath
		new Manager(storagePath: dataPath).tap {
			load()
		}
	}()

	@Lazy VocbModel vocbModel = {
		assert destCrowdRootFolder
		new VocbModel(
				destCrowdRootFolder: Paths.get(destCrowdRootFolder),
				resolveMediaLink: {String mediaLink ->
					Paths.get("/data/src/AnkiVocb/db/media").resolve(mediaLink)
				})
	}()

	void concept2CrowdNote(Concept c, Note n) {
		assert c?.firstTerm
		assert n
		int stars = numberOfStarts(c?.freq)
		n.with {
			Term ent = c.terms.values()[0]
			Term cst1 = c.terms.values()[1]
			Term cst2 = c.terms.values()[2]
			Term enx = c.examples.values()[0]
			Term csx = c.examples.values()[1]

			img= c?.img
			freq= stars
			foreign= ent.term
			foreignTTS= ent.tts
			foreignExample= enx?.term
			foreignExampleTTS= enx?.tts
			n.'native' = cst1?.term
			nativeTTS= cst1?.tts
			nativeAlt= cst2?.term
			nativeAltTTS= cst2?.tts
			nativeExample= csx?.term
			nativeExampleTTS = csx?.tts
			(1..5).each {tags.remove('🟊'*it)}
			if (stars >0) {
				tags.add('🟊'*stars)
			}
		}
	}


	void mapConcept(Concept c) {
		if (c.state == "ignore") return
			assert c?.firstTerm
		println c
		Note n = vocbModel.updateNoteHaving(c.firstTerm)
		concept2CrowdNote(c, n)
	}

	@Memoized
	BigDecimal similarConcepts(Concept a, Concept b) {
		assert a?.terms
		assert b?.terms
		int sz = Math.min( a.terms.size(), b.terms.size())
		BigDecimal ret = 0
		for (int i=0;i<sz;i++) {
			ret += sim.similarSubstrings(a.terms.values()[i].term, b.terms.values()[i].term)
			if (i == 0) ret*3 //First term is more important
		}
		return ret
	}

	BigDecimal topSimilarOf(Concept a, BigDecimal scoreCur=8*3) {
	}

	Concept[] optimizeOrder() {
		

		Random random = new Random()
		Concept[] prev = new ArrayList(dbMan.db.concepts)
		Concept[] ret = new ArrayList(dbMan.db.concepts)
		BigDecimal lastScore =0
		
		def mixIt = {
			(0..50).each {ret.swap(random.nextInt(ret.size()), random.nextInt(ret.size()))}
		}
		def isBetter = {
		}
		def scoreOf = { Concept[] cps ->
			BigDecimal penalty =0			
			Helper.withAllPairs(cps as List, 20) { Integer i, Concept c1,Concept c2->			
					//Similar and close => bigger  score
					penalty+= similarConcepts(c1, c2) * Helper.lerp(5, 1, i/30)   			
			}
			return penalty
		}
		
		lastScore = scoreOf(ret) 
		println lastScore 		
		for (int i=0;i<1000;i++) {
		mixIt()
		BigDecimal newScore = scoreOf(ret) 
		println newScore
		if (newScore < lastScore)
			newScore = lastScore
			prev = new ArrayList(ret as List)
			prev.take(40).collect {it.firstTerm} 
		}
		return prev
	}


	void exportToCrowd(int limit=10, int mult=1) {
		vocbModel.notes.clear()
		(0..limit).collect{it*mult} each {
			mapConcept(dbMan.db.concepts[it])
		}
		vocbModel.save()
	}


	public static void main(String[] args) {
		Data2Crowd a = new Data2Crowd()
		a.exportToCrowd(10, 1)
		//println a.dbMan.db.concepts.take(40).collect {it.firstTerm}
		//println a.optimizeOrder().take(40).collect {it.firstTerm}

		//println a.similarConcepts(a.dbMan.db.concepts[0], a.dbMan.db.concepts[1])



	}
}
