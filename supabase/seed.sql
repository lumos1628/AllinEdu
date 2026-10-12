-- Semilla sintética de desarrollo. La configuración y la contraseña vienen del
-- entorno local; este archivo no contiene correos ni contraseñas reales o fijas.
\set ON_ERROR_STOP on
\getenv demo_students_per_section EDUFAST_DEMO_STUDENTS_PER_SECTION
\getenv demo_sibling_pairs EDUFAST_DEMO_SIBLING_PAIRS
\getenv demo_email_domain EDUFAST_DEMO_EMAIL_DOMAIN
\getenv demo_password EDUFAST_DEMO_PASSWORD
\getenv demo_school_year EDUFAST_DEMO_SCHOOL_YEAR
\getenv demo_start_date EDUFAST_DEMO_START_DATE
\getenv demo_end_date EDUFAST_DEMO_END_DATE

SELECT length(:'demo_password') > 0 AS demo_password_configured \gset
\if :demo_password_configured
\else
\echo 'Configura EDUFAST_DEMO_PASSWORD en tu entorno local antes de ejecutar la semilla.'
\quit
\endif

SELECT extensions.crypt(:'demo_password', extensions.gen_salt('bf', 10)) AS demo_password_hash \gset
\unset demo_password

BEGIN;

CREATE TEMP TABLE edufast_demo_config (
    students_per_section integer not null,
    sibling_pairs integer not null,
    email_domain text not null,
    school_year integer not null,
    start_date date not null,
    end_date date not null,
    password_hash text not null
) ON COMMIT DROP;

INSERT INTO edufast_demo_config
    (students_per_section, sibling_pairs, email_domain, school_year,
     start_date, end_date, password_hash)
VALUES
    (:'demo_students_per_section'::integer,
     :'demo_sibling_pairs'::integer,
     lower(:'demo_email_domain'),
     :'demo_school_year'::integer,
     :'demo_start_date'::date,
     :'demo_end_date'::date,
     :'demo_password_hash');

CREATE TEMP TABLE edufast_demo_students (
    student_id bigint primary key,
    grade_code text not null,
    section_name text not null,
    student_number integer not null,
    student_code text not null unique
) ON COMMIT DROP;

CREATE TEMP TABLE edufast_demo_sibling_map (
    student_code text primary key,
    family_key text not null
) ON COMMIT DROP;

CREATE TEMP TABLE edufast_demo_families (
    student_id bigint primary key,
    family_key text not null
) ON COMMIT DROP;

CREATE TEMP TABLE edufast_demo_guardians (
    family_key text primary key,
    apoderado_id bigint not null,
    cuenta_usuario_id bigint not null,
    correo text not null unique
) ON COMMIT DROP;

CREATE TEMP TABLE edufast_demo_staff (
    staff_code text primary key,
    cuenta_usuario_id bigint not null unique
) ON COMMIT DROP;

DO $$
DECLARE
    cfg edufast_demo_config%rowtype;
    institution_id bigint;
    school_year_id bigint;
    section record;
    student_number integer;
    student_code text;
    person_id bigint;
    student_id bigint;
    enrollment_id bigint;
    max_sibling_pairs integer;
