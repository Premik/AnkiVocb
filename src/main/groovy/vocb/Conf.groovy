package vocb

import org.codehaus.groovy.control.CompilerConfiguration
import static java.lang.System.getProperty
import static java.lang.System.getenv


class ConfHelper {

	public static final List<String> resExplicitExtensions = ['.conf']
	private static String windowsHomePath = "${getenv('HOMEDRIVE')}${getenv('HOMEPATH')}"


	private static ConfigObject mergedCfg

	@Lazy  public static ConfigObject cfg = {
		if (mergedCfg == null) {
			mergedCfg = new ConfigObject()
			loadAll()
		}
		return mergedCfg
	}()

	public static String ensureEndSlash(String s) {
		if (s.endsWith(File.separator)) return s
		return s + File.separator
	}

	@Lazy public static List<File> lookupFolders = {
		[
			"${ensureEndSlash(getProperty('user.home'))}.local${File.separator}share${File.separator}Ankivocb",
			"${ensureEndSlash(windowsHomePath)}${File.separator}Ankivocb",
			"${ensureEndSlash(getProperty('user.dir'))}conf",
		].unique().findAll(ConfHelper.&isLookupFolderValid) as File[]
	}()

	public static boolean  isLookupFolderValid(String path) {
		if (!path) return false
		File f= new File(path)
		return f.exists() && f.isDirectory()
	}


	public static ConfigObject parseString(String cfgString, Map binding =[:]) {
		assert cfgString
		ConfigSlurper cfgSlurper = new ConfigSlurper()
		cfgSlurper.setBinding(binding)
		return cfgSlurper.parse(cfgString)
	}

	public static ConfigObject parseMap(Map<String, String> mapProps, Map binding =[:]) {
		ConfigSlurper cfgSlurper = new ConfigSlurper()
		Objects.requireNonNull(mapProps)
		Properties p = new Properties()
		p.putAll(mapProps)
		cfgSlurper.setBinding(binding)
		return cfgSlurper.parse(p)
	}

	public static ConfigObject loadConfig(String configName, Map binding = null) {
		String cfgStr = resolveRes( configName)?.text
		if (cfgStr == null) return null
		if (binding == null) binding = mergedCfg
		return ConfHelper.parseString(cfgStr, binding)
	}

	public  static ConfigObject loadAndMergeConfig(String configName, Map binding = null) {
		ConfigObject c = loadConfig(configName, binding)
		if (!c) return c
		if (mergedCfg != null) mergedCfg.merge(c)
		return c
	}

	public static BufferedInputStream resolveResExactName(String resName, File[] lookupPaths = []) {
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
			return [
				"conf/$resName",
				resName
			].collect{cl.getResourceAsStream(it)}.find {it}
		}.find {it}
		return is
	}

	public static BufferedInputStream resolveRes(String resName, File[] lookupPaths = lookupFolders) {
		resolveRes(resName, lookupPaths, this.&resolveResExactName)
	}

	public static BufferedInputStream resolveRes(String resName, File[] lookupPaths = lookupFolders, Closure cb) {
		(['']+ resExplicitExtensions).findResult {
			cb("$resName$it", lookupPaths)
		}
	}


	public static StringBuilder prettyPrintCfg(obj=mergedCfg, int level = 0, StringBuilder sb = new StringBuilder()) {
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


	public static loadAll() {
		assert loadAndMergeConfig("ankivocb-default") : "Failed to load the inbuilt default config"
		ConfigObject c=  loadAndMergeConfig("ankivocb")
		if (!c) System.print("Couldn't find the custom ankivocb.conf file. Using the default.\n")
	}


	public static void main(String[] args) {
		println getLookupFolders()
		println cfg.azure
		println prettyPrintCfg()
	}



}
