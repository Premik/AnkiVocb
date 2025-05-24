package vocb.corp

import groovy.transform.Memoized
import vocb.data.Concept
import vocb.data.Term


public class Similarity {

	//https://stackoverflow.com/questions/10433657/how-to-determine-character-similarity
	List<String> visualLettersSim= ['sbgae', 'rp', 'do', 'bhd', 'mw', 'il', 'vx']

	double sameLatter(char a, char b,  double exactScore=1, double visualScore=0.01) {
		if (a==b) return exactScore
	}

	List<String> allSubstringsWithLen(CharSequence s, int len=s.length()) {
		int count = s.length() - len +1
		if (count <=0) return []
		return (0..count-1).collect { s[it..it+len-1] }
	}

	List<String> allSubstringsOf(CharSequence s) {
		(s.length()..1).collect {allSubstringsWithLen(s, it)}.collectMany {it}
	}

	List<Integer> distanceMatches(List<String> aLst, List<String> bLst, int maxDist=-1) {
		aLst.withIndex().collect { String a, int ai ->
			int bi = bLst.indexOf(a)
			if (bi <0) return maxDist
			return Math.abs(bi-ai)
		}
	}

	double rateDistance(long distance) {
		// [err, 1.0, 0.44, 0.25, 0.16, 0.11, 0.08, 0.06, 0.05, 0.04, 0.03]
		assert distance >=0
		double d=distance+2
		return 4/(d*d)
	}

	double similarWithLen(List<String> aSubs, List<String> bSubs) {
		List<Integer> dst = distanceMatches(aSubs, bSubs, 1000)
		dst.withIndex().collect {Integer dist, int indx->
			if (indx==0) return 5*rateDistance(dist) //Match at the start makes words more similar
			if (indx==1) return 3*rateDistance(dist)
			return  rateDistance(dist)
		}.average()
	}

	double similarWithLen(CharSequence a, CharSequence b, int len) {
		List<String> aSubs = allSubstringsWithLen(a, len)
		List<String> bSubs = allSubstringsWithLen(b, len)
		return similarWithLen(aSubs, bSubs)
	}

	double similarWithLenSmart(CharSequence a, CharSequence b, int len) {
		double ret = similarWithLen(a,b,len)

		if (len == 2) {
			//Swapped letters in each pair
			List<String> swpA = allSubstringsWithLen(a, 2).collect {"${it[1]}${it[0]}".toString()}
			List<String> bSubs = allSubstringsWithLen(b, len)
			//ret = (2d*ret + similarWithLen(swpA, bSubs))/3d
			ret+= similarWithLen(swpA, bSubs)/5d //Similarty after swap has lower weight
		}
		return ret
	}


	double similarSubstrings(CharSequence a, CharSequence b) {
		if (!a || !b) return 0
		int len = Math.min(a.length() ,b.length())
		int lenDif = Math.abs(a.length() - b.length())
		double s = (len..1).collect {similarWithLenSmart(a, b, it)*it }.sum()
		return s+8*rateDistance(lenDif)
	}

	@Memoized
	double similar(CharSequence a, CharSequence b) {
		if (!a || !b) return 0
		double selfA = similarSubstrings(a,a)
		double selfB = similarSubstrings(b,b)
		double ab = similarSubstrings(a,b)/selfA
		double ba = similarSubstrings(b,a)/selfB
		

		//println "${selfA}($ab) ${selfB}( $ba) "
		return ((ab+ba)/2d).round(4)
	}

	double termSimilarity(Term t1, Term t2) {
		if (!t1 || !t2) return 0
		if (t1.lang != t2.lang) return 0
		[similar(t1.term, t2.term), 2*similar(t1.pron, t2.pron)].sum()
	}

	double conceptSimilarity(Concept c1, Concept c2) {

		double sum = [
			termSimilarity(c1.enTerm, c2.enTerm)*8,
			termSimilarity(c1.csTerm, c2.csTerm),
			termSimilarity(c1.csTerm, c2.csAltTerm),
			termSimilarity(c1.csAltTerm, c2.csAltTerm),
		].sum()
	}

	double conceptSimilarityNorm(Concept c1, Concept c2) {
		conceptSimilarity(c1, c2)/conceptSimilarity(c1, c1)

	}



	Set<String> commonSubstringsOf(CharSequence a, CharSequence b) {
		Set<String> aSet = allSubstringsOf(a)
		Set<String> bSet = allSubstringsOf(b)
		return aSet.intersect(bSet)
	}


	static void main(String... args) {
		Similarity n = new Similarity()
		Corpus c=  Corpus.buildDef()

		println "you yours   ${n.similar("you", "yours")}"
		println "where whenever  ${n.similar("when", "whenever")}"
		println "cake cook  ${n.similar("cake", "cook")}"
		println "now know  ${n.similar("now", "know")}"
		println "yesterday cat  ${n.similar("yesterday", "cat")}"



		String[] top = c.topX(16000)
		def prinSim = {String word->
			println top.sort {String a, String b ->
				n.similar(word, b) <=> n.similar(word, a)
			}.take(100).collect{"$it ${n.similar(word, it)}"}
		}

		prinSim("when")
		prinSim("what")
		prinSim("become")
		prinSim("what")
		prinSim("yesterday")
		prinSim("wet")
		prinSim("government")





		//bedroom


	}


}