BEGIN
    SELECT * INTO STRICT cfg FROM edufast_demo_config;

    IF cfg.students_per_section < 1 OR cfg.students_per_section > 100 THEN
        RAISE EXCEPTION 'EDUFAST_DEMO_STUDENTS_PER_SECTION debe estar entre 1 y 100';
    END IF;
    IF cfg.sibling_pairs < 0 THEN
        RAISE EXCEPTION 'EDUFAST_DEMO_SIBLING_PAIRS no puede ser negativo';
    END IF;
    IF cfg.email_domain !~ '^[a-z0-9.-]+$' THEN
        RAISE EXCEPTION 'EDUFAST_DEMO_EMAIL_DOMAIN no tiene un formato válido';
    END IF;
    IF cfg.end_date < cfg.start_date THEN
        RAISE EXCEPTION 'La fecha de fin del año escolar debe ser posterior a la de inicio';
    END IF;

    SELECT (count(*) / 2) * 2 * cfg.students_per_section
    INTO max_sibling_pairs
    FROM public.grados_educativos;
    IF cfg.sibling_pairs > max_sibling_pairs THEN
        RAISE EXCEPTION 'Hay % combinaciones disponibles para hermanos; se solicitaron %',
            max_sibling_pairs, cfg.sibling_pairs;
    END IF;

    INSERT INTO public.instituciones_educativas
        (codigo_modular, codigo_local, nombre, tipo_gestion)
    VALUES
        ('DEMO-EDUFAST', 'DEMO-LOCAL', 'Institución Educativa Demo', 'PUBLICA')
    ON CONFLICT (codigo_modular) DO UPDATE
        SET nombre = excluded.nombre
    RETURNING id INTO institution_id;

    INSERT INTO public.anios_escolares
        (institucion_id, anio, fecha_inicio, fecha_fin, estado)
    VALUES
        (institution_id, cfg.school_year, cfg.start_date, cfg.end_date, 'ACTIVO')
    ON CONFLICT (institucion_id, anio) DO UPDATE
        SET fecha_inicio = excluded.fecha_inicio,
            fecha_fin = excluded.fecha_fin
    RETURNING id INTO school_year_id;

    INSERT INTO public.secciones
        (anio_escolar_id, institucion_id, grado_id, nombre, capacidad_maxima, activa)
    SELECT school_year_id, institution_id, g.id, s.nombre,
           cfg.students_per_section, true
    FROM public.grados_educativos g
    CROSS JOIN (VALUES ('A'), ('B')) AS s(nombre)
    ON CONFLICT (institucion_id, anio_escolar_id, grado_id, nombre) DO UPDATE
        SET capacidad_maxima = excluded.capacidad_maxima,
            activa = true;

    FOR section IN
        SELECT s.id AS section_id,
               s.nombre AS section_name,
               g.codigo AS grade_code
        FROM public.secciones s
        JOIN public.grados_educativos g ON g.id = s.grado_id
        JOIN public.niveles_educativos n ON n.id = g.nivel_id
        WHERE s.anio_escolar_id = school_year_id
          AND s.institucion_id = institution_id
        ORDER BY n.id, g.numero, s.nombre
    LOOP
        FOR student_number IN 1..cfg.students_per_section LOOP
            student_code := format('DEMO-%s-%s-%s', section.grade_code,
                                   section.section_name, lpad(student_number::text, 3, '0'));

            INSERT INTO public.personas
                (nombres, apellidos, tipo_documento, numero_documento)
            VALUES ('Estudiante', student_code, 'CODIGO_DEMO', student_code)
            ON CONFLICT (tipo_documento, numero_documento) DO UPDATE
                SET apellidos = excluded.apellidos
            RETURNING id INTO person_id;

            INSERT INTO public.estudiantes (persona_id, codigo_estudiante)
            VALUES (person_id, student_code)
            ON CONFLICT (codigo_estudiante) DO UPDATE
                SET persona_id = excluded.persona_id
            RETURNING id INTO student_id;

            INSERT INTO public.matriculas
                (estudiante_id, anio_escolar_id, institucion_id, estado)
            VALUES (student_id, school_year_id, institution_id, 'ACTIVA')
            ON CONFLICT (estudiante_id, anio_escolar_id) DO UPDATE
                SET estado = 'ACTIVA'
            RETURNING id INTO enrollment_id;

            INSERT INTO public.ubicaciones_matricula
                (matricula_id, seccion_id, fecha_inicio, motivo)
            VALUES (enrollment_id, section.section_id, cfg.start_date, 'MATRICULA_INICIAL')
            ON CONFLICT (matricula_id, fecha_inicio) DO NOTHING;

            INSERT INTO edufast_demo_students
                (student_id, grade_code, section_name, student_number, student_code)
            VALUES (student_id, section.grade_code,
                    section.section_name, student_number, student_code);
        END LOOP;
    END LOOP;
END $$;

WITH cfg AS (
    SELECT sibling_pairs, students_per_section FROM edufast_demo_config
), grades AS (
    SELECT g.codigo,
           row_number() OVER (ORDER BY n.id, g.numero)::integer AS grade_order
    FROM public.grados_educativos g
    JOIN public.niveles_educativos n ON n.id = g.nivel_id
), candidates AS (
    SELECT row_number() OVER (ORDER BY g1.grade_order, s.nombre, n.student_number)::integer AS pair_number,
           g1.codigo AS older_grade,
           g2.codigo AS younger_grade,
           s.nombre AS section_name,
           n.student_number
    FROM grades g1
    JOIN grades g2 ON g2.grade_order = g1.grade_order + 1
    CROSS JOIN (VALUES ('A'), ('B')) AS s(nombre)
    CROSS JOIN cfg
    CROSS JOIN LATERAL generate_series(1, cfg.students_per_section) AS n(student_number)
    WHERE g1.grade_order % 2 = 1
), selected AS (
    SELECT *, 'HERMANOS-' || lpad(pair_number::text, 4, '0') AS family_key
    FROM candidates
    WHERE pair_number <= (SELECT sibling_pairs FROM cfg)
)
INSERT INTO edufast_demo_sibling_map (student_code, family_key)
SELECT format('DEMO-%s-%s-%s', older_grade, section_name, lpad(student_number::text, 3, '0')),
       family_key
FROM selected
UNION ALL
SELECT format('DEMO-%s-%s-%s', younger_grade, section_name, lpad(student_number::text, 3, '0')),
       family_key
FROM selected;

