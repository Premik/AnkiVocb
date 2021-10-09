package vocb.pck

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.conf.TreeConf


@Canonical
@ToString(
	includeNames=true,
	ignoreNulls=true,
	includePackage=false,
	excludes=['sentences', 'wordList'],	
	)
public class PackInfo {
	
	
	TreeConf treeConf	
	Pack pack
		
	@Lazy Path sentencesPath = treeConf.path.resolve("sentences.txt")
	@Lazy Path wordsPath= treeConf.path.resolve("words.txt")
	
	
	@Lazy String sentences = {
		if (!Files.exists(sentencesPath)) return ""
		return sentencesPath.text
	}()
	
	@Lazy List<String> wordList = {
		if (!Files.exists(wordsPath)) return [] as List<String>
		return wordsPath.text.split(/\s+/) as List<String>
	}()
	
		
	@Lazy Path destPath = {
		assert pack.destRootFolder 
		pack.destRootFolder.resolve(name?:displayName).tap {
			toFile().mkdirs()
		}
	}()
	
	public String getName() {
		treeConf.name
	}
	
	public String getDisplayName() {
		treeConf.conf.displayName?:name?.replaceAll(/(\p{Lu})(\p{L})/, ' $1$2')?.trim()
	}
	
	public void setDisplayName(String v) {
		treeConf.conf.displayName = v		
	}
	
	public String getBackgroundName() {
		treeConf.conf.backgroundName?:"_${name}Background"
	}
	
	public void setBackgroundName(String v) {
		treeConf.conf.backgroundName = v
	}
	
	public boolean getNoExamples() {
		treeConf.conf.noExamples
	}
	
	public String getUuid() {
		if (!name) return ""	
		String p = name.toLowerCase()*10
		assert p.length() > 30
		//return "ankivocb-${p[0..3]}-${p[4..7]}-${p[8..11]}-${p[11..22]}"
		return "ankivocb-2020-${p[0..21]}"
	}
	
	
		
}
