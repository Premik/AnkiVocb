package vocb.data

import static vocb.Helper.utf8

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import vocb.Helper
import vocb.corp.WordNormalizer


public class Manager {


	Path storagePath = Paths.get("/data/src/AnkiVocb/db/")

	@Lazy Path conceptsPath = {
		storagePath.resolve(conceptFilename)
	}()
	String conceptFilename = "concepts.yaml"

	@Lazy Path mediaRootPath = {
		storagePath.resolve("media")
	}()

	ConceptYamlStorage storage =new ConceptYamlStorage()
	ConceptDb db = new ConceptDb()
	Path dbPath
	

	Map<String, Concept> conceptByFirstTerm = [:]

	Map<String, Set<Concept>> conceptsByTerm = [:]

	Map<Integer, Set<Concept>> conceptsByStar = [:]

	List<String> ignoreConcepts = []

	//BigDecimal[] freqRanges = [0, 11000, 151000, 1511000, 1121000, 2811000, new BigDecimal("10e10")]
	BigDecimal[] freqRanges = [0, 16, 100, 250, 500, 1800, new BigDecimal("10e10")]
	.collect{it*1000}

	Integer numberOfStarts(BigDecimal freq) {
		if (!freq) return null
		freqRanges.findIndexOf { freq < it} -1
	}

	void reindex() {
		conceptByFirstTerm = new HashMap<String, Concept>(db.concepts.size())
		conceptsByTerm = [:].withDefault {[]}
		conceptsByStar = [:].withDefault {[]}
		ignoreConcepts.clear()
		db.concepts.each { Concept c->
			String ft = c.firstTerm
			if (conceptByFirstTerm.containsKey(ft)) {
				System.err.println("Warninig: duplicate word '$ft'")
			}
			if (ft) conceptByFirstTerm[ft] = c
			c.terms.values().each { Term t->
				conceptsByTerm[t.term] += t
			}
			conceptsByStar[numberOfStarts(c.freq)]+= c
			if (c.state == 'ignore') ignoreConcepts.add(c)
		}
	}

	void withTerms(boolean includeExamples=false, Closure cl) {
		db.concepts.each { Concept c->
			if (c.state != "ignore") {
				c.terms.collect { String key, Term t->
					cl(c, t)
				}
				if (includeExamples) {
					c.examples.collect { String key, Term t->
						cl(c, t)
					}
				}
			}
		}
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
		if (Files.exists(mediaPath)) return mediaPath
		mediaPath.parent.toFile().mkdirs()
		whenNotFound(mediaPath)
		return mediaRootPath.relativize(mediaPath)
	}

	public Path mediaLinkPath(String mediaLink, String group="") {
		mediaRootPath.resolve(group).resolve(mediaLink).toAbsolutePath()
	}

	public boolean linkedMediaExists(String mediaLink, String group="") {
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

	public BigDecimal getCompleteness() {
		if (db.concepts.size() ==0) return 0
		db.concepts.sum {it.completeness} / db.concepts.size()
	}

	public Map<String, Set<Concept>> groupByMedia(boolean stripExt=false, boolean includeImg=true) {
		Map<String, List<Concept>> ret = [:].withDefault {[] as LinkedHashSet}
		db.concepts.each { Concept c->
			if (c.img && includeImg) {
				ret[c.img]+= c
				if (stripExt) {
					ret[Helper.stripExt(c.img)] +=c
				}
			}


			//ret[Filena c.img]+= c
			(c.terms.values() + c.examples.values()).each { Term t->
				if (t.tts) {
					ret[t.tts]+= c
					if (stripExt) { ret[Helper.stripExt(t.tts)] +=c}
				}
			}
		}

		return ret
	}

	public void findBrokenMedia() {

		Map<String, Set<Concept>> grp = groupByMedia()

		println "${'-'*80}"
		println "Missing:"
		grp.each { String mp, Set<Concept> cs->
			if (!linkedMediaExists(mp)) {
				println "${mp} $cs"
			}
		}
		println "${'-'*80}"
		println "Not used:"
		mediaRootPath.toFile().eachFile { File f->
			if ( !grp.containsKey(f.name) && f.isFile()) {
				println "rm -f '${f}'"

			}
		}
		println "${'-'*80}"
		println "Clashes"
		grp.findAll{it.value.size() > 1} each {String ml, Set<Concept> cs->
			List<Term> termsWithTts = cs.collectMany {it.terms.values().findAll {it.tts == ml} }
			if (termsWithTts.any {it.lang == 'cs'} && termsWithTts.any {it.lang == 'en'}  ) {
				println "${cs.collect {it.firstTerm} }  $ml: ${termsWithTts}. $cs"
			}
		}
		println "${'-'*80}"
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
			(c.termsByLang(lang) + c.examplesByLang(lang)).collect {it.term}
		}
	}



	void printStats() {
		int accu=0
		(5..0).each {
			int sz = conceptsByStar[it].size()
			accu+=sz
			println "${sz.toString().padRight(5)} ${('🟊'*it).padRight(10)} $accu"
		}
	}

	Collection<String> filterByStars(Collection<String> src, Range starRange = (0..2)) {
		src.findAll { conceptsByStar[it] in starRange }
	}

	public void moveToSubFolders() {
		
		WordNormalizer wn =new WordNormalizer()
		groupByMedia().take(1000).each { String mp, Set<Concept> cs->			
			Concept c = cs[0]
			if (c.img == mp && !mp.contains("img/")) {								
				c.img = "img/$mp"
				println "$mp -> $c.img"
				Files.move(mediaLinkPath(mp) , mediaLinkPath(c.img))
			}
			
			String pp = "en-terms/"
			c.termsByLang("en")
			  .findAll{it.tts == mp }
			  .findAll{!it.tts.contains(pp) }
			  .each {
				it.tts = "$pp$mp"
				println "$mp -> $it.tts"
				Files.move(mediaLinkPath(mp) , mediaLinkPath(it.tts))
			}
			
			
		}
		save()
	}


	public static void main(String[] args) {
		new Manager().tap {
			load()
			findBrokenMedia()
			//printStats()
			moveToSubFolders()
			//println allTextWithLang("en")
		}


		//dbMan.save()
		//println "${Helper.roundDecimal(dbMan.completeness*100, 0)}% completed"


		//println "Resaved "
	}


}
