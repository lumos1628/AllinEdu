-- Semilla de la capa pedagógica de una institución de primaria.
-- Corre DESPUÉS de seed.sql, que crea la IE, el año escolar, las secciones,
-- las personas y las matrículas. Aquí se agrega lo curricular: calendario real
-- 2026, periodos, oferta por área, horario, una unidad con sus sesiones, el
-- instrumento de evaluación y el progreso del bimestre en curso.
--
-- Datos sintéticos. Ningún nombre de estudiante ni de docente real.
-- Fechas del calendario: RM N.º 501-2025-MINEDU, Tabla 1.
-- Horas por área: CNEB, Tabla N.º 6, p. 165 (30 horas pedagógicas de 45 min).

BEGIN;

CREATE TEMP TABLE cfg_primaria AS
SELECT
    -- Si la IE cuenta con docentes especializados, la tutora de aula no dicta
    -- esas áreas y le quedan más horas no lectivas. Es la variable que decide
    -- si el tiempo asignado para preparar es de ~16 o de ~45 min por sesión.
    true  AS tiene_docente_ingles,
    true  AS tiene_docente_ed_fisica,
    4     AS grado_objetivo,
    2026  AS anio;

-- Contexto: la IE y el año escolar que dejó seed.sql
CREATE TEMP TABLE ctx AS
SELECT ae.id AS anio_escolar_id, ae.institucion_id, ae.anio
FROM public.anios_escolares ae
JOIN cfg_primaria c ON c.anio = ae.anio
ORDER BY ae.id
LIMIT 1;

-- 1. Turnos
INSERT INTO public.turnos (codigo, nombre)
SELECT * FROM (VALUES ('M','Mañana'), ('T','Tarde')) AS t(codigo, nombre)
WHERE NOT EXISTS (SELECT 1 FROM public.turnos WHERE codigo = t.codigo);

-- 2. Calendario 2026 tal como lo fija la Norma Técnica
INSERT INTO public.eventos_calendario (anio_escolar_id, nombre, tipo, fecha_inicio, fecha_fin, descripcion)
SELECT ctx.anio_escolar_id, x.nombre, x.tipo, x.ini, x.fin,
       'RM N.º 501-2025-MINEDU, Tabla 1'
FROM ctx, (VALUES
    ('Primer bloque de semanas de gestión',  'GESTION', DATE '2026-03-02', DATE '2026-03-13'),
    ('Primer bloque de semanas lectivas',    'LECTIVO', DATE '2026-03-16', DATE '2026-05-15'),
    ('Segundo bloque de semanas de gestión', 'GESTION', DATE '2026-05-18', DATE '2026-05-22'),
    ('Segundo bloque de semanas lectivas',   'LECTIVO', DATE '2026-05-25', DATE '2026-07-24'),
    ('Tercer bloque de semanas de gestión',  'GESTION', DATE '2026-07-27', DATE '2026-08-07'),
    ('Tercer bloque de semanas lectivas',    'LECTIVO', DATE '2026-08-10', DATE '2026-10-09'),
    ('Cuarto bloque de semanas de gestión',  'GESTION', DATE '2026-10-12', DATE '2026-10-16'),
    ('Cuarto bloque de semanas lectivas',    'LECTIVO', DATE '2026-10-19', DATE '2026-12-18'),
    ('Quinto bloque de semanas de gestión',  'GESTION', DATE '2026-12-21', DATE '2026-12-31')
) AS x(nombre, tipo, ini, fin)
WHERE NOT EXISTS (
    SELECT 1 FROM public.eventos_calendario e
    WHERE e.anio_escolar_id = ctx.anio_escolar_id AND e.nombre = x.nombre);

-- 3. Cuatro bimestres, uno por bloque lectivo
INSERT INTO public.periodos_evaluacion
    (anio_escolar_id, nombre, tipo, numero, fecha_inicio, fecha_fin, estado)
SELECT ctx.anio_escolar_id, x.nombre, 'BIMESTRE', x.numero, x.ini, x.fin, x.estado
FROM ctx, (VALUES
    ('I bimestre',   1::smallint, DATE '2026-03-16', DATE '2026-05-15', 'CERRADO'),
    ('II bimestre',  2::smallint, DATE '2026-05-25', DATE '2026-07-24', 'CERRADO'),
    ('III bimestre', 3::smallint, DATE '2026-08-10', DATE '2026-10-09', 'ABIERTO'),
    ('IV bimestre',  4::smallint, DATE '2026-10-19', DATE '2026-12-18', 'PLANIFICADO')
) AS x(nombre, numero, ini, fin, estado)
ON CONFLICT (anio_escolar_id, tipo, numero) DO UPDATE
    SET fecha_inicio = excluded.fecha_inicio,
        fecha_fin    = excluded.fecha_fin,
        estado       = excluded.estado;

-- 4. Oferta curricular: cada sección del grado objetivo recibe todas las áreas
--    del plan de estudios, con sus horas.
-- Es una IE de primaria: las secciones de otros niveles quedan desactivadas.
UPDATE public.secciones s
   SET activa = false
  FROM ctx, public.grados_educativos g
 WHERE g.id = s.grado_id
   AND s.anio_escolar_id = ctx.anio_escolar_id
   AND g.nivel_id <> (SELECT id FROM public.niveles_educativos WHERE codigo = 'PRIMARIA');

-- Los estudiantes que seed.sql matriculó en secciones de otros niveles quedan
-- cerrados: esta IE es de primaria. No se borra nada, se cierra la vigencia.
-- GREATEST evita violar check (fecha_fin >= fecha_inicio) cuando la semilla de
-- demo arranca el año en una fecha posterior al cierre nominal.
UPDATE public.ubicaciones_matricula um
   SET fecha_fin = GREATEST(um.fecha_inicio, DATE '2026-03-15')
  FROM public.secciones s, public.grados_educativos g, ctx
 WHERE s.id = um.seccion_id AND g.id = s.grado_id
   AND s.anio_escolar_id = ctx.anio_escolar_id
   AND um.fecha_fin IS NULL
   AND g.nivel_id <> (SELECT id FROM public.niveles_educativos WHERE codigo = 'PRIMARIA');

