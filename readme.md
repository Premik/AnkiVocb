# Ankivob

- Zhruba 800 nejčastějších slov a slovních pojení.
- K anglickému slovu jedno nebo dvě české.
- U každého slova příklad užití ve větě.
- Téměř kažá kartička je opatřena obrázkem
- Kartičky obsahují tlačítko `hint` pro zobrazní nápovědy. Které odkryje jen začátek odpovědi a počet písmen.
- Vešekeré slova i věty jsou **namluvené** syntetizátorem. 
  - Vhodné pro děti co ještě neumí číst nebo čtou špatně.  
  - Pro angličtinu byl použit kvalitní Amazon Polly syntetizátor, který je podpořen neutronovými sítěma. 
  - Bylo použito namátkou pět různých hlasů, jak s UK tak US přízvukem.
  - Čeština je syntetizována pomocí staršího [systému Epos](http://epos.ufe.cz/).
- Pořadí karet v balíčku bylo **vyšlechtěné** zjednodušenou aplikací genetického alegoritmu tak, aby:
  - Slova s častějším výskytem byla dříve
  - Podobá slova byla dostatečně daleko od sebe. Srovnává se podobnost jak anglického slova, tak i jeho českého překladu(ů). Včetně jednoduchých přesmyček. U anglického slova se bere v potaz i fonetická podobnost.
  - Jednotlivá slova ze slovního spojení a věty  byly v balíčku dříve.
  - Obtížná slova byla s rovnoměrným rozestupem. Obtížnost se zatím odvozuje jen od delky slova:
    - Krátké, ale frekventované slova jsou obtížná.
    - Kartičky bez obrázku jsou obtížná.
    - Dluhá slova jsou obtížná.
