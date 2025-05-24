package vocb.data

import static vocb.Helper.utf8

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic
import vocb.Helper
import vocb.corp.WordNormalizer
import static vocb.Ansi.*

@CompileStatic
public class Manager {


	Path storagePath = Paths.get("/data/src/AnkiVocb/db/")
	WordNormalizer wn =new WordNormalizer()

	Path getConceptsPath()  {
		storagePath.resolve(conceptFilename.toString())
	}

	CharSequence conceptFilename = "concepts.yaml"


	@Lazy Path mediaRootPath = {
		storagePath.resolve("media")
	}()

	ConceptYamlStorage storage =new ConceptYamlStorage()
	ConceptDb db = new ConceptDb()
	Path dbPath


	Map<String, Concept> conceptByFirstTerm = [:]

	Map<String, Set<Concept>> conceptsByTerm = [:]

	Map<Integer, Set<Concept>> conceptsByStar = [:]

	Map<String, Set<Concept>> conceptsByOrigin = [:]

	Map<String, Set<Concept>> conceptsByEnWordsInSample = [:]

	Map<String, Set<Example>> examplesByFirstTermWords = [:]


	List<Concept> ignoreConcepts = []

	//BigDecimal[] freqRanges = [0, 11000, 151000, 1511000, 1121000, 2811000, new BigDecimal("10e10")]
	static BigDecimal[] freqRanges = [0, 16, 100, 250, 500, 1800, 1000*1000]
	.collect{it*1000 as BigDecimal} as BigDecimal[]

	public static Integer numberOfStarsFreq(BigDecimal freq) {
		if (!freq) return null
		freqRanges.findIndexOf { freq < it} -1
	}

	public static Integer numberOfStars(Concept c) {
		numberOfStarsFreq(c?.freq)
	}

	public static String starsOf(Concept c, boolean pad=true) {
		String s = '🟊'*numberOfStars(c)
		if (pad) return s.padRight(10, '  ')
		return pad
	}
	
	public Concept findConceptByFirstTermAnyVariant(String firstTerm) {
		wn.wordVariants(firstTerm).collect {conceptByFirstTerm[it] }.find()		
	}

	void reindex() {
		conceptByFirstTerm = new HashMap<String, Concept>(db.concepts.size())
		conceptsByTerm = [:].withDefault {[] as LinkedHashSet}
		conceptsByStar = [:].withDefault {[] as LinkedHashSet}
		conceptsByOrigin = [:].withDefault {[] as LinkedHashSet}
		examplesByFirstTermWords = [:].withDefault {[] as LinkedHashSet}


		ignoreConcepts.clear()
		db.concepts.each { Concept c->
			String ft = c.firstTerm
			if (conceptByFirstTerm.containsKey(ft)) {
				System.err.println("Warninig: duplicate word '$ft'")
			}
			if (ft) conceptByFirstTerm[ft] = c
			c.terms.each { Term t->
				conceptsByTerm[t.term].add(c)
			}
			conceptsByStar[numberOfStarsFreq(c.freq)].add(c)
			if (c.state == 'ignore') ignoreConcepts.add(c)
			c.origins?.each {String o->
				conceptsByOrigin[o].add(c)
			}
		}

		db.examples.each { Example e->
			wn.tokens(e.firstTerm).each {
				examplesByFirstTermWords[it].add(e)
			}
		}

		conceptsByEnWordsInSample = conceptsByWordsInSample()

	}

	void withTerms(boolean includeExamples=false, Closure cl) {
		db.concepts.each { Concept c->
			if (c.state != "ignore") {
				c.terms.collect { Term t->
					cl(c, t)
				}

			}
		}
		assert !includeExamples : "Not implemented"
		/*if (includeExamples) {
		 db.examples.each { Example e->
		 cl(e, t)
		 }
		 }*/
	}

	void withTermsByLang(String lang, boolean includeExamples=false, Closure cl) {
		withTerms(includeExamples) { Concept c, Term t->
			if (t.lang == lang) cl(c , t)
		}
	}

	public void load() {
		conceptsPath.withReader(utf8) { Reader r->
			db =storage.parseDb(r)
		}
		assert db.version == "0.0.1" : "Not compatible db version"
		reindex()
	}


	public String termd2MediaLink(String mediaName, String mediaExt="") {
		assert mediaName : "The media name is blank"
		if (mediaExt) mediaExt = ".$mediaExt"
		"${Helper.word2Key(mediaName)}$mediaExt"
	}

