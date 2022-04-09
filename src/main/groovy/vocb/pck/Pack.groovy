package vocb.pck

import static vocb.Ansi.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import vocb.Helper
import vocb.anki.crowd.Data2Crowd
import vocb.conf.ConfHelper
import vocb.conf.TreeConf
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager

@CompileStatic
public class Pack {

	String first100 = "First1000"

	Path destRootFolder = Paths.get("/tmp/work/pkg")


	Path packageRootPath = Paths.get("/data/src/AnkiVocb/pkg/")


	//ConfHelper cfgHelper = new ConfHelper()
	//@Lazy ConfigObject cfg = cfgHelper.config

	@Lazy
	TreeConf<PackInfo> treeConf = new TreeConf<PackInfo>(subFolderFilter:this.&isFolderPackage, path: packageRootPath)

	@Lazy List<PackInfo> allPackInfos = {
		treeConf.leafs.collect { TreeConf<PackInfo> tc->

			PackInfo pi = new PackInfo(
					pack: this,
					treeConf: tc).tap {
						tc.obj= it
					}
					
			return pi
		}
	}()



	WordNormalizer wn = new WordNormalizer()
	
	
	

	void doExport(PackInfo info) {
		assert info
		File pkgFile = info.treeConf.path.toFile()
		ConfHelper cfgHelper = new ConfHelper()
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		PackExport pe = new PackExport(dbMan: d2c.dbMan, info:info)
		cfgHelper.extraLookupFolders.add(pkgFile)
		d2c.export(pe.export())
		cfgHelper.extraLookupFolders.remove(pkgFile)
	}
	
	@Deprecated
	@CompileDynamic
	void doExportOld(PackInfo info) {
		assert info
		File pkgFile = info.treeConf.path.toFile()
		cfgHelper.extraLookupFolders.add(pkgFile)
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		LinkedHashSet<Example> exportExamples = [] as LinkedHashSet
		//if (info.name == first100) exportFirst1000(d2c)
		if (info.sentences) {
			collectSentencesForExport(info.sentencesText, d2c.dbMan, exportExamples)//Collect best example sentences
			if (info.strictlyWordlist) {
				//Don't add new words from sentences, only exports words in the wordlist
				d2c.exportExamplesToCrowdStrict(exportExamples, info.wordList as HashSet<String>)
			} else {
				//Export all words from sentences
				d2c.exportExamplesToCrowd(exportExamples)
			}
		}
		if (info.wordList && !info.strictlyWordlist) {
			//Word list (with no examples)
			d2c.exportWordsToCrowd(info.wordList)
		}
		cfgHelper.extraLookupFolders.remove(pkgFile)
	}

	@Deprecated
	@CompileDynamic
	void collectSentencesForExport(String text, Manager dbMan, LinkedHashSet<Example> exportExamples ) {
		assert text
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  { //Exact match, jsut export
				println color(sen, BOLD)
				exportExamples.add(e)
				return
			}

			String col = NORMAL
			if (mis.size() > 1 || !e)  {
				col = RED
			} else {
				exportExamples.add(e)
			}
			println "${color(sen, col)} -> ${color(e?.firstTerm, BLUE)} ${color(mis.join(' '), MAGENTA)}"
		}
	}



	void findFirst1000() {
		PackInfo info = pkgsByName(first100)?.first()
		
		assert info : "$first100 pkg not found"
		ConfHelper cfgHelper = new ConfHelper()
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		//Words from the whole db without words from any package
		Set<String> pkgWords = wn.lemming(wordsFromAllPackages(allPackInfos - info).stream())
				.collect(Collectors.toSet())
				
		//pkgWords.findAll {it.startsWith("cer")}
		//.each {println it}
				println "-"*100
		println findAllPacksWithWord("cert")
		return


		Set<String> wordsToExport = d2c.dbMan.db.concepts
				.findAll { Concept c->
					if (c.state == 'ignore') return false

					return !pkgWords.contains(wn.stripBracketsOut(c.firstTerm))
				}
				.collect {it.firstTerm}
				.toSet()


		wordsToExport.each {println it}
		//d2c.info.@$wordList = wordsToExport as List<String>
		//Example candidates. Any example from db which contains a word-to-export
		List<String> exCand = wordsToExport
				.collect {d2c.dbMan.examplesByFirstTermWords[it].collect{it.firstTerm}}
				.flatten()
				.toUnique() as List<String>
		exCand.each {println it}
		//Compose the "sentences.txt"
		//d2c.info.@$sentences= exCand.join("\n")
	}

	static Pack buildFromRootPath(Path path) {
		assert path
		assert Helper.subFolderCountIn(path) > 0 : "No packages found. $path has no sub-folders"
		new Pack(packageRootPath: path)
	}

	@CompileDynamic
	static boolean isFolderPackage(Path folder) {
		assert folder
		if (!Helper.subFolderFilter(folder.toFile())) return false
		return Files.isRegularFile(folder.resolve("packageDescription.md"))
	}

	static boolean isFolderPackage(File folder) {
		isFolderPackage(folder.toPath())
	}

	public export(List<PackInfo> pkgs=allPackInfos) {
		int cnt = pkgs.size()
		(0..cnt-1).stream().parallel().forEach( {int p->
			PackInfo pi
			synchronized (Pack.class ) {
				pi = pkgs[p]
			}

			println '*'*100
			println "* ${pi.treeConf.name}"
			println '*'*100

			doExport(pi)
		})

		println "Done"
	}
	

	public List<PackInfo>  pkgsByName(String name) {
		allPackInfos.findAll {it.treeConf.name.containsIgnoreCase(name)}
	}


	public exportByName(String name) {
		export(pkgsByName(name))
	}

	Set<String> wordsFromAllPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {it.allWords}.flatten().sort() as Set<String>
	}
	
	Set<String> exportedWordsFromAllPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {it.allWords}.flatten().sort() as Set<String>
	}
	
	List<PackInfo> findAllPacksWithAnyWord(List<PackInfo> pkgInfos = allPackInfos, Closure wordFilter) {
		pkgInfos.findAll { PackInfo pi->
			pi.allWords.any (wordFilter)
		}
	}
	
	List<PackInfo> findAllPacksWithWord(String word, List<PackInfo> pkgInfos = allPackInfos) {
		findAllPacksWithAnyWord(pkgInfos) { String w->
			w.containsIgnoreCase(word)
		}
	}

	@CompileDynamic
	List<String> sentencesFromAllPackages(List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {wn.sentences(it.sentences)}.flatten().toUnique().sort()
	}


	public static void main(String[] args) {
		
		new Pack().tap { Pack p->
			


			//p.exportByName("Basic")
			//findFirst1000()
			p.export()
		}
		println "Done"
	}
}
