CREATE TABLE kommentar (
	id SMALLINT NOT NULL GENERATED AS IDENTITY,
	text CLOB(1M) NOT NULL,
	datum timestamp DEFAULT CURRENT TIMESTAMP,
	sichtbarkeit VARCHAR(11) NOT NULL CHECK (sichtbarkeit IN ('oeffentlich', 'privat')),
	PRIMARY KEY (id)
);
