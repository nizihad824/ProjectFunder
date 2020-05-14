CREATE VIEW supported AS
SELECT kennung, titel, status, icon, spendenbetrag, spender, finanzierungslimit as limit
FROM projekt p
INNER JOIN spenden s ON p.kennung = s.projekt
INNER JOIN kategorie k ON p.kategorie = k.id
WHERE sichtbarkeit = 'oeffentlich';
