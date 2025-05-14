PROJEKTNA DOKUMENTACJA

IDEJA:
Neke vrste študentski servis, v katerem se lahko študentje prijavijo na različna dela. Omogočeno imajo, da so lahko prijavljeni na 2 dela hrkrati, ampak če je že nekdo prijavljen na neko temo se ne prijavi v njega. Dijaki imajo tudi možnost gledati nekaj svojih podatkov, in nalaganja osebne slike (oz. slik) na uvodni strani. Uporabnik se lahko na novo registrira, prijavi in pa hasshira se mu geslo. V teoriji je nov uporabnik vedno false, razen če se v bazi popravi.

Admin del:
Admin ima dostopno še en tab, ki se imenuje pregled tabel. V njemu admin vidi vse tabele, + ima dano da lahko dodaja delodajalca. Narejeno bi moglo tudi biti delete stavek, ki bi deloval na briši buttonu.

Podstrežniški podprogram:

create function dodaj_delo(p_naziv character varying, p_placilo integer, p_prosta_mesta integer, p_delodajalec_id integer, p_napotnica_id integer, p_placila_id integer, p_student_id integer DEFAULT NULL::integer) returns void
    language plpgsql
as
$$
BEGIN
    INSERT INTO delo (
        naziv, placilo, prosta_mesta,
        delodajalec_id, napotnica_id,
        placila_id, student_id
    ) VALUES (
                 p_naziv, p_placilo, p_prosta_mesta,
                 p_delodajalec_id, p_napotnica_id,
                 p_placila_id, p_student_id
             );
END;
$$;   // Dodaj delo

create function dodaj_delodajalca(p_ime_podjetja character varying, p_st_delavcev integer) returns void
    language plpgsql
as
$$
BEGIN
    INSERT INTO delodajalec (ime_podjetja, st_delavcev)
    VALUES (p_ime_podjetja, p_st_delavcev);
END;
$$; // Dodaj delodajalca

create function izbrisi_delodajalca(p_id_delodajalca integer) returns void
    language plpgsql
as
$$
BEGIN
    DELETE FROM delodajalec
    WHERE id = p_id_delodajalca;
END;
$$; // Izbriše delodajalca






