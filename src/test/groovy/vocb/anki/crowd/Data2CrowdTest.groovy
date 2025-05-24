package vocb.anki.crowd

import org.junit.jupiter.api.Test

import vocb.data.Concept
import vocb.data.ConceptDb
import vocb.data.ConceptYamlStorage

class Data2CrowdTest {

	Data2Crowd dc = new Data2Crowd()
	URL testConceptsUrl = getClass().getResource('/vocb/data/fullyPopulated.yaml')

	@Test
	void testNumberOfStars() {
		assert dc.numberOfStarts(0) ==null
		assert dc.numberOfStarts(10) == 0
		assert dc.numberOfStarts(16*1000) == 1
		assert dc.numberOfStarts(600*1000) == 2
		assert dc.numberOfStarts(1600*1000) == 3

		assert dc.numberOfStarts(null) == null
	}
	
	@Lazy Concept firstConcept= {
		ConceptDb db =  new ConceptYamlStorage().parseDb(testConceptsUrl.newReader())
		db.concepts[0]		
	}()

	@Test
	void mapConcept() {		
		Note n = new Note(model:dc.vocbModel.noteModel)
		dc.concept2CrowdNote(firstConcept, n)
		assert n.fields == ['in.jpeg', '5', 'in', 'in.mp3', 'Not in my city.', 'Not in my city.mp3', 'v', 'v.mp3', 've', 've.mp3', 'Ne v mém městě.', 'Ne v mem meste.mp3']
	
	}
	
	@Test
	void renderCardTemplate() {
		ConfigObject  renderCardTemplate= dc.cfg.renderCardTemplate
		NoteModel nm = dc.renderCardTemplate(renderCardTemplate)
		assert nm
		nm.tmpls.each {
			println "${it}"
		}
		nm.assureIsComplete()
	
	}
}


