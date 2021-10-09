package vocb.anki.crowd

import java.nio.file.Path
import java.nio.file.Paths

import org.junit.jupiter.api.Test

import vocb.conf.ConfHelper
import vocb.conf.TreeConf
import vocb.data.Concept
import vocb.data.ConceptDb
import vocb.data.ConceptYamlStorage
import vocb.data.Example
import vocb.pck.Pack
import vocb.pck.PackInfo

class Data2CrowdTest {
	
	Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("ankivocbData2Crowd")

	ConfHelper cfgHelper = new ConfHelper()
	ConfigObject cfg = cfgHelper.config
	
	Pack pack = new Pack(destRootFolder: tmpDir)
	TreeConf treeConf = new TreeConf().tap {
		it.@$conf = new ConfigObject()
	}
	
	Data2Crowd dc = new Data2Crowd(cfgHelper:cfgHelper).tap {		
		//delegate.metaClass.setAttribute(delegate, '$cfg', cfg)
        info = new PackInfo(treeConf: treeConf, displayName: "test", pack:pack)
    }
    
    URL testConceptsUrl = getClass().getResource('/vocb/data/fullyPopulated.yaml')

    @Lazy
    ConceptDb db = new ConceptYamlStorage().parseDb(testConceptsUrl.newReader())

    @Lazy
    Concept firstConcept = db.concepts[0]
    @Lazy
    Example firstExample = db.examples[0]


    @Test
    void mapConceptRawField() {
        cfg.useRawNoteFields = true
        Note n = new Note(model: dc.vocbModel.noteModel)
        dc.concept2CrowdNote(firstConcept, firstExample, n)
        assert n.fields == ['in', '_in-en-ankivocb.mp3', 'Not in my city.', 'Not in my city-en-ankivocb.mp3',
                            'v', '_v-cs-ankivocb.mp3', 've', '_ve-cs-ankivocb.mp3', 'Ne v mém městě.', 'Ne v mem meste-cs-ankivocb.mp3',
                            '_in-ankivocb.jpeg', '5', '']


    }

    @Test
    void mapConcept() {
        cfg.useRawNoteFields = false
        Note n = new Note(model: dc.vocbModel.noteModel)
        dc.concept2CrowdNote(firstConcept, firstExample, n)
        assert n.fields == ['in', '[sound:_in-en-ankivocb.mp3]', 'Not in my city.',
                            '[sound:Not in my city-en-ankivocb.mp3]', 'v', '[sound:_v-cs-ankivocb.mp3]', 've',
                            '[sound:_ve-cs-ankivocb.mp3]', 'Ne v mém městě.', '[sound:Ne v mem meste-cs-ankivocb.mp3]',
                            '<img src=\'_in-ankivocb.jpeg\'>', '5', '']

    }

    @Test
    void renderCardTemplate() {
        ConfigObject renderCardTemplate = dc.cfg.renderCardTemplate
        NoteModel nm = dc.renderCardTemplate(renderCardTemplate)
        assert nm
        nm.assureIsComplete()
        String css = nm.css
        assert css
        assert nm === dc.vocbModel.noteModel
        assert nm === dc.vocbModel.parser.defaultModel
        dc.vocbModel.syncNoteModels()
        String json = dc.vocbModel.parser.toJsonString()
        CrowdParser p2 = new CrowdParser(json: json)
        assert p2.defaultModel.css == css

    }
}


