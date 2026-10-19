-- Datos de referencia del currículo, no datos de demostración: son el CNEB y el
-- Programa Curricular tal como están publicados. Alcance declarado del proyecto:
-- 4.º grado de primaria, áreas de Matemática y Comunicación.
--
-- Horas: CNEB, Tabla N.º 6 "Organización y distribución del tiempo semanal de
-- Educación Primaria", p. 165. La hora pedagógica dura 45 minutos (misma página).
-- Total 30 horas pedagógicas semanales en todos los grados de primaria.

-- La versión curricular y las áreas se crean en la migración 550.

-- Plan de estudios de los seis grados de primaria, con las horas pedagógicas
-- de la Tabla N.º 6 del CNEB, p. 165. Todos los grados suman 30 horas.
with v as (select id from public.versiones_curriculares where codigo = 'CNEB_2016')
insert into public.planes_estudio (version_curricular_id, grado_id, codigo_modelo_servicio, nombre)
select v.id, g.id, 'EBR_REGULAR',
       'Plan de estudios ' || g.numero || '.º primaria · 30 horas pedagógicas'
from v
join public.grados_educativos g
     on g.nivel_id = (select id from public.niveles_educativos where codigo = 'PRIMARIA')
where g.numero between 1 and 6
on conflict (version_curricular_id, grado_id, codigo_modelo_servicio) do update
    set nombre = excluded.nombre;

with horas (grado, codigo, h) as (values
    (1,'MAT',5),(1,'COM',5),(1,'ING',2),(1,'PS',3),(1,'AYC',3),(1,'CYT',3),(1,'EF',3),(1,'ER',1),(1,'TOE',2),(1,'LD',3),
    (2,'MAT',5),(2,'COM',5),(2,'ING',2),(2,'PS',3),(2,'AYC',3),(2,'CYT',3),(2,'EF',3),(2,'ER',1),(2,'TOE',2),(2,'LD',3),
    (3,'MAT',4),(3,'COM',4),(3,'ING',3),(3,'PS',4),(3,'AYC',3),(3,'CYT',4),(3,'EF',3),(3,'ER',1),(3,'TOE',2),(3,'LD',2),
    (4,'MAT',4),(4,'COM',4),(4,'ING',3),(4,'PS',4),(4,'AYC',3),(4,'CYT',4),(4,'EF',3),(4,'ER',1),(4,'TOE',2),(4,'LD',2),
    (5,'MAT',4),(5,'COM',4),(5,'ING',3),(5,'PS',4),(5,'AYC',3),(5,'CYT',4),(5,'EF',3),(5,'ER',1),(5,'TOE',2),(5,'LD',2),
    (6,'MAT',4),(6,'COM',4),(6,'ING',3),(6,'PS',4),(6,'AYC',3),(6,'CYT',4),(6,'EF',3),(6,'ER',1),(6,'TOE',2),(6,'LD',2)
)
insert into public.planes_estudio_areas
    (plan_estudio_id, area_curricular_id, horas_pedagogicas, es_obligatoria)
select pe.id, a.id, horas.h, horas.codigo <> 'LD'
from horas
join public.areas_curriculares a on a.codigo = horas.codigo
join public.grados_educativos g
     on g.numero = horas.grado
    and g.nivel_id = (select id from public.niveles_educativos where codigo = 'PRIMARIA')
join public.planes_estudio pe
     on pe.grado_id = g.id and pe.codigo_modelo_servicio = 'EBR_REGULAR'
    and pe.version_curricular_id = (select id from public.versiones_curriculares where codigo='CNEB_2016')
on conflict (plan_estudio_id, area_curricular_id) do update
    set horas_pedagogicas = excluded.horas_pedagogicas,
        es_obligatoria = excluded.es_obligatoria;

-- Las capacidades de las 30 competencias viven en la migración 1000.

-- Desempeños de 4.º grado. Texto VERBATIM de la edición publicada del Programa
-- Curricular de Educación Primaria (396 pp.), columna "logra el nivel esperado del
-- ciclo IV". Página impresa verificada una por una sobre el PDF.
-- El desempeño de Comunicación está cortado en un límite verificable: la frase
-- continúa en la página y debe completarse en una pasada posterior.
with c as (
    select id, codigo from public.competencias
    where version_curricular_id = (select id from public.versiones_curriculares where codigo='CNEB_2016')
),
g as (select id from public.grados_educativos
       where nivel_id = (select id from public.niveles_educativos where codigo='PRIMARIA') and numero = 4)
insert into public.desempenos_grado (competencia_id, grado_id, descripcion, fuente_url, fuente_pagina, exige_material_concreto)
select c.id, g.id, x.descripcion,
       'https://www.minedu.gob.pe/curriculo/pdf/programa-curricular-educacion-primaria.pdf',
       x.pagina, x.material