UPDATE public.matriculas m
   SET estado = 'CERRADA'
 WHERE m.estado = 'ACTIVA'
   AND NOT EXISTS (SELECT 1 FROM public.ubicaciones_matricula um
                   WHERE um.matricula_id = m.id AND um.fecha_fin IS NULL);

CREATE TEMP TABLE secciones_objetivo AS
SELECT s.id AS seccion_id, g.numero AS grado, s.nombre
FROM public.secciones s
JOIN ctx ON ctx.anio_escolar_id = s.anio_escolar_id
JOIN public.grados_educativos g ON g.id = s.grado_id
WHERE g.nivel_id = (SELECT id FROM public.niveles_educativos WHERE codigo = 'PRIMARIA')
  AND s.activa;

INSERT INTO public.ofertas_curriculares (seccion_id, area_curricular_id, plan_estudio_area_id)
SELECT so.seccion_id, pa.area_curricular_id, pa.id
FROM secciones_objetivo so
JOIN public.grados_educativos g
     ON g.numero = so.grado
    AND g.nivel_id = (SELECT id FROM public.niveles_educativos WHERE codigo='PRIMARIA')
JOIN public.planes_estudio pe ON pe.grado_id = g.id AND pe.codigo_modelo_servicio = 'EBR_REGULAR'
JOIN public.planes_estudio_areas pa ON pa.plan_estudio_id = pe.id
ON CONFLICT (seccion_id, area_curricular_id) DO UPDATE
    SET plan_estudio_area_id = excluded.plan_estudio_area_id;

-- 4b. Plantilla docente real: una docente de aula por sección, dos especialistas,
--     director, subdirector y secretaría. seed.sql deja solo un docente de muestra.
DO $plantilla$
DECLARE
    v_ctx      record;
    v_sec      record;
    v_persona  bigint;
    v_cuenta   bigint;
    v_rol      smallint;
    v_doc      text;
    v_nombre   text;
    v_esp      record;
    v_dominio  text;
    v_hash     text;
