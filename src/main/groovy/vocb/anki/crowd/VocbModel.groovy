package vocb.anki.crowd

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic
import vocb.ConfHelper
import vocb.Helper

//@CompileStatic
class VocbModel {

    String version = "ankivocb1"
    boolean ignoreMissingMedia = false

    Path destCrowdFolder
    //Closure<Path> resolveMediaLink

    @Lazy
    CrowdParser parser = {
        InputStream deck = ConfHelper.instance.resolveRes("deck.json")
        assert deck != null
        new CrowdParser(json: deck.text)
    }()

    @Lazy
    NoteModel noteModel = {
        NoteModel n = parser.ankivocbModel
        assert n: "Failed to find the ankivobc model. The model name must start with 'ankivocb' "
        return n
    }()

    @Lazy
    List<Note> notes = {
        parser.allNotes.each { assureNote(it) }
        return parser.allNotes
    }()

    void assureNote(Note n) {
        assert n
        n.assertIsComplete()
        n.tags.remove("ankivocb1") //Legacy tag
        /*if (!n.hasTagWithPrefix(version)) {
         n.tags.add(version)
         }*/      
        n.guid = noteGuid(n)
    }
	
	static String noteGuid(Note n) {
		String h = Helper.shortHash(noteIdentity(n))
		return "avcb_${n['foreign'] ?: ''}_$h"
	}
	
	

    static String noteIdentity(Note n) {
        assert n
        return "${n.foreign} ${n.foreignExample}"
    }

    void syncNoteModels() {

        noteModel.assureIsComplete()
        parser.noteModels = [noteModel]
    }

    void syncNoteFields() {
        //println Helper.objectToJson(notes)
        notes.each { assureNote(it) }
        parser.jsonRoot.notes = notes
    }

    void assureCrowdDest() {
        assert destCrowdFolder
        assert Files.isDirectory(destCrowdFolder)
        Path corwdMediaPath = destCrowdFolder.resolve("media")
        corwdMediaPath.toFile().mkdirs()
    }

    @Deprecated
    void syncMedia() {
        copyMediaLinks(
                notes.collectMany { Note n ->
                    n.mediaLinks
                }.findAll()
                /*notes.collectMany { Note n->
                    [
                        n.img,
                        n.foreignTTS,
                        n.foreignExampleTTS,
                        n.nativeTTS,
                        n.nativeAltTTS,
                        n.nativeExampleTTS
                    ].findAll()*/
        )
    }


    @Deprecated
    String mediaLink2CrowdLink(CharSequence mediaLink) {
        if (!mediaLink) return null
        return new File(mediaLink.toString()).name
    }

    @Deprecated
    void copyMediaLinks(List<CharSequence> links) {
        assureCrowdDest()
        Path corwdMediaPath = destCrowdFolder.resolve("media")
        assert resolveMediaLink: "Set a closure to resolve the mediaLink to file path"
        links.each { CharSequence mediaLink ->
            Path crowdPath = corwdMediaPath.resolve(mediaLink2CrowdLink(mediaLink))
            if (!Files.exists(crowdPath)) {
                println "$mediaLink: copy"
                Path sourcePath = resolveMediaLink(mediaLink)
                assert ignoreMissingMedia || Files.exists(sourcePath)
                if (Files.exists(sourcePath)) {
                    Files.copy(sourcePath, crowdPath)
                }

            }
        }
    }

    Path copyToMedia(Path sourcePath, CharSequence targetName = null) {
        assureCrowdDest()
        Path corwdMediaPath = destCrowdFolder.resolve("media")
        if (!targetName) targetName = sourcePath.fileName.toString()
        Path crowdPath = corwdMediaPath.resolve(targetName.toString())
        if (!Files.exists(crowdPath)) {
            println "$sourcePath -> $crowdPath"
            assert ignoreMissingMedia || Files.exists(sourcePath)
            if (Files.exists(sourcePath)) {
                Files.copy(sourcePath, crowdPath)
            }
        }
        return crowdPath
    }


    void save() {
        assureCrowdDest()
        syncNoteFields()
        syncNoteModels()
        //syncMedia()
        Path deckJson = destCrowdFolder.resolve("${destCrowdFolder.fileName}.json")
        parser.saveTo(deckJson.toFile())
    }

    Note updateNoteHaving(String foreignTerm) {
        assert foreignTerm
        Note n = notes.find { it['foreign'] == foreignTerm }
        if (!n) {
            n = new Note(model: noteModel)
            notes.add(n)
            syncNoteFields()
            //syncMedia(Paths.get( "/tmp/work"))
        }
        return n

    }

    static void main(String... args) {
        new VocbModel(
                destCrowdFolder: Paths.get("/tmp/work/test"),
                ignoreMissingMedia: true,
                ).tap {
            updateNoteHaving("newWord").tap {
                this['img'] = "newWordImg"
            }
            save()
        }
        println "done"

    }

}
