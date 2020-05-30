# Todo

## vocb

- Zkontrolovat i cs slova pomoci korpusu (preklepy)
- Dekompozice slov
- Pridat dvojslova (primarne korpus + examples?)

## Trizeni
- Podle hvezdicek, castejsi drive
- Podobnost 
 
- Dvojslova, prvni druhe drive. Druhe slovo blizko dvojslvi? 
- Kompozitni slova po jejich castech?
- Preferovat, aby slova z examples byly drive
- Data ferignTerm jako prvni field a image nakonec 
 
## sql
 select c.* from cards as c where nid = '1590322168665'
 
select c.id as cardId, c.nid as nodeId,  n.guid,c.ord,  n.tags,  n.flds from cards as c left join  notes as n on c.nid == n.id
select * from cards where id =  1590322168668
//1590322168668
 