BEGIN
    SELECT * INTO v_ctx FROM ctx;

    -- El dominio y el hash se toman de las cuentas que ya creó seed.sql, para que
    -- el personal nuevo entre con la misma contraseña de demostración y no haya
    -- ninguna credencial escrita en este archivo.
    -- Cualquier cuenta activa sirve: al reaplicar la semilla los correos ya
    -- estan renombrados y el prefijo personal.% ya no existe.
    SELECT split_part(correo, '@', 2), password_hash
      INTO v_dominio, v_hash
      FROM public.cuentas_usuario
     WHERE estado = 'ACTIVA' ORDER BY id LIMIT 1;
    IF v_dominio IS NULL THEN
        v_dominio := 'example.test';
        v_hash := 'sin-credencial-ejecuta-seed-sql-primero';
    END IF;
    SELECT id INTO v_rol FROM public.roles WHERE codigo = 'DOCENTE';

    -- seed.sql deja un docente de muestra como tutor; se le cierra la vigencia
    -- porque la restricción admite un solo tutor activo por sección.
    UPDATE public.asignaciones_docentes ad
       SET vigente_hasta = GREATEST(ad.vigente_desde, DATE '2026-03-15')
     WHERE ad.es_tutor
       AND ad.vigente_hasta IS NULL
       AND ad.seccion_id IN (SELECT seccion_id FROM secciones_objetivo);

    -- una docente de aula por sección, tutora de la suya
    FOR v_sec IN SELECT seccion_id, grado, nombre FROM secciones_objetivo ORDER BY grado, nombre
    LOOP
        v_doc    := 'DEMO-DOC-' || v_sec.grado || v_sec.nombre;
        v_nombre := 'Docente ' || v_sec.grado || '.º ' || v_sec.nombre;

        INSERT INTO public.personas (nombres, apellidos, tipo_documento, numero_documento)
        VALUES (v_nombre, 'Aula', 'CODIGO_DEMO', v_doc)
        ON CONFLICT (tipo_documento, numero_documento) DO UPDATE SET nombres = excluded.nombres
        RETURNING id INTO v_persona;

        INSERT INTO public.cuentas_usuario (persona_id, correo, password_hash, estado)
        VALUES (v_persona, format('tutor.%s%s@%s', v_sec.grado, lower(v_sec.nombre), v_dominio),
                v_hash, 'ACTIVA')
        ON CONFLICT (persona_id) DO UPDATE SET estado = 'ACTIVA'
        RETURNING id INTO v_cuenta;

        INSERT INTO public.asignaciones_roles
            (cuenta_usuario_id, rol_id, institucion_id, anio_escolar_id, vigente_desde)
        SELECT v_cuenta, v_rol, v_ctx.institucion_id, v_ctx.anio_escolar_id, DATE '2026-03-16'
        WHERE NOT EXISTS (SELECT 1 FROM public.asignaciones_roles ar
                          WHERE ar.cuenta_usuario_id = v_cuenta AND ar.rol_id = v_rol
                            AND ar.anio_escolar_id = v_ctx.anio_escolar_id);

        INSERT INTO public.asignaciones_docentes
            (cuenta_usuario_id, seccion_id, es_tutor, funcion, vigente_desde)
        VALUES (v_cuenta, v_sec.seccion_id, true, 'DOCENTE', DATE '2026-03-16')
        ON CONFLICT (cuenta_usuario_id, seccion_id, vigente_desde) DO UPDATE
            SET es_tutor = true, vigente_hasta = NULL;
    END LOOP;

    -- especialistas de Educación Física e Inglés, para todas las secciones
    FOR v_esp IN
        SELECT * FROM (VALUES
            ('EF',  'Docente de Educación Física', 'ef'),
            ('ING', 'Docente de Inglés',           'ingles')
        ) AS e(area, nombre, slug)
    LOOP
        INSERT INTO public.personas (nombres, apellidos, tipo_documento, numero_documento)
        VALUES (v_esp.nombre, 'Especialista', 'CODIGO_DEMO', 'DEMO-ESP-' || v_esp.area)
        ON CONFLICT (tipo_documento, numero_documento) DO UPDATE SET nombres = excluded.nombres
        RETURNING id INTO v_persona;

        INSERT INTO public.cuentas_usuario (persona_id, correo, password_hash, estado)
        VALUES (v_persona, format('especialista.%s@%s', v_esp.slug, v_dominio),
                v_hash, 'ACTIVA')
        ON CONFLICT (persona_id) DO UPDATE SET estado = 'ACTIVA'
        RETURNING id INTO v_cuenta;

        INSERT INTO public.asignaciones_roles
            (cuenta_usuario_id, rol_id, institucion_id, anio_escolar_id, vigente_desde)
        SELECT v_cuenta, v_rol, v_ctx.institucion_id, v_ctx.anio_escolar_id, DATE '2026-03-16'
        WHERE NOT EXISTS (SELECT 1 FROM public.asignaciones_roles ar
                          WHERE ar.cuenta_usuario_id = v_cuenta AND ar.rol_id = v_rol
                            AND ar.anio_escolar_id = v_ctx.anio_escolar_id);

        -- dicta su área en todas las secciones de primaria
        INSERT INTO public.asignaciones_docentes_areas
            (oferta_curricular_id, cuenta_usuario_id, vigente_desde)
        SELECT oc.id, v_cuenta, DATE '2026-03-16'
        FROM public.ofertas_curriculares oc
        JOIN secciones_objetivo so ON so.seccion_id = oc.seccion_id
        JOIN public.areas_curriculares a ON a.id = oc.area_curricular_id
        WHERE a.codigo = v_esp.area
        ON CONFLICT (oferta_curricular_id, cuenta_usuario_id, vigente_desde) DO NOTHING;
    END LOOP;

    -- subdirector
    SELECT id INTO v_rol FROM public.roles WHERE codigo = 'SUBDIRECTOR';
    IF v_rol IS NOT NULL THEN
        INSERT INTO public.personas (nombres, apellidos, tipo_documento, numero_documento)
        VALUES ('Subdirector', 'de Primaria', 'CODIGO_DEMO', 'DEMO-SUBDIR')
        ON CONFLICT (tipo_documento, numero_documento) DO UPDATE SET nombres = excluded.nombres
        RETURNING id INTO v_persona;

        INSERT INTO public.cuentas_usuario (persona_id, correo, password_hash, estado)
        VALUES (v_persona, 'subdirector@' || v_dominio, v_hash, 'ACTIVA')
        ON CONFLICT (persona_id) DO UPDATE SET estado = 'ACTIVA'
        RETURNING id INTO v_cuenta;

        INSERT INTO public.asignaciones_roles
            (cuenta_usuario_id, rol_id, institucion_id, anio_escolar_id,
             nivel_educativo_id, vigente_desde)
        SELECT v_cuenta, v_rol, v_ctx.institucion_id, v_ctx.anio_escolar_id,
               (SELECT id FROM public.niveles_educativos WHERE codigo='PRIMARIA'), DATE '2026-03-16'
        WHERE NOT EXISTS (SELECT 1 FROM public.asignaciones_roles ar
                          WHERE ar.cuenta_usuario_id = v_cuenta AND ar.rol_id = v_rol
                            AND ar.anio_escolar_id = v_ctx.anio_escolar_id);
    END IF;
END $plantilla$;

-- 5. La tutora de aula dicta todas las áreas salvo las que tengan especialista.
INSERT INTO public.asignaciones_docentes_areas (oferta_curricular_id, cuenta_usuario_id, vigente_desde)
SELECT oc.id, ad.cuenta_usuario_id, DATE '2026-03-16'
FROM public.ofertas_curriculares oc
JOIN secciones_objetivo so ON so.seccion_id = oc.seccion_id
JOIN public.asignaciones_docentes ad
     ON ad.seccion_id = oc.seccion_id AND ad.es_tutor AND ad.vigente_hasta IS NULL
JOIN public.areas_curriculares a ON a.id = oc.area_curricular_id
CROSS JOIN cfg_primaria c
WHERE NOT (a.codigo = 'ING' AND c.tiene_docente_ingles)
  AND NOT (a.codigo = 'EF'  AND c.tiene_docente_ed_fisica)
  AND a.codigo <> 'LD'
ON CONFLICT (oferta_curricular_id, cuenta_usuario_id, vigente_desde) DO NOTHING;

-- 6. Horario semanal: tres bloques de 90 minutos por día.
--    30 horas pedagógicas × 45 min = 1350 min por semana = 270 min por día.
INSERT INTO public.horarios_seccion
    (seccion_id, oferta_curricular_id, dia_semana, hora_inicio, hora_fin, descripcion, vigente_desde)
