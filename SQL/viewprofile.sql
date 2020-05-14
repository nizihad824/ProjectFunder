select p.kennung, p.titel, p.beschreibung, p.status, p.finanzierungslimit, p.ersteller, p.vorgaenger, k.icon, b.name

from projekt p INNER JOIN benutzer b
on p.ersteller = b.email
inner join kategorie k on k.id = p.kategorie
left join (

select projekt as projektid, sum(spendenbetrag) as summe
from spenden
group by projekt

)

ON p.kennung = projektid;
