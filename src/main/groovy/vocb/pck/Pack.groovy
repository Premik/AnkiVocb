package vocb.pck

import static vocb.Ansi.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Map.Entry

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import vocb.Helper
import vocb.anki.crowd.Data2Crowd
import vocb.conf.ConfHelper
import vocb.conf.TreeConf
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.Manager
import vocb.ord.ConceptExtra
import vocb.ord.OrderSolver

@CompileStatic
public class Pack {
	
	String first100 = "First1000"

	Path destRootFolder = Paths.get("/tmp/work/pkg")


	Path packageRootPath = Paths.get("/data/src/AnkiVocb/pkg/")


	ConfHelper cfgHelper = new ConfHelper()
	@Lazy ConfigObject cfg = cfgHelper.config

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
		cfgHelper.extraLookupFolders.add(pkgFile)
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		LinkedHashSet<Example> exportExamples = [] as LinkedHashSet
		//if (info.name == first100) exportFirst1000(d2c)
		if (info.sentences) {
			exportSentences(info.sentences, d2c.dbMan, exportExamples)
			if (info.strictlyWordlist) {
				d2c.exportExamplesToCrowdStrict(exportExamples, info.wordList as HashSet<String>)
			} else {
				d2c.exportExamplesToCrowd(exportExamples)
			}
		}

		if (info.wordList && !info.strictlyWordlist) { //When strict, wordlist is not exported, only examples (with words)
			d2c.exportWordsToCrowd(info.wordList)
		}
		

		cfgHelper.extraLookupFolders.remove(pkgFile)
	}

	void exportSentences(String text, Manager dbMan, LinkedHashSet<Example> exportExamples ) {
		assert text
		dbMan.withBestExample(text) { Example e, String sen, Set<String> com, Set<String> mis->

			if (!mis)  {
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
	
	
	@CompileDynamic
	void exportFirst1000(Data2Crowd d2c) {
		//Words from the whole db without words from any package
		Set<String> pkgWords = wordsFromAllPackages()
		Set<String> wordsToExport = d2c.dbMan.db.concepts.findAll { Concept c->
			if (c.state == 'ignore') return false
			return !pkgWords.contains(c.firstTerm)
		} .collect {it.firstTerm}
		
		wordsToExport.each {println it}
		//d2c.info.@$wordList = wordsToExport as List<String>
		//Example candidates. Any example from db which contains a word-to-export 
		List<String> exCand = wordsToExport
			.collect {d2c.dbMan.examplesByFirstTermWords[it].collect{it.firstTerm}}
			.flatten()
			.toUnique()
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
	
	Set<String> wordsFromAllPackages() {
		allPackInfos.collect {it.allWords}.flatten().sort() as Set<String>
	}
	
	@CompileDynamic
	List<String> sentencesFromAllPackages() {
		allPackInfos.collect {wn.sentences(it.sentences)}.flatten().toUnique().sort()
	}


	public static void main(String[] args) {
		new Pack().tap { Pack p->			

			
			p.exportByName("First1000")
		}
		println "Done"

	}
}