SELECT so.seccion_id, oc.id, x.dia, x.ini, x.fin, a.nombre, DATE '2026-03-16'
FROM secciones_objetivo so
JOIN (VALUES
    (1::smallint, TIME '08:00', TIME '09:30', 'MAT'),
    (1::smallint, TIME '09:45', TIME '11:15', 'COM'),
    (1::smallint, TIME '11:30', TIME '13:00', 'PS'),
    (2::smallint, TIME '08:00', TIME '09:30', 'MAT'),
    (2::smallint, TIME '09:45', TIME '11:15', 'CYT'),
    (2::smallint, TIME '11:30', TIME '13:00', 'EF'),
    (3::smallint, TIME '08:00', TIME '09:30', 'COM'),
    (3::smallint, TIME '09:45', TIME '11:15', 'PS'),
    (3::smallint, TIME '11:30', TIME '13:00', 'AYC'),
    (4::smallint, TIME '08:00', TIME '09:30', 'MAT'),
    (4::smallint, TIME '09:45', TIME '11:15', 'CYT'),
    (4::smallint, TIME '11:30', TIME '13:00', 'ING'),
    (5::smallint, TIME '08:00', TIME '09:30', 'COM'),
    (5::smallint, TIME '09:45', TIME '11:15', 'TOE'),
    (5::smallint, TIME '11:30', TIME '13:00', 'ER')
) AS x(dia, ini, fin, area) ON TRUE
JOIN public.areas_curriculares a ON a.codigo = x.area
JOIN public.ofertas_curriculares oc
     ON oc.seccion_id = so.seccion_id AND oc.area_curricular_id = a.id
ON CONFLICT (seccion_id, dia_semana, hora_inicio) DO UPDATE
    SET oferta_curricular_id = excluded.oferta_curricular_id,
        descripcion = excluded.descripcion;

COMMIT;

-- ============================================================================
-- Unidad en curso con sus sesiones, criterios, instrumento y progreso.
-- Es lo que leen las pantallas de elaborar sesión y de cierre de bimestre.
-- Los títulos de las sesiones y la secuencia didáctica reproducen la forma del
-- material real cedido por una docente; los nombres de estudiantes son los
-- sintéticos que generó seed.sql.
-- ============================================================================

BEGIN;

-- La protagonista es la docente de 4.o B (entra como tutor.4b@). Todo lo que
-- sigue -- planificacion, unidad, sesiones, conclusiones -- cuelga de ella y de
-- su seccion, para que la simulacion sea la de una sola docente real.
CREATE TEMP TABLE ctx2 AS
WITH sec AS (
    SELECT s.id, s.anio_escolar_id
      FROM public.secciones s
      JOIN public.grados_educativos g ON g.id = s.grado_id
     WHERE g.numero = 4 AND s.nombre = 'B' AND s.activa
       AND g.nivel_id = (SELECT id FROM public.niveles_educativos WHERE codigo='PRIMARIA')
     ORDER BY s.id LIMIT 1
)
SELECT ae.id AS anio_escolar_id, ae.institucion_id,
       (SELECT id FROM public.periodos_evaluacion
         WHERE anio_escolar_id = ae.id AND tipo='BIMESTRE' AND numero=3) AS bimestre_iii,
       (SELECT id FROM public.periodos_evaluacion
         WHERE anio_escolar_id = ae.id AND tipo='BIMESTRE' AND numero=4) AS bimestre_iv,
       (SELECT ad.cuenta_usuario_id FROM public.asignaciones_docentes ad
         WHERE ad.seccion_id = (SELECT id FROM sec)
           AND ad.es_tutor AND ad.vigente_hasta IS NULL
         ORDER BY ad.id LIMIT 1) AS tutora_id,
       (SELECT id FROM sec) AS seccion_id
FROM public.anios_escolares ae WHERE ae.anio = 2026 ORDER BY ae.id LIMIT 1;

-- 1. Planificación anual y su destino
INSERT INTO public.planificaciones_anuales
    (institucion_id, anio_escolar_id, nombre, estado, creada_por)
SELECT institucion_id, anio_escolar_id,
       'Planificación anual 4.º B · 2026', 'PUBLICADA', tutora_id
FROM ctx2 WHERE tutora_id IS NOT NULL
ON CONFLICT (institucion_id, anio_escolar_id, nombre, version) DO NOTHING;

INSERT INTO public.destinos_planificacion (planificacion_anual_id, seccion_id)
SELECT pa.id, c.seccion_id
FROM public.planificaciones_anuales pa, ctx2 c
WHERE pa.anio_escolar_id = c.anio_escolar_id
ON CONFLICT DO NOTHING;

-- 2. La unidad
INSERT INTO public.experiencias_aprendizaje
    (planificacion_anual_id, tipo, titulo, situacion_significativa, proposito,
     fecha_inicio, fecha_fin, numero_orden, estado)
SELECT pa.id, 'UNIDAD', 'Repartimos en partes iguales',
       'En el barrio hay una olla común los domingos. Los niños ayudan a repartir y discuten si a todos les tocó igual. ¿Cómo sabemos si el reparto fue justo?',
       'Comparar y repartir cantidades usando material concreto, y escribir un texto instructivo a partir de la experiencia.',
       DATE '2026-10-20', DATE '2026-11-07', 6, 'EN_CURSO'
FROM public.planificaciones_anuales pa, ctx2 c
WHERE pa.anio_escolar_id = c.anio_escolar_id
ON CONFLICT (planificacion_anual_id, numero_orden) DO UPDATE
    SET estado = excluded.estado, situacion_significativa = excluded.situacion_significativa;

CREATE TEMP TABLE uni AS
SELECT e.id AS experiencia_id FROM public.experiencias_aprendizaje e
WHERE e.numero_orden = 6 ORDER BY e.id LIMIT 1;

-- Competencias que la unidad desarrolla, y el periodo al que pertenece
INSERT INTO public.experiencias_competencias (experiencia_id, competencia_id, proposito_especifico)
SELECT u.experiencia_id, c.id, x.prop
FROM uni u
JOIN (VALUES
    ('MAT_CANTIDAD','Comparar y repartir cantidades con material concreto antes de pasar al símbolo.'),
    ('COM_ESCRIBE','Escribir la receta de la olla común como texto instructivo.'),
    ('PS_ECONOMIA','Estimar cuánto costó la olla común y comparar precios.')
) AS x(cod, prop) ON TRUE
JOIN public.competencias c ON c.codigo = x.cod
ON CONFLICT (experiencia_id, competencia_id) DO UPDATE
    SET proposito_especifico = excluded.proposito_especifico;

