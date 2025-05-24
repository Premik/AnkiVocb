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


	@Memoized
	PackExport packExportOf(PackInfo info) {
		assert info
		ConfHelper cfgHelper = new ConfHelper()
		Data2Crowd d2c = new Data2Crowd (info : info, cfgHelper:cfgHelper)
		return new PackExport(data2crowd: d2c, info:info)
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
		names
				.collectMany {pkgsByName(it)}
				.findAll()
				.collect {packExportOf(it)}
				.collectMany { it.exportedWords } as LinkedHashSet
	}

	Manager getDbMan() {
		packExportOf(allPackInfos.first()).dbMan
	}

	void findTopxNotInDb(int topx=1000) {
		Set<String> top = Corpus.buildDef().topX(topx) as LinkedHashSet
		//Set<String> topInDb = top.collectMany { dbMan.findConceptsByFirstTermAllVariant(it) }.collect {it.firstTerm} as LinkedHashSet
		top.findAll { !dbMan.findConceptsByFirstTermAllVariant(it)}
		.findAll {!it.contains("'")}
		.findAll {!it.contains(".")}
		.findAll {it.size()>1}
		.each {
			println it
		}
	}

	void findTopxFromDb(int topx) {
		Helper.startWatch()


		Set<Concept> allExp = exportedItemsFromPackages()
				.map {it.concept}
				.filter {!it.ignore}
				.toSet()
				.toSorted {0-it.freq} as LinkedHashSet
		Helper.printLapseTime()
		allExp.take(100).each {println it}


		return


		File f = new File("/tmp/work/First1000.txt")
		if (f.exists()) {
			f.delete()
		}

		0.each {
			//f <<"$it\n"
			println it
		}
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
		def l = exportedWordsOf("Basic") - exportedWordsOf("Simple")
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
		allPackInfos.findAll { PackInfo pi->
			names.any {String name-> pi.treeConf.name.containsIgnoreCase(name)}
		}
	}


	public exportByName(String name) {
		export(pkgsByName(name))
	}


	Set<String> exportedWordsFromPackages( List<PackInfo> pkgInfos = allPackInfos) {
		pkgInfos.collect {packExportOf(it)}
		.collectMany { it.exportedWords } as HashSet
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



			//p.exportByName("BasicWords")
			//findTopxNotInDb(1000)
			findTopxFromDb(1000)
			//p.export()
			//findBasicWords()
		}
		println "Done"
	}
}
