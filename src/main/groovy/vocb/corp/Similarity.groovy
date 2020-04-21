package vocb.corp

import groovy.transform.Memoized


public class Similarity {

	//https://stackoverflow.com/questions/10433657/how-to-determine-character-similarity
	List<String> visualLettersSim= [
		'sbgae',
		'rp',
		'do',
		'bhd',
		'mw',
		'il',
		'vx'
	]

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

	BigDecimal similarWithLen(CharSequence a, CharSequence b, int len) {
		List<String> aSubs = allSubstringsWithLen(a, len)
		List<String> bSubs = allSubstringsWithLen(b, len)
		List<Integer> dst = distanceMatches(aSubs, bSubs, 1000) + distanceMatches(bSubs, aSubs, 1000)
		dst.collect{ (it+1) as BigDecimal}.collect {1/(it*it)}.collect {(it < 0.01) ? 0 : it }.sum()
	}

	@Memoized
	BigDecimal similarSubstrings(CharSequence a, CharSequence b) {
		assert a
		assert b
		int len = Math.min(a.length() ,b.length())
		(len..1).collect {similarWithLen(a, b, it)*it }.sum()
	}

	Set<String> commonSubstringsOf(CharSequence a, CharSequence b) {
		Set<String> aSet = allSubstringsOf(a)
		Set<String> bSet = allSubstringsOf(b)
		return aSet.intersect(bSet)
	}

	static void main(String... args) {
		Similarity n = new Similarity()
		Corpus c=  Corpus.buildDef()
		String word ="when"
		println n.similarSubstrings("you", "yours")
		String[] sorted = c.topX(10000).sort {String a, String b ->
			n.similarSubstrings(word, b) <=> n.similarSubstrings(word, a)
		}
		
		println word
		println ( sorted.take(20).collect{"$it ${n.similarSubstrings(word, it)} "})

		//bedroom


	}


}
