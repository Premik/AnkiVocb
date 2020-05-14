package vocb.anki.crowd

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class VocbModel {

	String version = "ankivocb1"
	boolean ignoreMissingMedia = false
	URL crowdJsonUrl = getClass().getResource('/template/deck.json')
	Path destCrowdRootFolder
	Closure<Path> resolveMediaLink

	@Lazy CrowdParser parser = new CrowdParser(json:crowdJsonUrl.text)

	@Lazy NoteModel noteModel = {
		NoteModel n = parser.ankivocbModel
		assert n : "Failed to find the ankivobc model. The model name must start with 'ankivocb' "
		return n
	}()

	@Lazy List<Note> notes = {
		parser.allNotes.each {assureNote(it)}
		return parser.allNotes
	}()

	void assureNote(Note n) {
		assert n
		n.assertIsComplete()
		n.tags.remove("ankivocb1") //Legacy tag
		/*if (!n.hasTagWithPrefix(version)) {
		 n.tags.add(version)
		 }*/
		n.guid = "avcb_${n.foreign?:n.hashCode() }"
	}

	void syncNoteModels() {

		noteModel.assureIsComplete()
		parser.noteModels = [noteModel]
	}
	
	void syncNoteFields() {
		//println Helper.objectToJson(notes)
		notes.each {assureNote(it)}
		parser.jsonRoot.notes = notes
	}

	void assureCrowdDest() {
		assert destCrowdRootFolder
		assert Files.isDirectory(destCrowdRootFolder)
		Path corwdMediaPath = destCrowdRootFolder.resolve( "media")
		corwdMediaPath.toFile().mkdirs()
	}

	void syncMedia() {
		copyMediaLinks(
				notes.collectMany { Note n->
					[
						n.img,
						n.foreignTTS,
						n.foreignExampleTTS,
						n.nativeTTS,
						n.nativeAltTTS,
						n.nativeExampleTTS
					].findAll()
				})
	}

	void copyMediaLinks(List<String> links) {
		assureCrowdDest()
		Path corwdMediaPath = destCrowdRootFolder.resolve( "media")
		assert resolveMediaLink : "Set a closure to resolve the mediaLink to file path"
		links.each { String mediaLink->
			Path crowdPath = corwdMediaPath.resolve(mediaLink)
			if (!Files.exists(crowdPath)) {
				println "$mediaLink: copy"
				Path sourcePath =resolveMediaLink(mediaLink)
				assert ignoreMissingMedia ||  Files.exists(sourcePath)
				if (Files.exists(sourcePath)) {
					Files.copy(sourcePath, crowdPath)
				}

			}
		}
	}

	void save() {
		assureCrowdDest()
		syncNoteFields()
		syncNoteModels()
		syncMedia()
		Path deckJson = destCrowdRootFolder.resolve("${destCrowdRootFolder.fileName}.json")
		parser.saveTo(deckJson.toFile())
	}

	Note updateNoteHaving(String foreignTerm) {
		assert foreignTerm
		Note n = notes.find {it.foreign == foreignTerm }
		if (!n) {
			n = new Note(model:noteModel)
			notes.add(n)
			syncNoteFields()
			//syncMedia(Paths.get( "/tmp/work"))
		}
		return n

	}

	static void main(String... args) {
		new VocbModel(
				destCrowdRootFolder: Paths.get("/tmp/work/test"),
				ignoreMissingMedia : true,
				resolveMediaLink: {String mediaLink ->
					Paths.get("/data/src/AnkiVocb/db/media").resolve(mediaLink)
				}).tap {
					updateNoteHaving("newWord").tap {
						img = "newWordImg"
					}
					save()
				}
		println "done"

	}

}
