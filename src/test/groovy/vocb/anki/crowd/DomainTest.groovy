package vocb.anki.crowd

import org.junit.jupiter.api.Test
import groovy.test.GroovyAssert

class DomainTest {
	
	VocbModel vocbModel = new VocbModel()

	NoteModel testModel = new NoteModel(name:"testModel", flds: [
		new FieldModel(name:"f1"),
		new FieldModel(name:"f2")
	])


	@Test
	void modelComplete() {
		testModel.assureIsComplete()
	}
	
	@Test
	void vocbModelLoads() {
		assert vocbModel.noteModel
	}
	
	@Test
	void vocbNotesLoads() {
		assert vocbModel.notes.size() == 1
		Note n = vocbModel.notes[0]
		assert n.img == "img"
		assert n.nativeTTS == "nativeTTS"
		assert n.freq == "freq"
		
		GroovyAssert.shouldFail { 
			n.nopeField
		} 
		
	}
	
	
}
