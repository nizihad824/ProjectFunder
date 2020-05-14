CREATE VIEW mainpage AS
SELECT email,kennung, b.name , titel, status, icon, sum(spendenbetrag)as summe
FROM projekt p LEFT JOIN benutzer b
ON b.email = p.ersteller
LEFT JOIN kategorie k ON k.id = p.kategorie
LEFT JOIN spenden s ON p.kennung = s.projekt
GROUP BY email, kennung, b.name, titel, status, icon ;
