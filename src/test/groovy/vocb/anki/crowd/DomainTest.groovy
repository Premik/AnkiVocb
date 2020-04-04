package vocb.anki.crowd

import org.junit.jupiter.api.Test

class DomainTest {


	NoteModel testModel = new NoteModel(name:"testModel", flds: [
		new FieldModel(name:"f1"),
		new FieldModel(name:"f2")
	])


	@Test
	void modelComplete() {
		testModel.assertIsComplete()
		println testModel
	}
}
