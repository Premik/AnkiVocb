package vocb.conf

import static java.lang.System.getProperty
import static java.lang.System.getenv

import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import groovy.util.logging.Log4j2

@Log4j2
class ConfHelper {

	@Lazy
	public static ConfHelper instance = new ConfHelper().tap {
		loadDefault()
	}

	@Lazy  public static ConfigObject cfg = instance.config

	//public GroovyClassLoader defaultClassLoader
	public final configRootFolder="Ankivocb"
	public  final List<String> resExplicitExtensions = ['.conf', '.html']
	public  final List<String> cpFolders = [
		'',
		'data',
		'conf',
		'wiki',
		'vocb',
		'templates',
		'vocb/data',
		'vocb/conf',
		'vocb/wiki',
		'vocb/templates'
	]
	private static String windowsHomePath = "${getenv('HOMEDRIVE')}${getenv('HOMEPATH')}"


	private  ConfigObject mergedCfg

	@Lazy  public  ConfigObject config = {
		if (mergedCfg == null) {
			mergedCfg = new ConfigObject()
			loadDefault()
		}
		return mergedCfg
	}()

	public  String ensureEndSlash(String s) {
		if (!s) return ""
		if (s.endsWith(File.separator)) return s
		return s + File.separator
	}

	public  List<String> lookupFoldersToConsider =
	[
		"${ensureEndSlash(getenv('XDG_CONFIG_HOME'))}${File.separator}$configRootFolder",
		"${ensureEndSlash(getProperty('user.home'))}.config${File.separator}$configRootFolder",
		"${ensureEndSlash(windowsHomePath)}${File.separator}$configRootFolder",
		"${ensureEndSlash(getProperty('user.dir'))}$configRootFolder"
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



	public static  ConfigObject parseString(String cfgString, Map binding =[:], ConfigSlurper cfgSlurper = new ConfigSlurper()) {
		cfgSlurper.setBinding(binding)
		//cfgSlurper.classLoader = ConfHelper.classLoader
		return cfgSlurper.parse(cfgString)
	}

	@CompileStatic
	public static ConfigObject parseMap(Map<String, String> mapProps, Map binding =[:]) {
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
		ConfigSlurper cfgSlurper = new ConfigSlurper()
		/*if (defaultClassLoader) {
		 cfgSlurper.classLoader = defaultClassLoader
		 Thread.currentThread().setContextClassLoader(defaultClassLoader)
		 }*/
		return parseString(cfgStr, binding, cfgSlurper)
	}

	@CompileStatic
	public   ConfigObject loadAndMergeConfig(String configName, Map binding = null) {
		log.info("Loading $configName config")
		ConfigObject c = loadConfig(configName, binding)
		if (!c) throw new IllegalArgumentException("Failed to load the '$configName' config.\n$confPathDetails")
		if (mergedCfg != null) {
			mergedCfg.merge(c)
		} else {
			mergedCfg = c
		}

		return mergedCfg
	}

	public InputStream resolveResExactName(String resName, File[] lookupPaths =extraLookupFolders + lookupFolders) {
		if (!resName) return null
		//First try to find a file. Take the first match
		File file = lookupPaths.findResult { File pf->
			File f
			if (!pf) f = new File(resName) //Res name is (absolute) path on its own
			else f = new File(pf, resName)
			if (f.exists() && f.isFile()) return f
			else return null
		}

		if (file) return file.newInputStream()
		//Not found in file. Try classpath resource

		InputStream is = [
			//Try few class-loaders. They might be different on Graal
			this.class.classLoader,
			ConfHelper.class.classLoader,
			Thread.currentThread().contextClassLoader,
			ClassLoader.systemClassLoader,
		].findAll {it}
		.toUnique{a -> a.hashCode()}
		.collect { ClassLoader cl ->
			cpFolders.collectMany {
				[
					"$it/$resName",
					"/$it/$resName",
					"../$it/$resName"
				]
			}
			.collect{ String cpPath ->
				//println "   $cpPath $cl"
				cl.getResourceAsStream(cpPath)
			}.find {it}
		}.find {it}
		return is
	}

	public  InputStream resolveRes(String resName, File[] lookupPaths = extraLookupFolders + lookupFolders) {
		resolveRes(resName, lookupPaths, this.&resolveResExactName)
	}

	public  InputStream resolveRes(String resName, File[] lookupPaths = extraLookupFolders + lookupFolders, Closure cb) {
		(['']+ resExplicitExtensions).findResult {
			cb("$resName$it", [null, *lookupPaths] as File[] ) //Allow using path as resName
		}
	}


	public  StringBuilder toPrettyString(obj=config, int level = 0, StringBuilder sb = new StringBuilder()) {
		//https://stackoverflow.com/questions/7898068/pretty-print-for-a-groovy-configobject
		Closure indent = { lev -> sb.append("  " * lev) }
		if(obj instanceof Map){
			sb.append("{\n")
			obj.each{ String name, value ->
				if(name.contains('.')) return // skip keys like "a.b.c", which are redundant
					indent(level+1).append(name)
				(value instanceof Map) ? sb.append(" ") : sb.append(" = ")
				toPrettyString(value, level+1, sb)
				sb.append("\n")
			}
			indent(level).append("}")
		}
		else if(obj instanceof List){
			sb.append("[\n")
			obj.each{ value ->
				indent(level+1)
				toPrettyString(value, level+1, sb).append(",")
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

	public String getConfPathDetails() {
		String cps = cpFolders.collect{"cp:$it"}.join(" ")
		return "$lookupFoldersToConsider  $cps"
	}

	public loadDefault() {
		assert loadAndMergeConfig("ankivocb-default") :
		"""\n
              Failed to load the inbuilt 'ankivocb-default.conf' config. Paths considered: 
              $confPathDetails""".stripIndent()

		ConfigObject c=  loadAndMergeConfig("ankivocb")
		if (!c) log.warn("Couldn't find the custom ankivocb.conf file. Using the default.\n")
	}

	public Path getStoragePath() {
		Paths.get("$cfg.rootPath/$cfg.db.dbName")
	}

	public Path getPkgPath() {
		Paths.get("$cfg.rootPath/$cfg.pkg.pkgName")
	}

	public Path getOutPath() {
		Paths.get(cfg.outputRoot)
	}


	public static void main(String[] args) {
		new ConfHelper().with {
			println confPathDetails
			loadDefault()
			println toPrettyString()
		}
	}

	public static Path resolveOutputPath(String name, ConfigObject cfg=cfg) {
		assert cfg.outputRoot
		Path root = Paths.get(cfg.outputRoot.toString())
		root.toFile().mkdirs()
		return root.resolve(name)
	}
}
