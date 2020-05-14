CREATE VIEW comment_section AS
SELECT * FROM (
SELECT text, projekt as kennung,
CASE
	WHEN sichtbarkeit = 'privat' THEN 'Anonymous'
	ELSE name
END AS name
FROM kommentar INNER JOIN schreibt
ON id = kommentar
INNER JOIN Benutzer ON 
Benutzer = email
ORDER BY datum desc
);
