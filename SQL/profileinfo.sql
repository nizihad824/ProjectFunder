--CREATE VIEW profileinfo as SELECT b.name, b.email, count(spender) as funding, count(ersteller) as created
--FROM benutzer b
--LEFT JOIN spenden s ON b.email = s.spender
--LEFT JOIN projekt p ON p.ersteller = b.email
--GROUP BY b.name, b.email;
Create VIEW profileinfo as 
SELECT b.name, b.email, funding, created
FROM benutzer b 
LEFT JOIN (SELECT spender as s, count(spender) as funding from spenden group by spender)
ON b.email = s
LEFT JOIN (SELECT ersteller as e, count(ersteller) as created from projekt group by ersteller)
ON b.email = e;