	public String resolveMedia(String term, String mediaExt, String group="", Closure whenNotFound) {
		String mediaLink = termd2MediaLink(term, mediaExt)
		Path mediaPath = mediaLinkPath(mediaLink, group)
		if (Files.exists(mediaPath)) return mediaRootPath.relativize(mediaPath)
		mediaPath.parent.toFile().mkdirs()
		whenNotFound(mediaPath)
		return mediaRootPath.relativize(mediaPath)
	}

	public Path mediaLinkPath(CharSequence mediaLink, CharSequence group="") {
		mediaRootPath.resolve(group.toString()).resolve(mediaLink.toString()).toAbsolutePath()
	}

	public boolean linkedMediaExists(CharSequence mediaLink, CharSequence group="") {
		if (!mediaLink) return false
		Files.exists(mediaLinkPath(mediaLink, group))
	}

	public String save(Path path= conceptsPath) {
		reindex()
		assert path : "Not opened"
		String yaml = storage.dbToYaml(db)
		path.write(yaml)
		println "Saved $path"
		return yaml
	}

	public List<String> getWarnings() {
		db.concepts.collectMany {it.validate()}
		assert false: "Add examples warning too"
	}

	public Map<CharSequence, Set<Concept>> groupConceptsByMedia(boolean stripExt=false, boolean includeImg=true) {
		Map<CharSequence, Set<Concept>> ret = [:].withDefault {[] as LinkedHashSet}
		db.concepts.each { Concept c->
			if (c.img && includeImg) {
				ret[c.img].add(c)
				if (stripExt) {
					ret[Helper.stripExt(c.img)].add(c)
				}
			}


			//ret[Filena c.img]+= c
			c.terms.each { Term t->
				if (t.tts) {
					ret[t.tts].add(c)
					if (stripExt) { ret[Helper.stripExt(t.tts)].add(c)}
				}
			}
		}
		return ret
	}

	public Map<CharSequence, Set<Term>> groupCTermsByMedia(boolean stripExt=false) {
		Map<CharSequence, Set<Term>> ret = [:].withDefault {[] as LinkedHashSet}
		List<Term> allTerms = db.concepts.collectMany {it.terms} + db.examples.collectMany {it.terms}
		allTerms.each { Term t->
			if (t.tts) {
				ret[t.tts].add(t)
				if (stripExt) { ret[Helper.stripExt(t.tts)].add(t)}
			}
		}
		return ret
	}



	Map<String, Set<Concept>> conceptsByWordsInSample(String lang="en") {
		Map<String, Set<Concept>> ret = [:].withDefault {[] as LinkedHashSet}


		db.examplesByLang("en").each {Term t ->
			wn.uniqueueTokens(t.term).each { String word->
				assert lang == "en" : "not implemented"
				Concept c=  conceptByFirstTerm[word]
				if (!c) {
					if (word.endsWith("s")) c = conceptByFirstTerm[word[0..-2]]
					else c=  conceptByFirstTerm["${word}s"]
				}
				if (!c) {
					println "${color('Unknown term', YELLOW)} ${color(word, BOLD)} used in the ${color(t.term, BLUE)} example."
				}
				ret[word].add(c)
			}
		}
		return ret
	}



	public void validate() {

		db.validate().each {println it}
		Map<CharSequence, Set<Concept>> cGrp = groupConceptsByMedia()
		Map<CharSequence, Set<Term>> tGrp = groupCTermsByMedia()

		println "${'-'*80}"
		println "Missing:"
		tGrp.each { CharSequence mp, Set<Term> t->
			if (!linkedMediaExists(mp)) {
				println "${mp} $t"
			}
		}
		println "${'-'*80}"
		//println tGrp
		
		println "Not used "
		
		mediaRootPath.toFile().eachFileRecurse { File f->

			String s = "${f.parentFile.name}/${f.name}".toString()
			
			
					
			if ( !tGrp.containsKey(s) && !cGrp.containsKey(s)  && f.isFile()) {
				//println "${s}"
				println "rm -f '${f}'"
				//println tGrp.keySet()
			}
		}
		println "${'-'*80}"
		println "Clashes"
		cGrp.findAll{it.value.size() > 1}.each {CharSequence ml, Set<Concept> cs->
			List<Term> termsWithTts = cs.collectMany {it.terms.findAll {it.tts == ml} }
			if (termsWithTts.any {it.lang == 'cs'} && termsWithTts.any {it.lang == 'en'}  ) {
				println "${cs.collect {it.firstTerm} }  $ml: ${termsWithTts}. $cs"
			}
		}
		println "${'-'*80}"
		println "Example dups"
		Map<String, Example> ex = [:] 
		db.examples.each { Example e->
			String sen = wn.uniqueueTokens(e.firstTerm, true).join(" ")
			if (ex.containsKey(sen)) {
				println """\
                   Duplicate example ${color(sen, BLUE)} 
                      ${color(e.toString(), BOLD)}. 
                      ${color(ex[sen].toString(), BOLD)}.
                   """.stripIndent()
			}
			ex.put(sen, e)
		}
		
		/*println "Plurals:"
		 db.concepts
		 .collectMany { Concept c-> c.termsByLang("en")}
		 .findAll {Term t -> t.term.endsWith("s")}
		 .collect {Term t-> t.term.dropRight(1) }
		 .each { String singular ->
		 if (conceptByFirstTerm.containsKey(singular)) {
		 println singular
		 }
		 }*/

		println "${'-'*80}"
	}

