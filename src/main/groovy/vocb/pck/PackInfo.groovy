package vocb.pck

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.ToString
import vocb.conf.TreeConf
import vocb.corp.WordNormalizer



@Canonical
@ToString(
includeNames=true,
ignoreNulls=true,
includePackage=false,
excludes=['sentences', 'wordList']
)
public class PackInfo {


	TreeConf treeConf
	Object pack
	//Pack pack //LinkageError error. Pack is loaded with GroovyCl, PackInfo with RootCl

	@Lazy Path sentencesPath = treeConf.path.resolve("sentences.txt")
	@Lazy Path wordsPath= treeConf.path.resolve("words.txt")

	public WordNormalizer getWn() {
		pack?.wn
	}


	@Lazy String sentencesText = {
		if (!Files.exists(sentencesPath)) return ""
		return sentencesPath.toFile().text
	}()


	List<String> getSentences()  {
		wn.sentences(sentencesText)
	}

	@Lazy Collection<String> wordList = {
		if (!Files.exists(wordsPath)) return [] as List<String>
		return wordsPath.toFile().text.split(/\s+/) as LinkedHashSet<String>
	}()

	@Lazy Set<String> allSentenceWords = {
		wn.wordsInSentences(sentencesText).keySet().sort() as Set<String>
	}()

	@Lazy Set<String> allWords = {
		allSentenceWords + wordList
	}()






	@Lazy Path destPath = {
		assert pack?.destRootFolder
		pack.destRootFolder.resolve(name?:displayName).tap {
			toFile().mkdirs()
		}
	}()

	@Lazy public String name = treeConf.name
	
	@Lazy public String groupName= {
		if (treeConf.isRoot) return ""
		return treeConf.parent.name
	}()
	

	@Deprecated
	public List<String> getExcludeWordsFromPackageNames() {
		treeConf.conf.excludeWordsFromPackages?:[]
	}

	@Deprecated
	public List<PackInfo> getExcludeWordsFromPackage() {
		excludeWordsFromPackageNames.collect {
			List<PackInfo> infos = pack.pkgsByName(it)
			assert infos.size() == 1
			return infos[0]
		}
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

	public boolean getStrictlyWordlist() {
		treeConf.conf.strictlyWordlist
	}


	public String getUuid() {
		if (!name) return ""
		String p = name.toLowerCase()*10
		assert p.length() > 30
		p = p[0..21]
		//return "ankivocb-${p[0..3]}-${p[4..7]}-${p[8..11]}-${p[11..22]}"
		return "ankivocb-2020-$p"
	}
}
