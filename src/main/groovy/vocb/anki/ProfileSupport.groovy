package vocb.anki

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import vocb.Helper
import vocb.anki.crowd.Note
import vocb.anki.crowd.NoteModel
import vocb.anki.crowd.VocbModel
import static vocb.Ansi.*
import static vocb.Ansi.*

public class ProfileSupport {

	String SqlDriverClassName = "org.sqlite.JDBC"

	Path profilePath = Paths.get(System.getProperty("user.home"), ".local", "share", "Anki2")
	String selectedProfile = "test"
	String nodeModelName = "ankivocb-en2cs-1"
	String deckName = ""

	@Lazy VocbModel vocbModel = new VocbModel()


	public Sql withProfileCollectionDb(String profileName, Closure<Sql> cl) {
		assert profileName
		assert Files.isDirectory(profilePath)
		Path pp=  profilePath.resolve(profileName).resolve("collection.anki2")
		assert Files.isRegularFile(pp)
		return Sql.withInstance("jdbc:sqlite:${pp}", SqlDriverClassName, cl)
	}



	public Set<String> listProfiles() {
		assert Files.isDirectory(profilePath)
		String[] ret
		Sql.withInstance("jdbc:sqlite:${profilePath.resolve('prefs21.db')}", SqlDriverClassName) { Sql sql ->
			ret =sql.rows("select name from profiles")
					.collect { GroovyRowResult r ->
						r.name
					}
					.findAll {
						!it.startsWith("_")
					} //Ignore _global
		}
		return ret as Set
	}

	public List<String> splitFields(String flds) {
		flds.split( (0x1f as Character).toString())
	}

	@Deprecated
	public Map loadModelsJson() {
		List<String> modelDbs
		withProfileCollectionDb(selectedProfile) { Sql sql->
			modelDbs= sql.rows("select models from col").collect { GroovyRowResult r ->
				r.models
			}
		}
		assert 	modelDbs.size() == 1
		Map ret = Helper.parseJson(modelDbs[0])
		//println Helper.jsonToString(ret)
		assert ret : "Failed to parse json ${modelDbs[0].take(200)}..."
		return ret
	}

	@Deprecated
	public Map loadDecksJson() {
		List<String> deckJsons
		withProfileCollectionDb(selectedProfile) { Sql sql->
			deckJsons= sql.rows("select decks from col").collect { GroovyRowResult r ->
				r.decks
			}
		}
		assert 	deckJsons.size() == 1
		Map ret = Helper.parseJson(deckJsons[0])
		//println Helper.jsonToString(ret)
		assert ret : "Failed to parse json ${deckJsons[0].take(200)}..."
		return ret
	}

	public Map<String, Map> getNodeModelsByName() {
		/*loadModelsJson().collectEntries {
			[it.value.name, it.value]
		}*/
		Map<String, Map> ret
		withProfileCollectionDb(selectedProfile) { Sql sql->
			ret = sql.rows("select * from notetypes").collectEntries { GroovyRowResult r ->
				[r.name, r]
			}
		}
		return ret
	}

	public Map getAnkivocbModel() {
		assert nodeModelsByName[nodeModelName]
		nodeModelsByName[nodeModelName]
	}

	public Long getAnkivocbModelId() {		
		return ankivocbModel.id
	}

	public Map<String, Map> getDecksByName() {
		/*loadDecksJson().collectEntries {
			[it.value.name, it.value]
		}*/
		Map<String, Map> ret
		withProfileCollectionDb(selectedProfile) { Sql sql->
			ret = sql.rows("select * from decks").collectEntries { GroovyRowResult r ->
				[splitFields(r.name)[-1], r]			
			}
		}
		return ret
	}

	public Long getSelectedDeckId() {
		assert !deckName || decksByName[deckName] : "Deck '$deckName' not found. Decks:\n ${decksByName.keySet().join('\n ')}"
		return decksByName[deckName]?.id
	}


	private GroovyRowResult expandNoteFields(GroovyRowResult r) {
		r.type = [0:"new", 1:"learning", 2:"due", 3:"relearning"][r.type]
		r.flds = splitFields(r.flds)
		return r
	}

	private String getDeckWhereCond() {
		if (!selectedDeckId) return ""
		return "and did = $selectedDeckId"
	}

	public List<GroovyRowResult> ankivocbCards() {
		List<GroovyRowResult> ret

		withProfileCollectionDb(selectedProfile) { Sql sql->
			String select = """
                  select c.id as cid, c.nid, c.did, c.ord, c.due, c.type, n.mid, n.guid, n.tags, n.flds from cards as c 
                        left join  notes as n on c.nid = n.id where 
                          mid = $ankivocbModelId
                          ${deckWhereCond}
                  """
			//println "${select}"
			ret =sql.rows(select).collect { expandNoteFields(it)}
		}
		return ret
	}

	public List<GroovyRowResult> ankivocbNotes() {
		List<GroovyRowResult> ret

		withProfileCollectionDb(selectedProfile) { Sql sql->
			String select = """
                  select id, guid, mid, tags, csum, flds from notes 
                   where mid = $ankivocbModelId				   
                  """
			//println "${select}"
			ret =sql.rows(select).collect { GroovyRowResult r ->
				r.flds = splitFields(r.flds)
				return r
			}
		}
		return ret
	}




	public List<Long> nodeIdsMissingNativeAlt(List<GroovyRowResult> rows) {
		int indx = vocbModel.noteModel.getFieldIndex("nativeAlt")
		rows
				.findAll { GroovyRowResult r-> !r?.flds[indx] }
				.findAll { GroovyRowResult r-> r.ord == 2  }
				.collect {it.cid as Long}
	}

	public void dropCardsByIdList(List<Long> ids) {
		String batch = ids.join(', ')
		withProfileCollectionDb(selectedProfile) { Sql sql->
			String s = "delete from cards where id in ($batch)"
			println s
			sql.execute(s)
		}
	}

	public void fixGuids() {
		NoteModel model = new VocbModel().noteModel
		int i = 0
		int fixed = 0
		ankivocbNotes().each { GroovyRowResult r->
			i++
			Note n =new Note(model: model, fields: r.flds)
			String newGuid = VocbModel.noteGuid(n)
			String guid = r.guid
			if (guid != newGuid) {
				fixed++
				println "${guid.padLeft(30)}\n${color(newGuid.padLeft(30), BOLD)} $r"
				withProfileCollectionDb(selectedProfile) { Sql sql->
					String s = "update notes set guid ='$newGuid' where id =$r.id"
					sql.execute(s)
					//throw new Exception("break")
				}
			}
		}
		println "$i processed. Fixed: $fixed"



	}


	static void main(String... args) {
		new ProfileSupport().with {
			println listProfiles()
			//selectedProfile = "test"
			selectedProfile = "Honzik"
			//println decksByName.keySet()
			deckName = "First1000"
			//deckName = "Jingle Bells"
			//deckName = "Five Little Monkeys"
			//deckName = "Mary Had a Little Lamb"
			//fixGuids()

			//println "mid($nodeModelName)=$ankivocbModelId did($deckName)=$selectedDeckId  "



			ankivocbCards().each { 
				println it.flds[2]
			}
			println "Done"








			//List<Long> toDelete = nodeIdsMissingNativeAlt(ankivocbCards())
			//println toDelete
			//dropCardsByIdList(toDelete)
			//println selectedDeckId
		}
	}
}
