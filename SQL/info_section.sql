CREATE VIEW info_section AS
SELECT kennung,summe, titel, p.beschreibung, status, finanzierungslimit, ersteller, b.name, icon, vid, vtitel
FROM projekt p LEFT JOIN ( SELECT Projekt as pid, sum(spendenbetrag) as summe from spenden group by Projekt)
ON kennung = pid
INNER JOIN benutzer b ON 
ersteller = email
INNER JOIN kategorie ON
id = kategorie
LEFT JOIN (SELECT titel as vtitel, kennung as vid from projekt) ON
vorgaenger = vid;
