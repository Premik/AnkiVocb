package vocb

import static java.lang.System.getProperty
import static java.lang.System.getenv

class ConfHelper {

	public static ConfHelper instance = new ConfHelper()
	@Lazy public static ConfigObject cfg = instance.config

	public  final List<String> resExplicitExtensions = ['.conf', '.html']
	public  final List<String> cpFolders = [
		'',
		'data',
		'conf',
		'wiki',
		'vocb',
		'template',
		'vocb/data',
		'vocb/conf',
		'vocb/wiki',
		'vocb/template'
	]
	private  String windowsHomePath = "${getenv('HOMEDRIVE')}${getenv('HOMEPATH')}"


	private  ConfigObject mergedCfg

	@Lazy  public  ConfigObject config = {
		if (mergedCfg == null) {
			mergedCfg = new ConfigObject()
			loadAll()
		}
		return mergedCfg
	}()

	public  String ensureEndSlash(String s) {
		if (s.endsWith(File.separator)) return s
		return s + File.separator
	}

	public  List<String> lookupFoldersToConsider =
	[
		"${ensureEndSlash(getProperty('user.home'))}.local${File.separator}share${File.separator}Ankivocb",
		"${ensureEndSlash(windowsHomePath)}${File.separator}Ankivocb",
		"${ensureEndSlash(getProperty('user.dir'))}conf"
	]

	@Lazy public  List<File> lookupFolders = {
		lookupFoldersToConsider.unique().findAll(this.&isLookupFolderValid) as File[]
	}()
	
	final List<File> extraLookupFolders= []

	public  boolean  isLookupFolderValid(String path) {
		if (!path) return false
		File f= new File(path)
		return f.exists() && f.isDirectory()
	}


	public  ConfigObject parseString(String cfgString, Map binding =[:]) {
		assert cfgString
		ConfigSlurper cfgSlurper = new ConfigSlurper()
		cfgSlurper.setBinding(binding)
		return cfgSlurper.parse(cfgString)
	}

	public  ConfigObject parseMap(Map<String, String> mapProps, Map binding =[:]) {
		ConfigSlurper cfgSlurper = new ConfigSlurper()
		Objects.requireNonNull(mapProps)
		Properties p = new Properties()
		p.putAll(mapProps)
		cfgSlurper.setBinding(binding)
		return cfgSlurper.parse(p)
	}

	public  ConfigObject loadConfig(String configName, Map binding = null) {
		String cfgStr = resolveRes( configName)?.text
		if (cfgStr == null) return null
		if (binding == null) binding = mergedCfg
		return parseString(cfgStr, binding)
	}

	public   ConfigObject loadAndMergeConfig(String configName, Map binding = null) {
		ConfigObject c = loadConfig(configName, binding)
		if (!c) return c
		if (mergedCfg != null) mergedCfg.merge(c)
		return c
	}

	public InputStream resolveResExactName(String resName, File[] lookupPaths =lookupFolders + extraLookupFolders) {
		if (!resName) return null
		//First try to find a file. Take the first match
		File file = lookupPaths.findResult { File pf->
			File f =  new File(pf, resName)
			if (f.exists() && f.isFile()) return f
			else return null
		}

		if (file) return file.newInputStream()
		//Not found in file. Try classpath resource

		InputStream is = [
			//Try few class-loaders. They might be different on Graal
			ConfHelper.class.classLoader,
			Thread.currentThread().contextClassLoader,
			ClassLoader.systemClassLoader,
		].findAll {it}.collect { ClassLoader cl ->
			cpFolders.collectMany {["$it/$resName", "/$it/$resName"]}
			.collect{ String cpPath ->
				//println cpPath
				cl.getResourceAsStream(cpPath)
			}.find {it}

		}.find {it}
		return is
	}

	public  InputStream resolveRes(String resName, File[] lookupPaths = lookupFolders + extraLookupFolders) {
		resolveRes(resName, lookupPaths, this.&resolveResExactName)
	}

	public  InputStream resolveRes(String resName, File[] lookupPaths = lookupFolders + extraLookupFolders, Closure cb) {
		(['']+ resExplicitExtensions).findResult {
			cb("$resName$it", lookupPaths)
		}
	}


	public  StringBuilder prettyPrintCfg(obj=config, int level = 0, StringBuilder sb = new StringBuilder()) {
		//https://stackoverflow.com/questions/7898068/pretty-print-for-a-groovy-configobject
		Closure indent = { lev -> sb.append("  " * lev) }
		if(obj instanceof Map){
			sb.append("{\n")
			obj.each{ String name, value ->
				if(name.contains('.')) return // skip keys like "a.b.c", which are redundant
					indent(level+1).append(name)
				(value instanceof Map) ? sb.append(" ") : sb.append(" = ")
				prettyPrintCfg(value, level+1, sb)
				sb.append("\n")
			}
			indent(level).append("}")
		}
		else if(obj instanceof List){
			sb.append("[\n")
			obj.each{ value ->
				indent(level+1)
				prettyPrintCfg(value, level+1, sb).append(",")
				sb.append("\n")
			}
			indent(level).append("]")
		}
		else if(obj instanceof String){
			sb.append(obj)
		}
		else {
			sb.append(obj)
		}
	}


	public  loadAll() {
		assert loadAndMergeConfig("ankivocb-default") : "Failed to load the inbuilt default config"
		ConfigObject c=  loadAndMergeConfig("ankivocb")
		if (!c) System.print("Couldn't find the custom ankivocb.conf file. Using the default.\n")
	}


	public static void main(String[] args) {
		println ConfHelper.instance.lookupFolders
		println ConfHelper.instance.prettyPrintCfg()
	}



}