from g, c
join (values
    ('MAT_CANTIDAD','Establece relaciones entre datos y una o más acciones de agregar, quitar, comparar, igualar, reiterar, agrupar, repartir cantidades y combinar colecciones diferentes de objetos, para transformarlas en expresiones numéricas (modelo) de adición, sustracción, multiplicación y división con números naturales de hasta tres cifras.', '238', false),
    ('MAT_CANTIDAD','Expresa con diversas representaciones y lenguaje numérico (números, signos y expresiones verbales) su comprensión sobre la centena como nueva unidad en el sistema de numeración decimal, sus equivalencias con decenas y unidades, el valor posicional de una cifra en números de tres cifras y la comparación y el orden de números.', '238', false),
    ('COM_LEE','Identifica información explícita y relevante que se encuentra en distintas partes del texto. Distingue esta información de otra semejante, en la que selecciona datos específicos, en diversos tipos de textos de estructura simple, con algunos elementos complejos.', '165', false)
) as x(cod, descripcion, pagina, material) on x.cod = c.codigo
on conflict (competencia_id, grado_id, descripcion) do update
    set fuente_pagina = excluded.fuente_pagina,
        fuente_url = excluded.fuente_url,
        exige_material_concreto = excluded.exige_material_concreto;

-- Enfoques transversales del CNEB.
with v as (select id from public.versiones_curriculares where codigo='CNEB_2016')
insert into public.enfoques_transversales (version_curricular_id, codigo, nombre)
select v.id, x.codigo, x.nombre from v, (values
    ('EBC','Orientación al bien común'),
    ('EA','Enfoque ambiental'),
    ('EDH','Enfoque de derechos'),
    ('EIB','Enfoque intercultural'),
    ('EIG','Enfoque de igualdad de género'),
    ('EAD','Enfoque de atención a la diversidad'),
    ('EBE','Enfoque de búsqueda de la excelencia')
) as x(codigo, nombre)
on conflict (version_curricular_id, codigo) do update set nombre = excluded.nombre;

-- Plantillas base de procesos didácticos. institucion_id nulo = sugeridas, cada IE
-- puede crear las suyas. origen PRACTICA_IE porque NO están en el currículo:
-- la expresión "procesos didácticos" aparece cero veces en el CNEB y cero veces
-- en el Programa Curricular de Primaria. Verificado en octubre de 2026.
insert into public.procesos_didacticos (institucion_id, area_curricular_id, competencia_id, nombre, origen, nota_fuente)
select null, a.id,
       (select id from public.competencias
         where codigo = 'MAT_CANTIDAD' and version_curricular_id =
               (select id from public.versiones_curriculares where codigo='CNEB_2016')),
       'Resolución de problemas · secuencia habitual', 'PRACTICA_IE',
       'No figura en el CNEB ni en el Programa Curricular. Recogida de sesiones reales de aula.'
from public.areas_curriculares a where a.codigo = 'MAT'
  and not exists (select 1 from public.procesos_didacticos p
                  where p.institucion_id is null and p.area_curricular_id = a.id);

insert into public.procesos_didacticos (institucion_id, area_curricular_id, competencia_id, nombre, origen, nota_fuente)
select null, a.id,
       (select id from public.competencias
         where codigo = 'COM_LEE' and version_curricular_id =
               (select id from public.versiones_curriculares where codigo='CNEB_2016')),
       'Comprensión de textos · secuencia habitual', 'PRACTICA_IE',
       'No figura en el CNEB ni en el Programa Curricular. Recogida de sesiones reales de aula.'
from public.areas_curriculares a where a.codigo = 'COM'
  and not exists (select 1 from public.procesos_didacticos p
                  where p.institucion_id is null and p.area_curricular_id = a.id);

insert into public.pasos_proceso_didactico (proceso_id, orden, nombre)
select p.id, x.orden, x.nombre
from public.procesos_didacticos p
join public.areas_curriculares a on a.id = p.area_curricular_id
join (values
    ('MAT',1,'Planteamiento del problema'),
    ('MAT',2,'Familiarización con el problema'),
    ('MAT',3,'Búsqueda y ejecución de la estrategia'),
    ('MAT',4,'Socialización de representaciones'),
    ('MAT',5,'Formalización y reflexión'),
    ('MAT',6,'Planteamiento de otras situaciones'),
    ('COM',1,'Antes de la lectura'),
    ('COM',2,'Durante la lectura'),
    ('COM',3,'Después de la lectura')
) as x(area, orden, nombre) on x.area = a.codigo
where p.institucion_id is null
on conflict (proceso_id, orden) do update set nombre = excluded.nombre;

-- NOTA SOBRE "MATERIAL CONCRETO", corregida el 19-10-2026.
-- El conteo de 7 ocurrencias que se venía citando sale de la maqueta de 212 pp.
-- del día de aprobación (03-06-2016). En la EDICIÓN PUBLICADA de 396 pp., que es
-- la que MINEDU sirve hoy, son 28 ocurrencias repartidas en 13 páginas.
-- La conclusión no cambia —la exigencia es por desempeño y no general— pero la
-- cifra correcta es 28 en 13 páginas. Ninguno de los tres desempeños cargados
-- aquí menciona material concreto, por eso exige_material_concreto queda en false:
-- marcarlo requiere una pasada por esas 13 páginas.
--
-- PENDIENTE: estandares_aprendizaje.fuente_pagina sigue en NULL. Mientras lo esté,
-- la aplicación debe mostrar la fuente sin página, nunca una página inventada.
