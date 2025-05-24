package vocb.anki.crowd

import org.apache.commons.lang3.builder.HashCodeExclude

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper


@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false,
includes=['fields']
)
public class Note {

	String __type__ = "Note"
	String[] fields


	String data=""
	int flags = 0
	String guid
	LinkedHashSet<String> tags = [] as LinkedHashSet

	String note_model_uuid
	NoteModel model

	public String sndField(String soundRef) {
		if (!soundRef) return ""
		return "[sound:$soundRef]"
	}

	public String imgField(String ref) {
		if (!ref) return ""
		return """<img src=":$ref">"""
	}

	@Deprecated
	public List<String> toRichFields(boolean fitToModel=true) {
		List<String> fls = [
			enWord,
			sndField(enSoundRef) ,
			czWord,
			sndField(czSoundRef),
			imgField(imageRef)
		].collect { it?:""}
		if (!fitToModel) return fls
		assert model : "Can't find to model since model is null"
		return Helper.padList(fls, "", model.fieldsCount, true)
	}

	public void hasTagWithPrefix(String prefix) {
		tags.find {it.toLowerCase().startsWith((prefix?:"").toLowerCase())}
	}


	public void assertIsComplete() {
		assert model : "Note has no model set"
		model.assureIsComplete()
		note_model_uuid = model.crowdanki_uuid

		//assert model.flds.size() == fields.length : "Model fields doesn't match the note"
		int modelSz = model.flds.size()
		if (modelSz != fields?.size()) {
			//Fields don't match the model. Resize
			String[] newFields = new String[modelSz]
			List<String> cropped = fields?.take(modelSz)?: []
			for (int i=0;i<modelSz;i++) {
				newFields[i] = cropped[i]?:""
			}
			fields = newFields
		}		
	}


	def propertyMissing(String name, value) {
		assertIsComplete()
		model.setFielValue(this, name, value)
	}

	def propertyMissing(String name) {
		assertIsComplete()
		model.getFielValue(this, name)
	}

	Object getAt(Integer index) {
		assertIsComplete()
		fields[index]
	}

	void putAt(Integer index, value) {
		assertIsComplete()
		fields[index] = value
	}
	
	

}
