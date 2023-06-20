package vocb.anki.crowd

import groovy.transform.CompileStatic
import org.apache.commons.lang3.builder.HashCodeExclude

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper


@ToString(
        includeNames = true,
        ignoreNulls = true,
        includePackage = false,
        includes = ['fields']
)
@EqualsAndHashCode(excludes = ["guid", "data", "model"])
//@CompileStatic
public class Note {

    String __type__ = "Note"
    String[] fields


    String data = ""
    int flags = 0
    String guid
    LinkedHashSet<String> tags = [] as LinkedHashSet

    String note_model_uuid
    NoteModel model
    //final LinkedHashSet<CharSequence> mediaLinks = [] as LinkedHashSet

    public void hasTagWithPrefix(String prefix) {
        tags.find { it.toLowerCase().startsWith((prefix ?: "").toLowerCase()) }
    }


    public void assertIsComplete() {
        assert model: "Note has no model set"
        model.assureIsComplete()
        note_model_uuid = model.crowdanki_uuid

        //assert model.flds.size() == fields.length : "Model fields doesn't match the note"
        int modelSz = model.flds.size()
				
        if (modelSz != fields?.size()) {
            //Fields don't match the model. Resize
            String[] newFields = new String[modelSz]
            List<String> cropped = fields?.take(modelSz) ?: []
            for (int i = 0; i < modelSz; i++) {
                newFields[i] = cropped[i] ?: ""
            }
			assert newFields
            fields = newFields
        }		
    }


    def propertyMissing(String name, value) {
        putAt(name, (String)value)
    }

    def propertyMissing(String name) {
        getAt(name)
    }

    String getAt(Integer index) {
        assertIsComplete()
        fields[index]
    }

    void putAt(Integer index, String value) {
        assertIsComplete()
        fields[index] = value
    }

    String getAt(String name) {
        assertIsComplete()
        model.getFielValue(this, name)
    }

    void putAt(String name, String value) {
        assertIsComplete()
        model.setFielValue(this, name, value)
    }


}
