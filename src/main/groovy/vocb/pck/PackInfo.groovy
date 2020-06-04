package vocb.pck

import groovy.transform.AutoClone
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import vocb.Helper


@Canonical
@CompileStatic
public class PackInfo {
	
	String name
	private String displayName
	private String backgroundName
	
	public String getDisplayName() {
		displayName?:name?.replaceAll(/(\p{Lu})(\p{L})/, ' $1$2')?.trim()
	}
	
	public void setDisplayName(String v) {
		displayName = v
	}
	
	public String getBackgroundName() {
		 "_${name}Background"
	}
	
	public void setBackgroundName(String v) {
		backgroundName = v
	}
	
	public String getUuid() {
		if (!name) return ""	
		String p = name.toLowerCase()*10
		assert p.length() > 30
		//return "ankivocb-${p[0..3]}-${p[4..7]}-${p[8..11]}-${p[11..22]}"
		return "ankivocb-2020-${p[0..21]}"
	}
		
}
