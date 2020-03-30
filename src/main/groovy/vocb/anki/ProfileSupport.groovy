package vocb.anki

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.sql.GroovyRowResult
import groovy.sql.Sql

public class ProfileSupport {

	String SqlDriverClassName = "org.sqlite.JDBC"

	Path profilePath = Paths.get(System.getProperty("user.home"), ".local", "share", "Anki2")


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
					.collect { GroovyRowResult r -> r.name}
					.findAll { !it.startsWith("_")} //Ignore _global
		}
		return ret as Set
	}

	public List<String> listAllFields(String profileName) {
		List<String> ret
		withProfileCollectionDb(profileName) {Sql sql->
			ret =sql.rows("select flds from notes")
					.collect { GroovyRowResult r -> r.flds}
					.collectMany { String fGrp->
						fGrp.split("[]") as List
					}
		}
		return ret
	}



	static void main(String... args) {
		ProfileSupport mm  = new ProfileSupport()
		println mm.listProfiles()
		println mm.listAllFields("Honzik")
	}
}