-- La unidad va del 20-oct al 07-nov: cae en el IV bimestre (19-oct a 18-dic).
-- Las conclusiones descriptivas de mas abajo si pertenecen al III, que es el
-- bimestre que la docente arrastra sin cerrar.
INSERT INTO public.experiencias_periodos (experiencia_id, periodo_evaluacion_id)
SELECT u.experiencia_id, c.bimestre_iv FROM uni u, ctx2 c WHERE c.bimestre_iv IS NOT NULL
ON CONFLICT DO NOTHING;

-- 3. Criterios de evaluación de la competencia de Matemática
INSERT INTO public.criterios_evaluacion (experiencia_id, competencia_id, descripcion, orden)
SELECT u.experiencia_id, c.id, x.descripcion, x.orden
FROM uni u
JOIN public.competencias c ON c.codigo = 'MAT_CANTIDAD'
JOIN (VALUES
    ('Compara dos fracciones del mismo denominador e identifica cuál es mayor.', 1::smallint),
    ('Usa las tiras de fracciones para comprobar su respuesta.', 2::smallint),
    ('Explica con sus palabras cómo supo cuál era mayor.', 3::smallint)
) AS x(descripcion, orden) ON TRUE
ON CONFLICT (experiencia_id, competencia_id, orden) DO UPDATE
    SET descripcion = excluded.descripcion;

-- 4. El instrumento real: escala valorativa con niveles por criterio
INSERT INTO public.instrumentos_evaluacion (experiencia_id, nombre, tipo, descripcion)
SELECT u.experiencia_id, 'Escala valorativa · comparar fracciones', 'ESCALA_VALORACION',
       'Un nivel por criterio y por estudiante. Es el instrumento que usa la IE.'
FROM uni u
WHERE NOT EXISTS (SELECT 1 FROM public.instrumentos_evaluacion i WHERE i.experiencia_id = u.experiencia_id);

INSERT INTO public.criterios_instrumento (instrumento_id, criterio_id, descripcion, orden)
SELECT i.id, ce.id, ce.descripcion, ce.orden
FROM public.instrumentos_evaluacion i
JOIN uni u ON u.experiencia_id = i.experiencia_id
JOIN public.criterios_evaluacion ce ON ce.experiencia_id = u.experiencia_id
ON CONFLICT (instrumento_id, orden) DO UPDATE SET criterio_id = excluded.criterio_id;

INSERT INTO public.niveles_instrumento (criterio_instrumento_id, codigo, descripcion, orden)
SELECT ci.id, x.codigo, x.descripcion, x.orden
FROM public.criterios_instrumento ci
JOIN (VALUES
    ('A', 'Lo logra por sí solo.', 1::smallint),
    ('B', 'Lo logra con apoyo o con el material delante.', 2::smallint),
    ('C', 'Todavía no lo logra.', 3::smallint)
) AS x(codigo, descripcion, orden) ON TRUE
ON CONFLICT (criterio_instrumento_id, codigo) DO UPDATE SET descripcion = excluded.descripcion;

-- 5. Las 12 sesiones de la unidad
INSERT INTO public.sesiones_planificadas
    (experiencia_id, numero_orden, titulo, proposito, duracion_minutos, estado)
SELECT u.experiencia_id, x.n, x.titulo, x.prop, 90, x.estado
FROM uni u
JOIN (VALUES
    (1::smallint,  '¿Nos tocó igual a todos?', 'Reconocer repartos justos e injustos.', 'PUBLICADA'),
    (2::smallint,  'Partimos la unidad con las tiras', 'Representar fracciones con material concreto.', 'PUBLICADA'),
    (3::smallint,  'Comparamos fracciones para saber si el reparto fue justo', 'Comparar fracciones de igual denominador con material.', 'PUBLICADA'),
    (4::smallint,  'Escribimos la receta de la olla común', 'Planificar y escribir un texto instructivo.', 'PUBLICADA'),
    (5::smallint,  '¿Cuánto costó la olla común?', 'Estimar y sumar costos.', 'BORRADOR'),
    (6::smallint,  'Comparamos precios del mercado', 'Comparar precios y decidir.', 'BORRADOR'),
    (7::smallint,  'Fracciones en la receta', 'Usar fracciones en cantidades de ingredientes.', 'BORRADOR'),
    (8::smallint,  'Revisamos y corregimos la receta', 'Revisar el texto propio.', 'BORRADOR'),
    (9::smallint,  'Repartos que no son iguales', 'Comparar fracciones de distinto denominador.', 'BORRADOR'),
    (10::smallint, 'Publicamos el recetario del aula', 'Editar y compartir el producto.', 'BORRADOR'),
    (11::smallint, 'Cerramos lo que aprendimos de fracciones', 'Formalizar la regla descubierta.', 'BORRADOR'),
    (12::smallint, 'Contamos a las familias lo que hicimos', 'Comunicar oralmente el proceso.', 'BORRADOR')
) AS x(n, titulo, prop, estado) ON TRUE
ON CONFLICT (experiencia_id, numero_orden) WHERE experiencia_id IS NOT NULL DO UPDATE
    SET titulo = excluded.titulo, proposito = excluded.proposito, estado = excluded.estado;