INSERT INTO edufast_demo_families (student_id, family_key)
SELECT student.student_id,
       coalesce(sibling.family_key, 'ALUMNO-' || student.student_code)
FROM edufast_demo_students student
LEFT JOIN edufast_demo_sibling_map sibling ON sibling.student_code = student.student_code;

DO $$
DECLARE
    cfg edufast_demo_config%rowtype;
    family record;
    person_id bigint;
    account_id bigint;
    guardian_id bigint;
    family_number integer := 0;
    guardian_email text;
    document_code text;
    apoderado_role_id smallint;
    institution_id bigint;
    school_year_id bigint;
BEGIN
    SELECT * INTO STRICT cfg FROM edufast_demo_config;
    SELECT id INTO STRICT institution_id
    FROM public.instituciones_educativas WHERE codigo_modular = 'DEMO-EDUFAST';
    SELECT id INTO STRICT school_year_id
    FROM public.anios_escolares
    WHERE institucion_id = institution_id AND anio = cfg.school_year;
    SELECT id INTO STRICT apoderado_role_id FROM public.roles WHERE codigo = 'APODERADO';

    FOR family IN
        SELECT family_key FROM edufast_demo_families GROUP BY family_key ORDER BY family_key
    LOOP
        family_number := family_number + 1;
        guardian_email := format('apoderado.%s@%s',
            lower(regexp_replace(family.family_key, '[^a-zA-Z0-9]+', '.', 'g')),
            cfg.email_domain);
        document_code := 'DEMO-APO-' || substr(md5(family.family_key), 1, 16);

        INSERT INTO public.personas
            (nombres, apellidos, tipo_documento, numero_documento)
        VALUES ('Apoderado', 'Familia Demo ' || lpad(family_number::text, 4, '0'),
                'CODIGO_DEMO', document_code)
        ON CONFLICT (tipo_documento, numero_documento) DO UPDATE
            SET apellidos = excluded.apellidos
        RETURNING id INTO person_id;

        INSERT INTO public.cuentas_usuario
            (persona_id, correo, password_hash, estado)
        VALUES (person_id, guardian_email, cfg.password_hash, 'ACTIVA')
        ON CONFLICT (correo) DO UPDATE
            SET password_hash = excluded.password_hash
        RETURNING id INTO account_id;

        INSERT INTO public.apoderados (persona_id, cuenta_usuario_id)
        VALUES (person_id, account_id)
        ON CONFLICT (persona_id) DO UPDATE
            SET cuenta_usuario_id = excluded.cuenta_usuario_id
        RETURNING id INTO guardian_id;

        INSERT INTO public.contactos_persona
            (persona_id, tipo, valor, es_principal, activo)
        VALUES (person_id, 'CORREO', guardian_email, true, true)
        ON CONFLICT (persona_id, tipo, valor) DO NOTHING;

        INSERT INTO public.asignaciones_roles
            (cuenta_usuario_id, rol_id, institucion_id, anio_escolar_id, vigente_desde)
        SELECT account_id, apoderado_role_id, institution_id, school_year_id, cfg.start_date
        WHERE NOT EXISTS (
            SELECT 1 FROM public.asignaciones_roles ar
            WHERE ar.cuenta_usuario_id = account_id
              AND ar.rol_id = apoderado_role_id
              AND ar.institucion_id = institution_id
              AND ar.anio_escolar_id = school_year_id
              AND ar.vigente_hasta IS NULL
        );

        INSERT INTO edufast_demo_guardians (family_key, apoderado_id, cuenta_usuario_id, correo)
        VALUES (family.family_key, guardian_id, account_id, guardian_email)
        ON CONFLICT (family_key) DO UPDATE
            SET apoderado_id = excluded.apoderado_id,
                cuenta_usuario_id = excluded.cuenta_usuario_id,
                correo = excluded.correo;
    END LOOP;
END $$;

INSERT INTO public.vinculos_apoderado_estudiante
    (apoderado_id, estudiante_id, parentesco, puede_consultar, recibe_notificaciones,
     vigente_desde, verificado_at)
SELECT guardian.apoderado_id, family.student_id, 'FAMILIAR', true, false,
       cfg.start_date, now()
FROM edufast_demo_families family
JOIN edufast_demo_guardians guardian USING (family_key)
CROSS JOIN edufast_demo_config cfg
ON CONFLICT (apoderado_id, estudiante_id, vigente_desde) DO NOTHING;

DO $$
DECLARE
    cfg edufast_demo_config%rowtype;
    staff record;
    person_id bigint;
    account_id bigint;
    role_id smallint;
    level_id smallint;
    supervisor_id bigint;
    email text;
    document_code text;
    institution_id bigint;
    school_year_id bigint;
    section_id bigint;