	List<String> allTextWithLang(String lang="cs") {
		db.concepts.collectMany {Concept c ->
			c.termsByLang(lang)*.term
		} +
		db.examples.collectMany {Example e ->
			e.byLang(lang)*.term
		}
	}



	void printStats() {
		int accu=0
		(5..0).each {
			int sz = conceptsByStar[it].size()
			accu+=sz
			println "${sz.toString().padRight(5)} ${starsOf(conceptsByStar[it][0])} $accu"
		}
	}

	Collection<String> filterByStars(Collection<String> src, List<Integer> starRange = (0..2)) {
		src.findAll { conceptsByStar[it] in starRange }
	}

	List<Concept> conceptsFromWordList(Collection<String> enWords) {
		enWords
				.findAll()
				.collect {it.toLowerCase()}
				.collect { conceptByFirstTerm[it] }
				.findAll()
				.findAll {it.state != 'ignore'}
	}

	List<Concept> conceptsFromWordsInSentence(CharSequence sen) {
		Set<String> words = wn.uniqueueTokens(wn.normalizeSentence(sen))
		return conceptsFromWordList(words)
	}


	List<Concept> conceptsFromWordsInExample(Example e) {
		conceptsFromWordsInSentence(e.firstTerm)
	}
	
	public Example findBestExampleForSentence(String sentence) {
		Set<String> words = wn.uniqueueTokens(sentence)
		Set<Example> cand =  words.collectMany {String w->
			examplesByFirstTermWords[w]
		} as LinkedHashSet
		return cand.max { Example e->
			//Example covering the most words in the give sentence
			wn.commonWordOf(e.firstTerm, sentence).size()			
		}		
	}
	
	public void withBestExample(String text, Closure cl) {
		List<String> snts = wn.sentences(text)
		assert snts
		snts.each { String sen->
			Example e = findBestExampleForSentence(sen)			
			String et = e?.firstTerm
			//assert et : "No example for '$sen' \n ${snts.take(5)} ..."
			if (wn.normalizeSentence(et) == wn.normalizeSentence(sen)) {
				cl(e, sen, [] as Set, [] as Set)
			} else {
				Set<String> com = wn.commonWordOf(sen, et)
				Set<String> mis = wn.uniqueueTokens(sen) + wn.uniqueueTokens(et) - com
				cl(e, sen, com, mis)
			}			
		}
		
	}


	public void moveToSubFolders() {


		groupConceptsByMedia().each { CharSequence mp, Set<Concept> cs->
			Concept c = cs[0]
			if (c.img == mp && !mp.contains("img/")) {
				c.img = "img/$mp"
				println "$mp -> $c.img"
				Files.move(mediaLinkPath(mp) , mediaLinkPath(c.img))
			}

			String pp = "cs-samples/"
			/*c.examplesByLang("cs")
			 .findAll{it.tts == mp }
			 .findAll{!it.tts.contains(pp) }
			 .each {
			 it.tts = "$pp$mp"
			 println "$mp -> $it.tts"
			 if (linkedMediaExists(mp)) {
			 Files.move(mediaLinkPath(mp) , mediaLinkPath(it.tts))
			 }
			 }*/


		}
		save()
	}



	public static void main(String[] args) {
		new Manager().tap {
			load()
			validate()
			printStats()
			//allTextWithLang("en").each {println it}
			

			//moveToSubFolders()
			//println allTextWithLang("en")
			//moveSamples()
			save()

			println "Resaved "
		}



		//println "${Helper.roundDecimal(dbMan.completeness*100, 0)}% completed"



	}


}
