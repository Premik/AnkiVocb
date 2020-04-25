package vocb.data

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.lang3.StringUtils

import groovy.transform.CompileStatic
import vocb.Helper
import static vocb.Helper.utf8


public class Manager {


	Path storagePath = Paths.get("/data/src/AnkiVocb/db/")

	@Lazy Path conceptsPath = {
		storagePath.resolve(conceptFilename)
	}()
	String conceptFilename = "concepts.yaml"

	@Lazy Path mediaPath = {
		storagePath.resolve("media")
	}()

	ConceptYamlStorage storage =new ConceptYamlStorage()
	ConceptDb db = new ConceptDb()
	Path dbPath

	Map<String, Concept> conceptByFirstTerm = [:]

	Map<String, Set<Concept>> conceptsByTerm = [:]

	void reindex() {
		conceptByFirstTerm = new HashMap<String, Concept>(db.concepts.size())
		conceptsByTerm = [:].withDefault {[]}
		db.concepts.each { Concept c->
			String ft = c.firstTerm
			if (conceptByFirstTerm.containsKey(ft)) {
				System.err.println("Warninig: duplicate word '$ft'")
			}
			if (ft) conceptByFirstTerm[ft] = c
			c.terms.values().each { Term t->
				conceptsByTerm[t.term] += t
			}
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


	public String termd2MediaLink(String mediaName, String mediaExt="", String[] groups=[]) {
		assert mediaName : "The media name is blank"
		if (mediaExt) mediaExt = ".$mediaExt" 
		"${Helper.word2Key(mediaName)}$mediaExt"
	}

	public String resolveMedia(String mediaName, String mediaExt, Closure whenNotFound) {
		String mediaLink = termd2MediaLink(mediaName, mediaExt)
		if (linkedMediaExists(mediaLink)) return mediaLink
		whenNotFound(mediaLinkPath(mediaLink))
		return mediaLink
	}

	public Path mediaLinkPath(String mediaLink) {
		mediaPath.resolve(mediaLink).toAbsolutePath()
	}

	public boolean linkedMediaExists(String mediaLink) {
		if (!mediaLink) return false
		Files.exists(mediaLinkPath(mediaLink))
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

	public Map<String, List<Concept>> groupByMedia(boolean stripExt=false, boolean includeImg=true) {
		Map<String, List<Concept>> ret = [:].withDefault {[]}
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
		Map<String, List<Concept>> grp = groupByMedia()
		grp.each { String mp, List<Concept> cs->
			if (!linkedMediaExists(mp)) {
				println "${mp} not found. $cs"
			}
		}
		println "Not used:"
		mediaPath.toFile().eachFile { File f->
			if ( !grp.containsKey(f.name)) {
				println "rm -f ${f}"
				
			}  
		}
		

	}



	public static void main(String[] args) {
		Manager dbMan = new Manager()
		dbMan.load()
		dbMan.findBrokenMedia()
		//dbMan.save()
		//println "${Helper.roundDecimal(dbMan.completeness*100, 0)}% completed"


		//println "Resaved "
	}


}