-- Cada sesión hereda la competencia de la unidad
INSERT INTO public.sesiones_competencias (sesion_id, competencia_id)
SELECT sp.id, c.id
FROM public.sesiones_planificadas sp
JOIN uni u ON u.experiencia_id = sp.experiencia_id
JOIN public.competencias c ON c.codigo = CASE
    WHEN sp.numero_orden IN (1,2,3,7,9,11) THEN 'MAT_CANTIDAD'
    WHEN sp.numero_orden IN (4,8,10,12)    THEN 'COM_ESCRIBE'
    ELSE 'PS_ECONOMIA' END
ON CONFLICT (sesion_id, competencia_id) DO NOTHING;

-- 6. La sesión 3 desarrollada: momentos y pasos didácticos del área
INSERT INTO public.actividades_sesion
    (sesion_id, numero_orden, momento, titulo, instrucciones, duracion_minutos, paso_proceso_id)
SELECT sp.id, x.n, x.momento, x.titulo, x.instr, x.mins, pp.id
FROM public.sesiones_planificadas sp
JOIN uni u ON u.experiencia_id = sp.experiencia_id AND sp.numero_orden = 3
JOIN (VALUES
    (1::smallint,'INICIO','Actividad permanente','Saludo, marcar la asistencia y la oración del día.',5::smallint,NULL::text),
    (2::smallint,'INICIO','Dinámica','La recta en el suelo: cinta del 0 al 100 en el patio y saltos según la operación.',5::smallint,NULL),
    (3::smallint,'INICIO','Propósito y criterios','Se presenta el propósito y se socializan los tres criterios de evaluación.',5::smallint,NULL),
    (4::smallint,'DESARROLLO','Planteamiento del problema','Se presenta el reparto del queque en 8 partes iguales.',10::smallint,'Planteamiento del problema'),
    (5::smallint,'DESARROLLO','Familiarización','Responden qué se pregunta, qué datos hay y qué pide el problema.',10::smallint,'Familiarización con el problema'),
    (6::smallint,'DESARROLLO','Estrategia','En equipos de cuatro resuelven con sus propias estrategias en papelógrafo.',15::smallint,'Búsqueda y ejecución de la estrategia'),
    (7::smallint,'DESARROLLO','Socialización','Cada equipo comunica su representación con las tiras.',15::smallint,'Socialización de representaciones'),
    (8::smallint,'DESARROLLO','Formalización','Se formaliza la regla y se reflexiona sobre las estrategias usadas.',10::smallint,'Formalización y reflexión'),
    (9::smallint,'CIERRE','Ficha de aplicación','Resuelven la ficha y se recogen las evidencias.',10::smallint,'Planteamiento de otras situaciones'),
    (10::smallint,'CIERRE','Metacognición','¿Qué hicimos hoy? ¿Para qué nos será útil?',5::smallint,NULL)
) AS x(n, momento, titulo, instr, mins, paso) ON TRUE
LEFT JOIN public.pasos_proceso_didactico pp ON pp.nombre = x.paso
ON CONFLICT (sesion_id, numero_orden) DO UPDATE
    SET instrucciones = excluded.instrucciones, paso_proceso_id = excluded.paso_proceso_id;

-- 7. DUA, enfoque transversal, evidencia prevista y materiales de la sesión 3
INSERT INTO public.sesiones_dua (sesion_id, pauta_dua_id, descripcion)
SELECT sp.id, pd.id, x.desc
FROM public.sesiones_planificadas sp
JOIN uni u ON u.experiencia_id = sp.experiencia_id AND sp.numero_orden = 3
JOIN (VALUES
    ('REPRESENTACION','Tiras de fracciones y esquema para representar el reparto.'),
    ('ACCION_EXPRESION','Pueden resolver con material, con dibujo o explicando en voz alta.'),
    ('COMPROMISO','La situación parte de la olla común del barrio, que ya conocen.')
) AS x(cod, "desc") ON TRUE
JOIN public.pautas_dua pd ON pd.codigo = x.cod
ON CONFLICT (sesion_id, pauta_dua_id) DO UPDATE SET descripcion = excluded.descripcion;

INSERT INTO public.sesiones_enfoques (sesion_id, enfoque_id, accion_observable)
SELECT sp.id, et.id,
       'Los estudiantes discuten si el reparto fue justo y proponen cómo repartir mejor.'
FROM public.sesiones_planificadas sp
JOIN uni u ON u.experiencia_id = sp.experiencia_id AND sp.numero_orden = 3
JOIN public.enfoques_transversales et ON et.codigo = 'EBC'
ON CONFLICT (sesion_id, enfoque_id) DO UPDATE SET accion_observable = excluded.accion_observable;

INSERT INTO public.evidencias_previstas (experiencia_id, sesion_id, descripcion, formato, requiere_entrega)
SELECT u.experiencia_id, sp.id,
       'Ficha resuelta y explicación oral de cómo supo cuál fracción era mayor.',
       'ESCRITA', true
FROM public.sesiones_planificadas sp
JOIN uni u ON u.experiencia_id = sp.experiencia_id AND sp.numero_orden = 3
WHERE NOT EXISTS (SELECT 1 FROM public.evidencias_previstas ev WHERE ev.sesion_id = sp.id);

INSERT INTO public.materiales_educativos (institucion_id, nombre, tipo, es_estructurado_minedu)
SELECT c.institucion_id, x.nombre, x.tipo, x.minedu
FROM ctx2 c, (VALUES
    ('Tiras de fracciones','CONCRETO',true),
    ('Material base diez','CONCRETO',true),
    ('Papelógrafos y plumones','FUNGIBLE',false),
    ('Ficha de aplicación impresa','IMPRESO',false)
) AS x(nombre, tipo, minedu)
WHERE NOT EXISTS (SELECT 1 FROM public.materiales_educativos m WHERE m.nombre = x.nombre);

