package vocb.pck

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString


@Canonical
@CompileStatic
@ToString(
	includeNames=true,
	ignoreNulls=true,
	includePackage=false,
	excludes=['sentences', 'wordList'],	
	)
public class PackInfo {
	
	String name	
	Path destRootFolder = Paths.get("/tmp/work")
	Path packageRootPath = Paths.get("/data/src/AnkiVocb/pkg/")
	
	
	
	@Lazy Path packagePath = packageRootPath.resolve(name)
	@Lazy Path infoConfPath = packagePath.resolve("info.conf")
	@Lazy Path sentencesPath = packagePath.resolve("sentences.txt")
	@Lazy Path wordsPath= packagePath.resolve("words.txt")
	@Lazy ConfigObject infoCfg =  {
		if (!Files.exists(infoConfPath)) return new ConfigObject()
		return new ConfigSlurper().parse(infoConfPath.text)
	}();
	
	@Lazy String sentences = {
		if (!Files.exists(sentencesPath)) return ""
		return sentencesPath.text
	}()
	
	@Lazy List<String> wordList = {
		if (!Files.exists(wordsPath)) return [] as List<String>
		return wordsPath.text.split(/\s+/) as List<String>
	}()
	
	
	
	@Lazy Path destPath = {
		assert destRootFolder
		destRootFolder.resolve(name).tap {
			toFile().mkdirs()
		}
	}()
	
	
	
	public String getDisplayName() {
		infoCfg.displayName?:name?.replaceAll(/(\p{Lu})(\p{L})/, ' $1$2')?.trim()
	}
	
	public void setDisplayName(String v) {
		infoCfg.displayName = v		
	}
	
	public String getBackgroundName() {
		infoCfg.backgroundName?:"_${name}Background"
	}
	
	public void setBackgroundName(String v) {
		infoCfg.backgroundName = v
	}
	
	public boolean getNoExamples() {
		infoCfg.noExamples
	}
	
	public String getUuid() {
		if (!name) return ""	
		String p = name.toLowerCase()*10
		assert p.length() > 30
		//return "ankivocb-${p[0..3]}-${p[4..7]}-${p[8..11]}-${p[11..22]}"
		return "ankivocb-2020-${p[0..21]}"
	}
		
}
