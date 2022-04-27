package vocb.data

import static vocb.Ansi.*
import static vocb.Helper.utf8

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

import groovy.transform.CompileStatic
import vocb.Helper
import vocb.corp.WordNormalizer

@CompileStatic
public class Manager {


	Path defaultStoragePath = Paths.get("/data/src/AnkiVocb/db/")
	WordNormalizer wn =WordNormalizer.instance

	@Lazy
	DataLocation defaultConceptsLocation = new DataLocation(storageRootPath:defaultStoragePath, filename:"concepts.yaml")
	@Lazy
	DataLocation defaultExamplesLocation = new DataLocation(storageRootPath:defaultStoragePath, filename:"examples.yaml")


	@Lazy Path mediaRootPath = {
		defaultStoragePath.resolve("media")
	}()

	ConceptYamlStorage storage =new ConceptYamlStorage()
	ConceptDb db = new ConceptDb()



	Map<String, Concept> conceptByFirstTerm = [:]

	Map<String, Set<Concept>> conceptsByTerm = [:]

	Map<Integer, Set<Concept>> conceptsByStar = [:]

	Map<String, Set<Concept>> conceptsByEnWordsInSample = [:]

	Map<String, Set<Example>> examplesByFirstTermWords = [:]

	//Map<String, Concept> multiWordconceptByFirstTerm = [:]

	List<Concept> ignoreConcepts = []

	//BigDecimal[] freqRanges = [0, 11000, 151000, 1511000, 1121000, 2811000, new BigDecimal("10e10")]
	static BigDecimal[] freqRanges = [
		0,
		49,
		116,
		255,
		540,
		1407,
		1000*1000*1000
	]
	.collect{it as BigDecimal} as BigDecimal[]

	public static Integer numberOfStarsFreq(BigDecimal freq) {
		if (!freq) return null
		freqRanges.findIndexOf { freq < it} -1
	}

	public static Integer numberOfStars(Concept c) {
		if (c.ignore) return null
		numberOfStarsFreq(c?.freq)
	}

	public static String starsOf(Concept c, boolean pad=true) { 
		String s = '🟊'*(numberOfStars(c)?:0)
		if (pad) return s.padRight(10, '  ')
		return pad
	}

	public Concept findConceptByFirstTermAnyVariant(String firstTerm) {
		findConceptsByFirstTermAllVariant(firstTerm)[0]
	}

	public Collection<Concept> findConceptsByFirstTermAllVariant(String firstTerm) {
		/*if (firstTerm == "i") {
			println "I"
		}*/
		List<String> variants = wn.wordVariantsWithBrackets(firstTerm)
		Collection<Concept> sourceVars = variants.findResults {conceptByFirstTerm[it] }
		if (sourceVars) return sourceVars
		db.concepts.parallelStream().map{ Concept c-> 			
			List<String> vars = wn.wordVariantsWithBrackets(c.firstTerm)
			new Tuple2<Concept, List<String >>(c, vars)
		}.filter { Tuple2<Concept, List<String >> t->
			t.v2.contains(firstTerm)
		}.map { Tuple2<Concept, List<String >> t->
			t.v1
		}.toList()
				
	}



