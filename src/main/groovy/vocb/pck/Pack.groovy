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
import vocb.corp.Corpus
import vocb.corp.WordNormalizer
import vocb.data.Concept
import vocb.data.Example
import vocb.data.ExampleComparatorMatch
import vocb.data.Manager

@CompileStatic
public class Pack {

	String first100 = "First1000"

	Path destRootFolder = Paths.get("/tmp/work/pkg")


	Path packageRootPath = Paths.get("/data/src/AnkiVocb/pkg/")
	boolean silent=false


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



	WordNormalizer wn = WordNormalizer.instance


	@Memoized
	PackExport packExportOf(PackInfo info) {
		assert info
		ConfHelper cfgHelper = new ConfHelper()
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		return new PackExport(data2crowd: d2c, info:info, silent:silent)
	}

	void doExport(PackInfo info) {
		assert info
		File pkgFile = info.treeConf.path.toFile()
		packExportOf(info).with {
			confHelper.extraLookupFolders.add(pkgFile)
			data2crowd.export(export())
			confHelper.extraLookupFolders.remove(pkgFile)
		}
	}

	Set<String> exportedWordsOf(String ... names) {
		pkgsByName(names)
				.findAll()
				.collect {packExportOf(it)}
				.collectMany { it.exportedWords } as LinkedHashSet
	}

	Manager getDbMan() {
		packExportOf(allPackInfos.first()).dbMan
	}

	Set<String> findTopWordsNotInDb(int topx=1000) {
		Set<String> top = Corpus.buildDef().topX(topx) as LinkedHashSet
		//Set<String> topInDb = top.collectMany { dbMan.findConceptsByFirstTermAllVariant(it) }.collect {it.firstTerm} as LinkedHashSet
		top.findAll { !dbMan.findConceptsByFirstTermAllVariant(it)}
		.findAll {!it.contains("'")}
		.findAll {!it.contains(".")}
		.findAll {it.size()>1} as LinkedHashSet
	}

	Set<Concept> findTopConceptsFromAllPackages(int topx) {
		Helper.startWatch()

		Set<Concept> allExp = exportedItemsFromPackages()
				.map {it.concept}
				.filter {!it.ignore}
				.toSet()
				.toSorted {0-it.freq}
				.take(topx) as LinkedHashSet
		Helper.printLapseTime()
		return allExp
		//allExp.take(100).each {println it}
	}



	void printFirstX(int x=1000) {
		Helper.startWatch()
		Set<String> topDb = dbMan.db.concepts
				.findAll {!it.ignore}
				.toSorted {-1*(it.freq?:0)}
				.take(x)
				.collect {it.firstTerm} as LinkedHashSet
		Set<String> ignore = exportedWordsOf("Simple", "Supa", "Uncomm", "Basic", "First")

		Set<String> list = (topDb - ignore) as LinkedHashSet
		Helper.printLapseTime()
		Paths.get("/tmp/work/first${x}.txt").withPrintWriter { PrintWriter w->
			list.each {
				w.println(it)
			}
		}
		findBestExamplesFor(list)
	}

	private LinkedHashSet findBestExamplesFor(Collection<String> wordList) {
		println "${wordList.take(100).join(' ')} \nSize: ${wordList.size()}"
		int lastDec = 0
		//Set<String> matchedVariants = [] as LinkedHashSet
		List<String> matched = []
		Helper.startWatch()
		while (wordList.size() > 0) {
			
			ExampleComparatorMatch m =dbMan.bestExampleForSentence(wordList)[0]
			Helper.printLapseTime()
			if (!m) break
			println "-$lastDec ${m.toAnsiString()}"
			Collection<String> remove = m.matchesWordlist(wordList)
			matched.addAll(remove)
			/*if (!removed) {
			 //remove = m.a.wordsMatchingVariant(m.commonWordVariants)
			 println "Removing variants"					
			 removed = matchedVariants.intersect(list)
			 }*/
			println remove
			lastDec = remove.size()
			wordList-=remove

			if (lastDec == 0) {
				println "No more common words"
				break
			}
		}
		println "Matched: $matched"

		//println "Vars   : $matchedVariants"
		println "Left   : ${wordList-matched}"
	}

	void printExamplesExport() {
		Helper.startWatch()
		//exportedExamplesFromPackages(pkgsByName("First")).forEach {

		//}
		//return

		Set<Example> exported = exportedExamplesFromPackages().toSet() as LinkedHashSet
		Set<Example> ignore = exportedExamplesFromPackages(allPackInfos-pkgsByName("First")).toSet() as LinkedHashSet
		println ignore
		dbMan.db.examples.toSorted{it.firstTerm}.each { Example e->
			if (ignore.contains(e)) {
				println color(e.firstTerm, WHITE)
			}
			else {
				if (exported.contains(e)) {
					println color(e.firstTerm, BOLD)
				} else
					println e.firstTerm
			}
		}
		Helper.printLapseTime()
	}

	/*Stream<String> wordsFromPackages(List<PackInfo> infos) {
	 ConfHelper cfgHelper = new ConfHelper()
	 infos.stream()
	 .flatMap { PackInfo info->
	 Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
	 //Words from the whole db without words from any package
	 //wn.lemming(wordsFromAllPackages(packs).stream())
	 }
	 }*/


	void findBasicWords() {
		Set<String> l = exportedWordsOf("Basic") - exportedWordsOf("Simple")
		//def l = exportedWordsOf("BasicWords")
		println l
		l.each {

			new File("/tmp/work/BasicWordsNoSimple.txt") <<"$it\n"
		}
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


	public List<PackInfo>  pkgsByName(String ... names) {
		Map<String, Boolean> matches = names.collectEntries {[it, false]}
		List<PackInfo> ret = allPackInfos.findAll { PackInfo pi->
			names.any {String name->
				if (pi.treeConf.name.containsIgnoreCase(name)) {
					matches[name] = true
					return true
				}
				return false
			}
		}
		assert matches.each { String pkgSearch, Boolean m->
			assert m : "No package matching '$pkgSearch'"
		}
		return ret
	}


	public exportByName(String name) {
		export(pkgsByName(name))
	}


	Set<String> exportedWordsFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {packExportOf(it)}
		.collectMany { it.exportedWords } as HashSet
	}

	Stream<Example> exportedExamplesFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		exportedItemsFromPackages(pkgInfos).map{it.example}.filter {it as boolean}
	}



	Stream<ExportItem> exportedItemsFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.parallelStream().flatMap {packExportOf(it).export()}
	}

	Set<String> wordsFromAllPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collectMany {it.allWords}.toSet()
	}


	@Deprecated
	List<PackInfo> findAllPacksWithAnyWord(List<PackInfo> pkgInfos = allPackInfos, Closure wordFilter) {
		pkgInfos.findAll { PackInfo pi->
			pi.allWords.any (wordFilter)
		}
	}

	@Deprecated
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
			silent=true
			//p.exportByName("BasicWords")
			/*findTopxNotInDb(1000).each {
			 println it
			 }*/
			/*exportedWordsOf("Jing").each {
			 println it
			 }*/
			//printFirstX()
			ExampleComparatorMatch.preferedWords = Corpus.buildDef(1500).topX(1000) as LinkedHashSet
			//findBestExamplesFor(["that's (that is)", "that is"])
			//return
			printFirstX()

			//printExamplesExport()
			//p.export()
			//findBasicWords()
		}
		println "Done"
	}
}