INSERT INTO public.sesiones_materiales (sesion_id, material_id, cantidad, caracter, desempeno_id)
SELECT sp.id, m.id, x.cant, x.car, NULL
FROM public.sesiones_planificadas sp
JOIN uni u ON u.experiencia_id = sp.experiencia_id AND sp.numero_orden = 3
JOIN (VALUES
    ('Tiras de fracciones', 7::smallint, 'SUGERIDO'),
    ('Papelógrafos y plumones', 7::smallint, 'SUGERIDO'),
    ('Ficha de aplicación impresa', 25::smallint, 'SUGERIDO')
) AS x(nombre, cant, car) ON TRUE
JOIN public.materiales_educativos m ON m.nombre = x.nombre
WHERE NOT EXISTS (
    SELECT 1 FROM public.sesiones_materiales sm WHERE sm.sesion_id = sp.id AND sm.material_id = m.id);

-- 8. Progreso del III bimestre: una mezcla de niveles. El trigger de la RVM 048-2024
--    obliga a que B y C traigan conclusión descriptiva y recomendación.
INSERT INTO public.progreso_competencias
    (matricula_id, competencia_id, periodo_evaluacion_id, estado, nivel_logro,
     conclusion_descriptiva, recomendacion, registrada_por, registrada_at)
SELECT m.matricula_id, cm.id, c.bimestre_iii, 'EVALUADA', m.nivel,
       CASE WHEN m.nivel IN ('B','C') THEN m.conclusion END,
       CASE WHEN m.nivel IN ('B','C') THEN m.recomendacion END,
       c.tutora_id, now()
FROM ctx2 c
JOIN public.competencias cm ON cm.codigo = 'MAT_CANTIDAD'
JOIN (
    SELECT um.matricula_id,
           (ARRAY['A','A','B','A','B','C','A','B','A','A','B','C','A','B','A',
                  'AD','B','A','C','B','A','A','B','A','A'])[
               1 + (row_number() OVER (ORDER BY um.matricula_id) - 1) % 25] AS nivel,
           'Compara fracciones del mismo denominador cuando usa las tiras y explica al grupo cómo llegó a su respuesta. Le cuesta todavía hacerlo solo con números, sin el material delante.' AS conclusion,
           'Dos semanas más con las tiras, pidiéndole que escriba el símbolo recién después de haberlo armado.' AS recomendacion
    FROM public.ubicaciones_matricula um
    JOIN ctx2 cc ON cc.seccion_id = um.seccion_id
    WHERE um.fecha_fin IS NULL
) AS m ON TRUE
WHERE c.bimestre_iii IS NOT NULL AND c.tutora_id IS NOT NULL
ON CONFLICT (matricula_id, competencia_id, periodo_evaluacion_id) DO UPDATE
    SET nivel_logro = excluded.nivel_logro,
        conclusion_descriptiva = excluded.conclusion_descriptiva,
        recomendacion = excluded.recomendacion;

COMMIT;

-- ============================================================================
-- Correos de acceso con el rol en el prefijo, para que al entrar se sepa como
-- quien se esta entrando. No se cambia ninguna contrasena: solo el correo.
--   tutor.4b@        la docente de 4.o B, protagonista del prototipo
--   especialista.*@  Ingles y Educacion Fisica
--   director@ subdirector@ secretaria@
--   apoderado.NNNN@  numerados, en lugar del codigo largo de la semilla
-- ============================================================================

BEGIN;

CREATE TEMP TABLE dom AS
SELECT split_part(correo, '@', 2) AS dominio
FROM public.cuentas_usuario WHERE estado = 'ACTIVA' ORDER BY id LIMIT 1;

UPDATE public.cuentas_usuario cu
   SET correo = 'director@' || d.dominio
  FROM dom d WHERE cu.correo = 'personal.director.general@' || d.dominio;

UPDATE public.cuentas_usuario cu
   SET correo = 'director.primaria@' || d.dominio
  FROM dom d WHERE cu.correo = 'personal.director.primaria@' || d.dominio;

UPDATE public.cuentas_usuario cu
   SET correo = 'secretaria@' || d.dominio
  FROM dom d WHERE cu.correo = 'personal.secretaria@' || d.dominio;

UPDATE public.cuentas_usuario cu
   SET correo = 'docente.muestra@' || d.dominio
  FROM dom d WHERE cu.correo = 'personal.docente.regular@' || d.dominio;

UPDATE public.cuentas_usuario cu
   SET correo = 'auxiliar.muestra@' || d.dominio
  FROM dom d WHERE cu.correo = 'personal.docente.auxiliar@' || d.dominio;

-- apoderados: numeracion corta y estable por orden de cuenta
WITH num AS (
    SELECT cu.id, row_number() OVER (ORDER BY cu.id) AS n
    FROM public.cuentas_usuario cu WHERE cu.correo LIKE 'apoderado.%'
)
UPDATE public.cuentas_usuario cu
   SET correo = format('apoderado.%s@%s', lpad(num.n::text, 4, '0'), d.dominio)
  FROM num, dom d
 WHERE cu.id = num.id;

COMMIT;

-- ============================================================================
-- Cuentas de muestra que seed.sql deja y que esta IE no tiene.
-- Es una IE solo de primaria: un director y un subdirector, sin docente
-- auxiliar (figura de inicial) y sin el docente regular generico, cuya
-- tutoria ya se cerro mas arriba. No se borran: se revoca el acceso y se
-- cierra la vigencia del rol, para no romper las filas que las referencian.
-- ============================================================================

BEGIN;

-- El prefijo revocada. entra tambien en la seleccion: al reaplicar la semilla,
-- seed.sql vuelve a abrir estos roles y hay que cerrarlos otra vez, pero el
-- correo ya no empieza por el nombre original.
CREATE TEMP TABLE cuentas_muestra AS
SELECT cu.id
FROM public.cuentas_usuario cu
WHERE split_part(cu.correo, '@', 1) IN
      ('director.primaria', 'docente.muestra', 'auxiliar.muestra')
   OR cu.correo LIKE 'revocada.%';