	void reindex() {
		conceptByFirstTerm = new HashMap<String, Concept>(db.concepts.size())
		conceptsByTerm = [:].withDefault {[] as LinkedHashSet}
		conceptsByStar = [:].withDefault {[] as LinkedHashSet}
		examplesByFirstTermWords = [:].withDefault {[] as LinkedHashSet}
		//multiWordconceptByFirstTerm = [:] as HashMap


		ignoreConcepts.clear()
		db.concepts.each { Concept c->
			String ft = wn.stripBracketsOut(c.firstTerm)
			if (conceptByFirstTerm.containsKey(ft)) {
				System.err.println("Warninig: duplicate word '$ft'")
			}
			if (ft) conceptByFirstTerm[ft] = c
			c.terms.each { Term t->
				conceptsByTerm[t.term].add(c)
			}
			conceptsByStar[numberOfStars(c)].add(c)
			if (c.ignore) ignoreConcepts.add(c)
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
			if (!c.ignore) {
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

	Stream<String> termsStream(String lang="en") {
		db.concepts.stream().flatMap{ Concept c ->
			c.termsByLang(lang).collect {it.term}.stream()
		}
	}


	public void load() {
		load(defaultConceptsLocation, true)
		load(defaultExamplesLocation, true)
		reindex()
	}

	public ConceptDb load(DataLocation loc, boolean merge=true) {
		assert loc?.storageRootPath
		assert loc.storagePath
		if (!loc.exists()) {
			println "Ignoring non-existing source $loc"
			return
		}
		assert  !(db.dataLocations.contains(loc)) : "$loc already loaded"
		ConceptDb cdb
		loc.storagePath.withReader(utf8) { Reader r->
			cdb =storage.parseDb(r)
			assert cdb.version == "0.0.1" : "Not compatible db version"
		}
		cdb.assignDataLocationToAll(loc)
		if (merge) {
			db.mergeWith(cdb)
			reindex()
		}
		return cdb
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

	public void save(boolean forceSaveAll=true) {
		reindex()

		assert db.dataLocations
		db.dataLocations
				.findAll { DataLocation dl-> dl.dirty || forceSaveAll }
				.each { DataLocation dl->
					save(dl)
				}
	}

	public String save(DataLocation loc) {
		assert loc
		String yaml = storage.dbToYaml(db) {TermContainer t->
			t.location === loc
		}
		loc.storagePath.write(yaml)
		println "Saved $loc"
	}

	public List<String> getWarnings(ValidationProfile vp) {
		db.concepts.collectMany {it.validate(vp)}
		assert false: "Add examples warning too"
	}

	public Map<CharSequence, Set<Concept>> groupConceptsByMedia(boolean stripExt=false, boolean includeImg=true, boolean stripPrefix=false) {
		Map<CharSequence, Set<Concept>> ret = [:].withDefault {[] as LinkedHashSet}
		db.concepts.each { Concept c->
			if (c.img && includeImg) {
				if (stripPrefix) {
					String strp = c.img.takeAfter("/")
					if (strp) {
						ret[strp].add(c)
						if (stripExt) {
							ret[Helper.stripExt(strp)].add(c)
						}
					}
				}
				ret[c.img].add(c)
				if (stripExt) {
					ret[Helper.stripExt(c.img)].add(c)
				}
			}


			//ret[Filena c.img]+= c
			c.terms.each { Term t->
				if (t.tts) {
					ret[t.tts].add(c)
					if (stripPrefix) {
						String strp = t.tts.takeAfter("/")
						if (strp) {
							ret[strp].add(c)
							if (stripExt) {
								ret[Helper.stripExt(strp)].add(c)
							}
						}
					}

					if (stripExt) {
						ret[Helper.stripExt(t.tts)].add(c)
					}
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
				if (stripExt) {
					ret[Helper.stripExt(t.tts)].add(t)
				}
			}
		}
		return ret
	}



	Map<String, Set<Concept>> conceptsByWordsInSample(String lang="en") {
		Map<String, Set<Concept>> ret = [:].withDefault {[] as LinkedHashSet}


		db.examplesByLang("en").each {Term t ->
			wn.uniqueueTokens(t.term).each { String word->
				assert lang == "en" : "not implemented"
				Concept c = findConceptByFirstTermAnyVariant(word)

				if (!c && !word.contains("'")) {
					//if (!c) {
					println "${color('Unknown term', YELLOW)} ${color(word, BOLD)} used in the ${color(t.term, BLUE)} example."
				}
				ret[word].add(c)
			}
		}
		return ret
	}





	public void validate(ValidationProfile vp=ValidationProfile.currentDefaultProfile) {

		db.validate(vp).each {println it}
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



		//println groupConceptsByMedia(false, true, true)["ocean.mp3"]
		groupConceptsByMedia(false, true, true).each {CharSequence mediaLink, Set<Concept> cs->
			List<Term> termsWithTts = cs.collectMany {
				it.terms.findAll {
					//assert it?.tts
					if (!it.tts) return false
					it.tts == mediaLink || it.tts.takeAfter("/") == mediaLink.takeAfter("/")
				}
			}

			if (termsWithTts.any {it.lang == 'cs'} && termsWithTts.any {it.lang == 'en'}  ) {
				println "${cs.collect {it.firstTerm} }  $mediaLink: ${termsWithTts}. $cs"
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
		println  "Concepts: ${db.concepts.size()} Unique: ${conceptByFirstTerm.size()}"
	}

	List<String> allTextWithLang(String lang="cs") {
		db.concepts.collectMany {Concept c ->
			c.termsByLang(lang)*.term
		} +
		db.examples.collectMany {Example e ->
			e.termsByLang(lang)*.term
		}
	}



	void printStats() {
		int accu=0
		(5..0).each {
			int sz = conceptsByStar[it].size()
			if (it == 0) sz+=conceptsByStar[null].size()
			accu+=sz
			String samples = conceptsByStar[it].take(40).collect {it.firstTerm}
			println "${sz.toString().padRight(5)} (${accu.toString().padRight(5)}) ${starsOf(conceptsByStar[it][0])} $samples "
		}
	}

	Collection<String> filterByStars(Collection<String> src, List<Integer> starRange = (0..2)) {
		src.findAll { conceptsByStar[it] in starRange }
	}

	List<Concept> conceptsFromWordList(Collection<String> enWords) {
		enWords
				.findAll()
				.collect {it.toLowerCase()}
				.collect {findConceptByFirstTermAnyVariant(it)}
				.findAll()
				.findAll {!it.ignore}
	}

	List<Concept> conceptsFromWordsInSentence(CharSequence sen) {
		Set<String> words = wn.uniqueueTokens(wn.normalizeSentence(sen))
		return conceptsFromWordList(words)
	}


	List<Concept> conceptsFromWordsInExample(Example e) {
		conceptsFromWordsInSentence(e.firstTerm)
	}

	@Deprecated
	public Example findBestExampleForSentence(String sentence) {
		Set<String> words = wn.uniqueueTokens(sentence)
		Set<Example> cand =  words.collectMany {String w->
			examplesByFirstTermWords[w]
		} as LinkedHashSet
		return cand.max { Example e->
			//Example covering the most words in the give sentence
			//prefer shorter examples
			wn.commonWordOf(e.firstTerm, sentence).size()*100 - e.firstTerm.length()
		}
	}
	
	
	public List<ExampleComparatorMatch> bestExampleForSentence(String sentence) {
		ExampleComparator.of(sentence).bestFromExamples(db.examples.stream())
	}
	
	public List<ExampleComparatorMatch> bestExampleForSentence(Collection<String> words) {		
		new ExampleComparator(words:words).bestFromExamples(db.examples.stream())
	}

	@Deprecated
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

	void tuneStars() {
		db.concepts.findAll {!it.ignore}.sort {-1*it.freq}
		.drop(650)
		.take(400).each {
			println it
		}
		printStats()
	}



	public static void main(String[] args) {

		new Manager().tap {
			load()

			validate()
			printStats()
			//allTextWithLang("cs").each {println it}


			//moveToSubFolders()
			//println allTextWithLang("en")
			//moveSamples()
			save()

			println "Resaved "
		}



		//println "${Helper.roundDecimal(dbMan.completeness*100, 0)}% completed"
	}
}
