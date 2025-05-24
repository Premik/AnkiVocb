package vocb.anki.crowd;

class VocbModel {

	URL crowdJsonUrl = getClass().getResource('/template/deck.json')

	@Lazy CrowdParser parser = new CrowdParser(json:crowdJsonUrl.text)


	@Lazy NoteModel noteModel = {
		NoteModel n = parser.ankivocbModel
		assert n : "Failed to find the ankivobc model. The model name must start with 'ankivocb' "
		return n
	}()
	
	List<Note> getNotes() {
		parser.allNotes.findAll {it.model == noteModel}
	} 
	
}
