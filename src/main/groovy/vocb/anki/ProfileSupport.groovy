package vocb.anki

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import vocb.Helper
import vocb.anki.crowd.CrowdParser
import vocb.anki.crowd.VocbModel

public class ProfileSupport {

	String SqlDriverClassName = "org.sqlite.JDBC"

	Path profilePath = Paths.get(System.getProperty("user.home"), ".local", "share", "Anki2")
	String selectedProfile = "test"
	String nodeModelName = "ankivocb-en2cs-1"
	String deckName = "JingleBells"

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
		loadModelsJson().collectEntries {
			[it.value.name, it.value]
		}
	}

	public Map getAnkivocbModel() {
		nodeModelsByName[nodeModelName]
	}

	public Long getAnkivocbModelId() {
		assert ankivocbModel.id : "Failed to find the $nodeModelName model "
		return ankivocbModel.id
	}

	public Map<String, Map> getDecksByName() {
		loadDecksJson().collectEntries {
			[it.value.name, it.value]
		}
	}

	public Long getSelectedDeckId() {
		decksByName[deckName]?.id
	}




	public List<GroovyRowResult> ankivocbCards() {
		List<GroovyRowResult> ret

		withProfileCollectionDb(selectedProfile) { Sql sql->
			String did = ""
			if (selectedDeckId) did = "and did = $selectedDeckId"
			String select = """
                  select c.id as cid, c.nid, c.did, c.ord, c.due, c.type, n.mid, n.guid, n.tags, n.flds from cards as c 
                        left join  notes as n on c.nid = n.id where 
                          mid = $ankivocbModelId
                          $did
                  """
			//println "${select}"
			ret =sql.rows(select) //
					.collect { GroovyRowResult r ->
						r.type = [0:"new", 1:"learning", 2:"due", 3:"relearning"][r.type]
						r.flds = splitFields(r.flds)
						return r
					}
		}
		return ret
	}
	
	public void ankivocbCards(List<GroovyRowResult> interleaveAltCards) {
		
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


	static void main(String... args) {
		new ProfileSupport().with {
			//println listProfiles()
			selectedProfile = "Honzik" 
			//deckName = "Supaplex"
			//deckName = "Jingle Bells"
			//deckName = "Five Little Monkeys"
			println "mid($nodeModelName)=$ankivocbModelId did($deckName)=$selectedDeckId  "


			List<Long> toDelete = nodeIdsMissingNativeAlt(ankivocbCards())
			println toDelete
			dropCardsByIdList(toDelete)
			//println selectedDeckId
		}
	}
}
