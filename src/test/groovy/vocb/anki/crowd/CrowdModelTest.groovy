package vocb.anki.crowd

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import groovy.test.GroovyAssert

class CrowdModelTest {
	
	VocbModel vocbModel = new VocbModel()
	
	TemplateModel tmp1 = new TemplateModel(name: "test", afmt: "answer formaate", qfmt: "questionFormat")

	NoteModel testModel = new NoteModel(name:"testModel", flds: [
		new FieldModel(name:"f1"),
		new FieldModel(name:"f2")
	], tmpls: [tmp1])


	
	
	@Test
	void noteClone() {
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
		assert n
		assert n.img == "img"
		assert n.nativeTTS == "nativeTTS"
		assert n.freq == "freq"
		
		GroovyAssert.shouldFail { 
			n.nopeField
		} 	
	}
	
	@Test
	@Disabled
	void modelComplete() {
		Note a = vocbModel.notes[0]
		Note b = a.clone()
		assert a.img == b.img
		assert a.freq == b.freq
		assert a.tags == b.tags
		assert a.model
		assert a.model === b.model		
	}
	
	@Test
	void hashTest() {
		Note a = vocbModel.notes[0]
		println "${a}"		
		//a.assertIsComplete()
		int orighash = a.hashCode()
		a.data= "data"
		assert orighash == a.hashCode()
		vocbModel = new VocbModel()
		a = vocbModel.notes[0]
		//println "${a}"
		assert orighash == a.hashCode()
		a.img = "newImg"
		assert orighash != a.hashCode()
	}
	
	
}
