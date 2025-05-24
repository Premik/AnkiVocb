package vocb.aws

import java.util.concurrent.TimeUnit

import vocb.Helper

public class AwsTranslate {

	String trn(String text, String srcLang="nl", String destLang="en") {
		List<String> cmd =[
			'aws',
			'translate',
			'translate-text',
			'--source-language-code',
			srcLang,
			'--target-language-code',
			destLang,
			'--text',
			text
		]

		println cmd
		Process p = cmd.execute()
		p.waitFor(5, TimeUnit.SECONDS)
		if (p.exitValue() != 0)  {
			Helper.printProcOut(p)
			throw new IllegalArgumentException("Error code ${p.exitValue()}.")
		}
		return Helper.parseJson(p.text)?.TranslatedText
	}



	static void main(String... args) {
		
		new AwsTranslate().tap {
			int i =nl.size()
			nl.collect { String nl->
				"${nl.padRight(55)} ${trn(nl)}"
			}.each {
				println it
			}			
		}
	}
	
List<String> nl =	'''
	aantal bijlagen
	aantal cent per km inz verreken
	aantal dag huur
	aantal dag inname mond
	aantal dag laatste inname mond
	aantal dag laatste mond
	aantal dag mond
	aantal dag na inname
	aantal dagen vervangend vervoer reparatie en onderhoud
	aantal dagen vervangend vervoer schade
	aantal dgn laatste mond
	aantal dgn mond
	aantal kalender dgn inname mond
	aantal kalender dgn laatste inname mond
	aantal kalender dgn laatste mond
	aantal kalender dgn mond
	aantal km verreken meer/minder
	aantal km vervangend vervoer reparatie en onderhoud
	aantal km vervangend vervoer schade
	aantal km volgens kontrakt
	aantal liters
	aantal liters benzine
	aantal liters diesel
	aantal liters lpg
	aantal meer km
	aantal mond inname korr periode
	aantal mond na inname
	aantal mond periode
	aantal schades niet verhaalbaar
	aantal schades totaal
	aantal schades verhaalbaar
	aantal vervangend vervoer reparatie en onderhoud
	aantal vervangend vervoer schades
	afdelingsnaam
	auto lease factuur adrescode afnemer
	auto lease factuur adrescode factuuradres
	auto lease factuur adrescode leverancier
	bedrag aanbetaling
	bedrag achterst rente
	bedrag afschr bpm
	bedrag afschr bpm al verreken
	bedrag afschr bpm nieuwe regeling
	bedrag afschr bpm nog te verreken
	bedrag afschr bpm ontvangen
	bedrag afschr exclusief bpm
	bedrag afschr voortijdige beëindiging
	bedrag assurantie te verreken
	bedrag berekening toeslag fiscale eenheid
	bedrag bijk kosten
	bedrag bijk kosten nieuw
	bedrag bijk kosten oud
	bedrag boekwaarde
	bedrag boete voortijdige beëindiging
	bedrag bpm
	bedrag bpm bij aanvang
	bedrag bpm heffing
	bedrag bpm overig
	bedrag brand
	bedrag brand niet kontrakt
	bedrag brand nieuw
	bedrag brand nog te verreken
	bedrag brand oud
	bedrag brand te verreken
	bedrag brandstof
	bedrag brandstof binnenlnd
	bedrag brandstof buitenlnd
	bedrag brandstof mond
	bedrag brandstof verschil
	bedrag brandstof vervangend vervoer binnenlnd
	bedrag brandstof vervangend vervoer buitenlnd
	bedrag brandstof volg mond
	bedrag brengkstn
	bedrag brstf btw belast
	bedrag brstf buitenland
	bedrag brstf getankt exclusief
	bedrag brstf verreken dgn na inname
	bedrag brstf verreken mond na inname
	bedrag btw belast
	bedrag btw btw belast
	bedrag btw vrij
	bedrag bufferrente
	bedrag bufferrente uit te factureren
	bedrag casco expert
	bedrag casco geschat
	bedrag dekking
	bedrag deze verrekening
	bedrag diverse
	bedrag eigen risico
	bedrag eigen risico casco
	bedrag eigen risico schade
	bedrag er schade berekening
	bedrag er te verreken
	bedrag exploitatie result
	bedrag exploitatie result sale and lease back
	bedrag exploitatie saldo
	bedrag extra afschr bpm
	bedrag faktuur achterst rente
	bedrag haalkstn
	bedrag handelswaarde
	bedrag houderschapbelasting vrij
	bedrag huur
	bedrag huur al verreken
	bedrag huur en km te verreken
	bedrag huur nieuw
	bedrag huur oud
	bedrag huur te corrigeren
	bedrag huur te verreken
	bedrag huurtar mond per te verreken
	bedrag huurtar mond te verreken
	bedrag huurtarief
	bedrag huurtarief mond
	bedrag huurtarief mond periode
	bedrag km al verreken
	bedrag km nog te verreken
	bedrag km te verreken
	bedrag korting huur
	bedrag kosten
	bedrag kosten administratie
	bedrag kosten administratie beschik
	bedrag kosten administratie beschik hr
	bedrag kosten administratie brstfpas
	bedrag kosten administratie overig
	bedrag kosten annulering
	bedrag kosten assurantie
	bedrag kosten assurantie overig
	bedrag kosten casco
	bedrag kosten diverse overig
	bedrag kosten houderschapbelasting
	bedrag kosten ivm aftanken
	bedrag kosten kenteken
	bedrag kosten keuring
	bedrag kosten niet lease
	bedrag kosten niet lease overig
	bedrag kosten spoedreservering
	bedrag kosten veiling
	bedrag kosten veiling overig
	bedrag kosten veilng overig
	bedrag lease tarief
	bedrag na aanbet
	bedrag na aanvll dienst
	bedrag na administratie kosten
	bedrag na afschr
	bedrag na afschr bpm
	bedrag na banden
	bedrag na brstf binnenlnd
	bedrag na brstf buitenlnd
	bedrag na calculatie afschr
	bedrag na calculatie auto kostprijs
	bedrag na calculatie opbrengst verk
	bedrag na houderschapbelasting
	bedrag na keuring
	bedrag na kleine schades
	bedrag na management fee
	bedrag na rente
	bedrag na reparatie en onderhoud
	bedrag na reserveren er
	bedrag na vervangend vervoer
	bedrag na verz casco
	bedrag na verz overig
	bedrag nagek post
	bedrag nagekomen post
	bedrag naheffing bpm
	bedrag prolongatie laatste mond
	bedrag prolongatie mond
	bedrag prolongatie volg mond
	bedrag rente achterst rente
	bedrag rente negatief
	bedrag rente op invest
	bedrag rest bpm grijs hoog (btw)
	bedrag rest bpm grijs nul
	bedrag result restwaarde boekwaarde
	bedrag result restwaarde opbrengst verk
	bedrag sanctie
	bedrag schade afh eig wp
	bedrag schade afh te verreken
	bedrag schade beneden er
	bedrag schade bij inname
	bedrag subtotaal
	bedrag toesl mond
	bedrag toesl verreken dgn na inname
	bedrag toesl verreken laatste mond
	bedrag toesl verreken mond na inname
	bedrag toesl verreken mond periode
	bedrag toeslag
	bedrag toeslag laatste mond
	bedrag toeslag mond
	bedrag toeslag nieuw
	bedrag toeslag oud
	bedrag toeslag verschil
	bedrag toeslag volg mond
	bedrag totaal brandstof
	bedrag totaal declaratie
	bedrag totaal nacalculatie
	bedrag totaal result restwaarde
	bedrag totaal te verrekenen
	bedrag totaal verreken
	bedrag totaal verschil
	bedrag totaal voorcalculatie
	bedrag totaal voorsch
	bedrag transport
	bedrag verkoopprijs
	bedrag verkoopprijs overig
	bedrag verkoopprs overig
	bedrag verreken aanbet
	bedrag verreken aantal dag na inname
	bedrag verreken aantal mond na inname
	bedrag verreken aantal mond periode
	bedrag verreken aanvll dienst
	bedrag verreken administratie kosten
	bedrag verreken afschr
	bedrag verreken afschr bpm
	bedrag verreken banden
	bedrag verreken brstf binnenlnd
	bedrag verreken brstf buitenlnd
	bedrag verreken calc versch
	bedrag verreken houderschapbelasting
	bedrag verreken keuring
	bedrag verreken kleine schades
	bedrag verreken laatste mond
	bedrag verreken management fee
	bedrag verreken meer/minder
	bedrag verreken meer/minder oud
	bedrag verreken mond
	bedrag verreken nagekomen post
	bedrag verreken rente
	bedrag verreken reparatie en onderhoud
	bedrag verreken reserveren er
	bedrag verreken vervangend vervoer
	bedrag verreken verz casco
	bedrag verreken verz overig
	bedrag verreken volg mond
	bedrag versch aanbet
	bedrag versch aanvll dienst
	bedrag versch administratie kosten
	bedrag versch afschr
	bedrag versch afschr bpm
	bedrag versch banden
	bedrag versch brstf binnenlnd
	bedrag versch brstf buitenlnd
	bedrag versch calc versch
	bedrag versch houderschapbelasting
	bedrag versch keuring
	bedrag versch kleine schades
	bedrag versch management fee
	bedrag versch rente
	bedrag versch reparatie en onderhoud
	bedrag versch reserveren er
	bedrag versch vervangend vervoer
	bedrag versch verz casco
	bedrag versch verz overig
	bedrag verschil prolongatie mond
	bedrag verschil prolongatie volg mond
	bedrag voor aanbet
	bedrag voor aanvll dienst
	bedrag voor administratie kosten
	bedrag voor afschr
	bedrag voor afschr bpm
	bedrag voor banden
	bedrag voor brstf binnenlnd
	bedrag voor brstf buitenlnd
	bedrag voor calc versch
	bedrag voor houderschapbelasting
	bedrag voor keuring
	bedrag voor kleine schades
	bedrag voor management fee
	bedrag voor rente
	bedrag voor reparatie en onderhoud
	bedrag voor reserveren er
	bedrag voor vervangend vervoer
	bedrag voor verz casco
	bedrag voor verz overig
	bedrag voortijdige beëindiging te verreken
	beschikkingsnr
	bestelnr
	betalings wijze tekst
	boekstuknr
	btw perc hoog
	btw perc laag
	btw perc nul
	code btw aanbetaling
	code btw afschr bpm
	code btw afschr exclusief bpm
	code btw afschr voortijdige beëindiging
	code btw assurantie te verreken
	code btw berekening toeslag fiscale eenheid
	code btw boete voortijdige beëindiging
	code btw bpm
	code btw bpm heffing
	code btw bpm overig
	code btw brand niet kontrakt
	code btw brand nog te verreken
	code btw brand te verreken
	code btw brandstof
	code btw diverse
	code btw eigen risico
	code btw eigen risico casco
	code btw eigen risico schade
	code btw er schade berekening
	code btw extra afschr bpm
	code btw huur en km te ver
	code btw huur te corrigern
	code btw huur te verreken
	code btw km nog te verreken
	code btw kosten administratie
	code btw kosten administratie beschik
	code btw kosten administratie beschikking hr
	code btw kosten administratie overig
	code btw kosten assurantie
	code btw kosten houderschapbelasting
	code btw kosten kenteken
	code btw kosten keuring
	code btw kosten niet lease
	code btw kosten niet lease overig
	code btw kosten veiling
	code btw kosten veiling overig
	code btw kosten veilng overig
	code btw lease tarief
	code btw naheffing bpm
	code btw rente negatief
	code btw rente op invest
	code btw rest bpm grijs hoog (btw)
	code btw rest bpm grijs nul
	code btw sanctie
	code btw schade afh eig wp
	code btw schade afh te verreken
	code btw schade beneden er
	code btw schade bij inname
	code btw toeslag
	code btw totaal
	code btw transport
	code btw verkoopprijs
	code btw verkoopprijs overig
	code btw verkoopprs overig
	code btw voortijdige beëindiging te verreken
	dagen achterstand
	datum betaal
	datum deel i
	datum eind brandstoflease
	datum factuur tot en met
	datum faktuur
	datum getankt
	datum inclusief brandstoflease
	datum inname definitief
	datum inname nieuw
	datum inname oud
	datum inzet definitief
	datum inzet nieuw
	datum inzet oud
	datum laatste getankt
	datum laatste verreken meer/minder
	datum mutatie
	datum pleging
	datum schade
	datum totaal
	datum van
	datum verkoop
	datum verrekening
	datum verval
	datum verval betaaltermijn
	datum verwerk
	datum wijz
	debiteuren nummer
	debiteuren nummer onguard
	detail kenteken volgnr
	detail verhuur inzet volgnr
	extern uitgaand faktuurnr
	faktureermaand
	faktuur data detail volgnr
	faktuur data opslag id
	faktuur nummer onguard
	faktuurrun nummer
	herkomst brandstof id
	huurkenteken
	huurklasse
	indicator btw
	indicator buitenland
	indicator kostenplaats
	indicator sale lease back
	indicator soort totaal
	indicator subtotalen dochter
	indicator vrijgave voor uitlevering
	inkomend extern faktuurnr
	intern uitgaand faktuurnr
	jaarkm berekend
	kenteken
	klant
	klant afdeling
	klant postcode woonplaats
	klant straat
	klantnaam
	km gereden
	km gereden laatste mond
	km gereden mond
	km gereden mond periode
	km kontraktueel toegestaan
	km te verreken
	km totaal
	km trage
	km werkelijk
	kmstand berekend
	kmstand getankt
	kmstand inname
	kmstand korr
	kmstand start
	kode vervangend vervoer
	kontraktnr
	korr bedrag accessoire
	korr bedrag toeslag
	korrektie te printen factuur nummer extern
	kostenplaats
	land voluit
	lease tarief
	lease tarief nieuw
	lease tarief oud
	lease tarief verschil
	looptijd
	looptijd werkelijk
	medew mutatie id
	merk model type
	mutatie nummer
	naam bedrijf
	naam berijder
	naam bestuurder
	naam eigenaar
	naam schade bestuurder
	naam tav
	omschr aanbet
	omschr aanbetaling
	omschr aanvll dienst
	omschr achterst rente
	omschr administratie kosten
	omschr afschr
	omschr afschr bpm
	omschr afschr exclusief bpm
	omschr afschr voortijdige beëindiging
	omschr assurantie te verreken
	omschr banden
	omschr berekening toeslag fiscale eenheid
	omschr boete voortijdige beëindiging
	omschr bpm
	omschr bpm heffing
	omschr bpm overig
	omschr brand niet kontrakt
	omschr brand nog te verreken
	omschr brand te verreken
	omschr brandstof
	omschr brstf binnenlnd
	omschr brstf buitenland
	omschr brstf buitenlnd
	omschr bufferrente
	omschr bufferrente uit te factureren
	omschr calc versch
	omschr datum totaal
	omschr diverse
	omschr eigen risico
	omschr eigen risico casco
	omschr eigen risico schade
	omschr er schade berekening
	omschr er te verreken
	omschr exploitatie result
	omschr exploitatie result sale and lease back
	omschr extra afschr bpm
	omschr houderschapbelasting
	omschr huur en km te verreken
	omschr huur te corrigeren
	omschr huur te verreken
	omschr huurcategorie
	omschr keuring
	omschr kleine schades
	omschr km nog te verreken
	omschr kosten administratie
	omschr kosten administratie beschik
	omschr kosten administratie beschik hr
	omschr kosten administratie overig
	omschr kosten assurantie
	omschr kosten assurantie overig
	omschr kosten casco
	omschr kosten houderschapbelasting
	omschr kosten kenteken
	omschr kosten keuring
	omschr kosten niet lease
	omschr kosten niet lease overig
	omschr kosten veiling
	omschr kosten veiling overig
	omschr kosten veilng overig
	omschr lease tarief
	omschr management fee
	omschr nagek post
	omschr naheffing bpm
	omschr rente
	omschr rente negatief
	omschr rente op invest
	omschr reparatie en onderhoud
	omschr reserveren er
	omschr rest bpm grijs hoog (btw)
	omschr rest bpm grijs nul
	omschr sanctie
	omschr schade aard
	omschr schade afh eig wp
	omschr schade afh te verreken
	omschr schade beneden er
	omschr schade bij inname
	omschr schade oorzaak
	omschr soort faktuur
	omschr toeslag
	omschr transport
	omschr verkoopprijs
	omschr verkoopprijs overig
	omschr verkoopprs overig
	omschr vervangend vervoer
	omschr verz casco
	omschr verz overig
	omschr voortijdige beëindiging te verreken
	pas nummer
	perc achterst rente
	perc afwijking jaarkm
	perc btw aanbetaling
	perc btw achterst rente
	perc btw afschr bpm
	perc btw afschr exclusief bpm
	perc btw afschr voortijdige beëindiging
	perc btw assurantie te verreken
	perc btw berekening toeslag fiscale eenheid
	perc btw boete voortijdige beëindiging
	perc btw bpm
	perc btw bpm heffing
	perc btw bpm overig
	perc btw brand niet kontrakt
	perc btw brand nog te verreken
	perc btw brand te verreken
	perc btw brandstof
	perc btw brstf buitenland
	perc btw bufferrente
	perc btw bufferrente uit te factureren
	perc btw diverse
	perc btw eigen risico
	perc btw eigen risico casco
	perc btw eigen risico schade
	perc btw er schade berekening
	perc btw er te verreken
	perc btw exploitatie result
	perc btw exploitatie result sale and lease back
	perc btw extra afschr bpm
	perc btw hoog
	perc btw hoog totaal
	perc btw huur en km te ver
	perc btw huur te corrigern
	perc btw huur te verreken
	perc btw km nog te verreken
	perc btw kosten administratie
	perc btw kosten administratie beschik
	perc btw kosten administratie beschikking hr
	perc btw kosten administratie overig
	perc btw kosten assurantie
	perc btw kosten assurantie overig
	perc btw kosten casco
	perc btw kosten houderschapbelasting
	perc btw kosten kenteken
	perc btw kosten keuring
	perc btw kosten niet lease
	perc btw kosten niet lease overig
	perc btw kosten veiling
	perc btw kosten veiling overig
	perc btw kosten veilng overig
	perc btw laag
	perc btw laag totaal
	perc btw lease tarief
	perc btw nagek post
	perc btw naheffing bpm
	perc btw nul
	perc btw nul totaal
	perc btw rente negatief
	perc btw rente op invest
	perc btw rest bpm grijs hoog (btw)
	perc btw rest bpm grijs nul
	perc btw sanctie
	perc btw schade afh eig wp
	perc btw schade afh te verreken
	perc btw schade beneden er
	perc btw schade bij inname
	perc btw toeslag
	perc btw totaal
	perc btw transport
	perc btw verkoopprijs
	perc btw verkoopprijs overig
	perc btw verkoopprs overig
	perc btw voortijdige beëindiging te verreken
	perc bufferrente
	perc korting huurtarief
	perc verreken
	perc verreken aanbet
	perc verreken aanvll dienst
	perc verreken administratie kosten
	perc verreken afschr
	perc verreken afschr bpm
	perc verreken banden
	perc verreken brstf binnenlnd
	perc verreken brstf buitenlnd
	perc verreken calc versch
	perc verreken houderschapbelasting
	perc verreken keuring
	perc verreken kleine schades
	perc verreken management fee
	perc verreken nagekomen post
	perc verreken rente
	perc verreken reparatie en onderhoud
	perc verreken reserveren er
	perc verreken vervangend vervoer
	perc verreken verz casco
	perc verreken verz overig
	plaats eigenaar
	plaats ongeval
	plaats voluit
	prijs meer km
	prijs meer/minder km
	prijs minder km
	prijs per dag
	prijs per km
	produkt
	reden inzet verhuur
	reden wijziging
	relatie id
	schade formulier
	schade verhaalbaar
	schadenr
	soort factuur id
	soort nagekomen post
	specificatie
	straat voluit
	subtotaal volgnr
	tankstation
	tekst
	tekst brandstof kosten
	tekst brandstof pas
	tekst faktuur
	tekst toelichting bijk kosten
	tekst verhuur info
	telefoonnr
	tijdstip ongeval
	totaal aantal dag huur
	totaal aantal km verreken
	totaal aantal schades
	totaal aantal verbr km
	totaal bedrag
	totaal bedrag acht rente debiteuren nummer rente
	totaal bedrag achterst rente
	totaal bedrag bijk kosten
	totaal bedrag brand
	totaal bedrag brand al verreken
	totaal bedrag brand declaratie
	totaal bedrag brand oud
	totaal bedrag brand verreken
	totaal bedrag brand voorsch
	totaal bedrag brndstf versch
	totaal bedrag brstf btw belast
	totaal bedrag brstf exclusief
	totaal bedrag btw
	totaal bedrag btw belast
	totaal bedrag btw btw belast
	totaal bedrag btw hoog
	totaal bedrag btw laag
	totaal bedrag btw nul
	totaal bedrag btw vrij
	totaal bedrag bufferrente
	totaal bedrag detail
	totaal bedrag detail inclusief btw
	totaal bedrag exclusief btw
	totaal bedrag exclusief toeslag
	totaal bedrag exploitatie saldo
	totaal bedrag getankt met brstfpas
	totaal bedrag grondslag hoog
	totaal bedrag grondslag laag
	totaal bedrag grondslag nul
	totaal bedrag houderschapbelasting vrij
	totaal bedrag huur
	totaal bedrag huur al verreken
	totaal bedrag huur inclusief korting
	totaal bedrag huur te verreken
	totaal bedrag huurkontrakt
	totaal bedrag inclusief btw
	totaal bedrag inclusief btw exclusief toeslag
	totaal bedrag kenteken
	totaal bedrag korting huur
	totaal bedrag leasetarief
	totaal bedrag nagek post verreken
	totaal bedrag overige exclusief
	totaal bedrag verreken
	totaal bedrag verreken totaal
	totaal bedrag verreken verreken
	totaal bedrag versch prolongatie totaal per
	totaal bedrag versch toesl totaal per
	totaal deb bedrag brand declaratie
	totaal deb bedrag brand totaal verreken
	totaal deb bedrag brand totaal voorsc
	totaal km
	totaal km gereden
	totaal km huur vrij
	totaal km vervangend vervoer gereden
	totaal lease tarief nieuw
	totaal lease tarief oud
	totaal liters
	vervallen
	vervangend vervoer tijdelijk vervoer nummer
	verz faktuur detail volgnr
	verz faktuur opslag id
	werk km trage
	woonpl berijder
'''.split('\n')
	.collect {it.trim()}
	.findAll()	
	
}
