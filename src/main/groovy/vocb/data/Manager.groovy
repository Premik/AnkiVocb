package vocb.data

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

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

	void reindex() {
		conceptByFirstTerm = new HashMap<String, Concept>(db.concepts.size())
		db.concepts.each { Concept c->
			String ft = c.firstTerm
			if (conceptByFirstTerm.containsKey(ft)) {
				System.err.println("Warninig: duplicate word '$ft'")
			}			
			if (ft) conceptByFirstTerm[ft] = c
		}
	}

	void withTerms(Closure cl) {
		db.concepts.each { Concept c->
			if (c.state != "ignore") {
				c.terms.collect { 
					cl(c, it)
				}
			}
		}
	}
	
	void withTermsByLang(String lang, Closure cl) {
		withTerms { Concept c, Term t->
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
	
	
	public String termd2MediaLink(String mediaName, String mediaExt) {
		assert mediaName : "The media name is blank"
		"${Helper.word2Key(mediaName)}.$mediaExt"
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
}