package vocb.conf;

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import java.util.stream.StreamSupport

import groovy.transform.CompileStatic
import vocb.Helper

@CompileStatic
public class TreeConf<T> {

	Path path
	TreeConf parent
	T obj
	
	@Lazy
	List<TreeConf> children = {
		assert path
		
	
		List<TreeConf> ret = path.toFile()
				.listFiles()				
				.findAll(subFolderFilter)
				.collect { it.toPath() }
				.collect { Path cp->					
					TreeConf tf = new TreeConf(
							path:cp,
							parent:this,
							subFolderFilter: subFolderFilter,
							)						
					return tf
				}
		//descendants.withIndex().each {TreeConf tf, int i-> tf.seq = i+1}
		return ret
	}()
	
	Stream<Path> thisFiles() { Files.list(path).filter(Files.&isRegularFile) }
	
	Stream<Path> files() {
		if (isRoot) return thisFiles()
		return Stream.concat(thisFiles(), parent.thisFiles())
	}

	@Lazy
	Path confPath = {
		List<Path> paths = thisFiles()		
		.filter{it.fileName.toString().endsWith(".conf")}
		.toList()
		assert paths.size() <=1
		return paths[0]
	}()
	
	public Path getRelativePath() {
		root?.path?.relativize(path)
	}

	@Lazy
	Map binding = {
		[self: this]
	}()

	@Lazy
	ConfigObject thisConf = {
		if (!confPath) return new ConfigObject()
		//println " parsing $confPath.text" 
		ConfHelper.parseString(confPath.text, binding)
	}()
	
	@Lazy
	ConfigObject conf = {
		if (isRoot) return thisConf
		ConfigObject ret = new ConfigObject()
		ret.merge(parent.conf)
		ret.merge(thisConf)
		return ret
	}()

	@Lazy
	String name = path?.fileName




	Closure<Boolean> subFolderFilter = Helper.subFolderFilter

	public List<CharSequence> validate() {
		List<CharSequence> ret = []
		if (!path || !Files.isDirectory(path) || !Files.exists(path) ) {
			ret.add("The '$path' is not an existing directory")
		}
		/*Set<TreeConf> visited = []
		descendantsStream().forEach { TreeConf t->
			assert !visited.contains(t) : "Cycle detected in the TreeConf $t"			
			visited.add(t)
		}*/
		
		List<TreeConf> wrongLinks = children.findAll {it.parent != this}
		if (wrongLinks) {
			ret.add("Children has unknown parent: $wrongLinks")
		}
		return ret
	}

	boolean getIsRoot() {
		parent == null
	}
	boolean getIsLeaf() {
		!children
	}


	List<TreeConf> getParents() {
		if (isRoot) return [this]
		return [this, *parent.parents] as List<TreeConf>
	}

	TreeConf getRoot() {
		if (isRoot) return this
		return parents[-1]
	}
	
	@Deprecated
	Stream<TreeConf> descendantsStream() {
		Stream.concat(Stream.of(this), children.stream())
	}
	
	
	
	List<TreeConf> getDescendants() {
		List<TreeConf> dsd = children*.descendants.flatten() as List<TreeConf>
		return [this, *dsd] as List<TreeConf>
	}
	
	List<TreeConf> getLeafs() { descendants.findAll {it.isLeaf} }
		
	
	TreeConf findByName(String name) {
		descendants.find {it.name == name}		
	}


	@Override
	public String toString() {
		String ch = ""
		if (children) {
			ch = "ꕞ$children"
		}
		"$name$ch"
	}
}
