CREATE VIEW donation_section AS
select * from (
select spendenbetrag, projekt, 
CASE
	WHEN sichtbarkeit = 'privat' THEN 'Anonymous'
	ELSE name
END AS name
FROM spenden inner join benutzer
ON spender = email
ORDER BY spendenbetrag desc
);
