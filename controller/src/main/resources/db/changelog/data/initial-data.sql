CREATE OR REPLACE PROCEDURE initial_data()
AS
$proc$
DECLARE
BEGIN
    PERFORM id FROM roles WHERE name = 'User';
    IF NOT FOUND THEN
        INSERT INTO roles(name, created_at, updated_at) VALUES ('User', now(), now());
    END IF;

    PERFORM id FROM roles WHERE name = 'Admin';
    IF NOT FOUND THEN
        INSERT INTO roles(name, created_at, updated_at) VALUES ('Admin', now(), now());
    END IF;
END;
$proc$ LANGUAGE plpgsql;

CALL initial_data();