BEGIN
    SELECT * INTO STRICT cfg FROM edufast_demo_config;
    SELECT id INTO STRICT institution_id
    FROM public.instituciones_educativas WHERE codigo_modular = 'DEMO-EDUFAST';
    SELECT id INTO STRICT school_year_id
    FROM public.anios_escolares
    WHERE institucion_id = institution_id AND anio = cfg.school_year;
    SELECT s.id INTO STRICT section_id
    FROM public.secciones s
    JOIN public.grados_educativos g ON g.id = s.grado_id
    WHERE s.institucion_id = institution_id
      AND s.anio_escolar_id = school_year_id
      AND g.codigo = 'PRI-04'
      AND s.nombre = 'A';

    FOR staff IN
        SELECT * FROM (VALUES
            ('DOCENTE', 'DOCENTE_REGULAR', 'Docente regular', NULL::text, NULL::text, true, 'DOCENTE'),
            ('DOCENTE', 'DOCENTE_AUXILIAR', 'Docente auxiliar', NULL::text, NULL::text, false, 'AUXILIAR'),
            ('DIRECTOR', 'DIRECTOR_PRIMARIA', 'Director de Primaria', 'PRIMARIA', NULL::text, false, NULL::text),
            ('DIRECTOR', 'DIRECTOR_GENERAL', 'Director General', NULL::text, NULL::text, false, NULL::text),
            ('SECRETARIA', 'SECRETARIA', 'Secretaría', NULL::text, 'DIRECTOR_GENERAL', false, NULL::text)
        ) AS staff(role_code, staff_code, display_name,
                   level_code, supervisor_code, is_tutor, teacher_function)
    LOOP
        email := format('personal.%s@%s',
            lower(replace(staff.staff_code, '_', '.')), cfg.email_domain);
        document_code := 'DEMO-PER-' || substr(md5(staff.staff_code), 1, 16);

        INSERT INTO public.personas
            (nombres, apellidos, tipo_documento, numero_documento)
        VALUES (staff.display_name, 'Personal Demo EduFast', 'CODIGO_DEMO', document_code)
        ON CONFLICT (tipo_documento, numero_documento) DO UPDATE
            SET nombres = excluded.nombres,
                apellidos = excluded.apellidos
        RETURNING id INTO person_id;

        INSERT INTO public.cuentas_usuario
            (persona_id, correo, password_hash, estado)
        VALUES (person_id, email, cfg.password_hash, 'ACTIVA')
        ON CONFLICT (correo) DO UPDATE
            SET password_hash = excluded.password_hash
        RETURNING id INTO account_id;

        SELECT id INTO STRICT role_id FROM public.roles WHERE codigo = staff.role_code;
        level_id := NULL;
        IF staff.level_code IS NOT NULL THEN
            SELECT id INTO STRICT level_id
            FROM public.niveles_educativos WHERE codigo = staff.level_code;
        END IF;
        supervisor_id := NULL;
        IF staff.supervisor_code IS NOT NULL THEN
            SELECT cuenta_usuario_id INTO STRICT supervisor_id
            FROM edufast_demo_staff WHERE staff_code = staff.supervisor_code;
        END IF;

        INSERT INTO public.asignaciones_roles
            (cuenta_usuario_id, rol_id, institucion_id, anio_escolar_id,
             nivel_educativo_id, reporta_a_cuenta_usuario_id, vigente_desde)
        SELECT account_id, role_id, institution_id, school_year_id,
               level_id, supervisor_id, cfg.start_date
        WHERE NOT EXISTS (
            SELECT 1 FROM public.asignaciones_roles ar
            WHERE ar.cuenta_usuario_id = account_id
              AND ar.rol_id = role_id
              AND ar.institucion_id = institution_id
              AND ar.anio_escolar_id = school_year_id
              AND ar.nivel_educativo_id IS NOT DISTINCT FROM level_id
              AND ar.reporta_a_cuenta_usuario_id IS NOT DISTINCT FROM supervisor_id
              AND ar.vigente_hasta IS NULL
        );

        INSERT INTO edufast_demo_staff (staff_code, cuenta_usuario_id)
        VALUES (staff.staff_code, account_id)
        ON CONFLICT (staff_code) DO UPDATE
            SET cuenta_usuario_id = excluded.cuenta_usuario_id;

        IF staff.teacher_function IS NOT NULL THEN
            INSERT INTO public.asignaciones_docentes
                (cuenta_usuario_id, seccion_id, es_tutor, funcion, vigente_desde)
            VALUES (account_id, section_id, staff.is_tutor,
                    staff.teacher_function, cfg.start_date)
            ON CONFLICT (cuenta_usuario_id, seccion_id, vigente_desde) DO UPDATE
                SET es_tutor = excluded.es_tutor,
                    funcion = excluded.funcion,
                    vigente_hasta = NULL;
        END IF;

    END LOOP;
END $$;

COMMIT;