UPDATE public.asignaciones_roles ar
   SET vigente_hasta = GREATEST(ar.vigente_desde, DATE '2026-03-15')
  FROM cuentas_muestra cm
 WHERE ar.cuenta_usuario_id = cm.id
   AND ar.vigente_hasta IS NULL;

UPDATE public.asignaciones_docentes ad
   SET vigente_hasta = GREATEST(ad.vigente_desde, DATE '2026-03-15')
  FROM cuentas_muestra cm
 WHERE ad.cuenta_usuario_id = cm.id
   AND ad.vigente_hasta IS NULL;

UPDATE public.asignaciones_docentes_areas ada
   SET vigente_hasta = GREATEST(ada.vigente_desde, DATE '2026-03-15')
  FROM cuentas_muestra cm
 WHERE ada.cuenta_usuario_id = cm.id
   AND ada.vigente_hasta IS NULL;

UPDATE public.cuentas_usuario cu
   SET correo = CASE WHEN cu.correo LIKE 'revocada.%'
                     THEN cu.correo ELSE 'revocada.' || cu.correo END,
       estado = 'REVOCADA'
  FROM cuentas_muestra cm
 WHERE cu.id = cm.id;

COMMIT;

-- ============================================================================
-- Estudiantes y familias que esta IE no tiene.
-- seed.sql genera dos secciones por grado de TODOS los niveles, porque es la
-- demostracion genérica. Esta es una IE solo de primaria: los estudiantes de
-- inicial y secundaria, cuyas matriculas quedaron cerradas mas arriba, no
-- pertenecen a la institucion y aqui si se borran, junto con los apoderados
-- que se quedan sin ningun estudiante. Si no, el recuento de la IE mentiria:
-- apareceria con 550 estudiantes cuando tiene 12 secciones.
-- Solo alcanza a filas sinteticas de la semilla y nunca a una matricula
-- vigente, asi que no hay historico que preservar.
-- ============================================================================

BEGIN;

CREATE TEMP TABLE mat_fuera AS
SELECT m.id AS matricula_id, m.estudiante_id, e.persona_id
FROM public.matriculas m
JOIN public.estudiantes e ON e.id = m.estudiante_id
WHERE NOT EXISTS (SELECT 1 FROM public.ubicaciones_matricula um
                   WHERE um.matricula_id = m.id AND um.fecha_fin IS NULL);

DELETE FROM public.registros_asistencia r
 USING public.ubicaciones_matricula um, mat_fuera f
 WHERE r.ubicacion_matricula_id = um.id AND um.matricula_id = f.matricula_id;

DELETE FROM public.asignaciones_actividad x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.destinos_actividad    x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.evidencias_aprendizaje x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.movimientos_matricula x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.progreso_competencias x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.retroalimentaciones   x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.ubicaciones_matricula x USING mat_fuera f WHERE x.matricula_id = f.matricula_id;
DELETE FROM public.matriculas            x USING mat_fuera f WHERE x.id = f.matricula_id;

DELETE FROM public.vinculos_apoderado_estudiante v
 USING mat_fuera f WHERE v.estudiante_id = f.estudiante_id;

DELETE FROM public.estudiantes e USING mat_fuera f WHERE e.id = f.estudiante_id;

-- Apoderados que se quedaron sin ningun estudiante
CREATE TEMP TABLE apo_fuera AS
SELECT a.id, a.persona_id, a.cuenta_usuario_id
FROM public.apoderados a
WHERE NOT EXISTS (SELECT 1 FROM public.vinculos_apoderado_estudiante v
                   WHERE v.apoderado_id = a.id);

DELETE FROM public.preferencias_notificacion x USING apo_fuera a WHERE x.cuenta_usuario_id = a.cuenta_usuario_id;
DELETE FROM public.dispositivos_notificacion x USING apo_fuera a WHERE x.cuenta_usuario_id = a.cuenta_usuario_id;
DELETE FROM public.notificaciones            x USING apo_fuera a WHERE x.cuenta_destinataria_id = a.cuenta_usuario_id;
DELETE FROM public.asignaciones_roles        x USING apo_fuera a WHERE x.cuenta_usuario_id = a.cuenta_usuario_id;
DELETE FROM public.apoderados                x USING apo_fuera a WHERE x.id = a.id;
DELETE FROM public.cuentas_usuario           x USING apo_fuera a WHERE x.id = a.cuenta_usuario_id;

-- Las personas que ya no sostienen ni un estudiante ni una cuenta
DELETE FROM public.contactos_persona c
 USING (SELECT persona_id FROM mat_fuera UNION SELECT persona_id FROM apo_fuera) q
 WHERE c.persona_id = q.persona_id;

DELETE FROM public.personas p
 USING (SELECT persona_id FROM mat_fuera UNION SELECT persona_id FROM apo_fuera) q
 WHERE p.id = q.persona_id
   AND NOT EXISTS (SELECT 1 FROM public.estudiantes e WHERE e.persona_id = p.id)
   AND NOT EXISTS (SELECT 1 FROM public.cuentas_usuario cu WHERE cu.persona_id = p.id)
   AND NOT EXISTS (SELECT 1 FROM public.apoderados a WHERE a.persona_id = p.id);

-- Renumera los correos de apoderado ya sin huecos
WITH num AS (
    SELECT cu.id, row_number() OVER (ORDER BY cu.id) AS n,
           split_part(cu.correo, '@', 2) AS dominio
    FROM public.cuentas_usuario cu WHERE cu.correo LIKE 'apoderado.%'
)
UPDATE public.cuentas_usuario cu
   SET correo = format('apoderado.%s@%s', lpad(num.n::text, 4, '0'), num.dominio)
  FROM num WHERE cu.id = num.id;

COMMIT;
