-- Las 30 competencias del Programa Curricular de Educación Primaria con la página
-- impresa donde se desarrollan sus desempeños. Extraídas por script de la edición
-- publicada de 396 pp. (sha256 en investigacion/normativa/FUENTES.md).
-- Los códigos son legibles porque la aplicación referencia por código.
-- 'C2L' = Castellano como segunda lengua, área del CNEB para IE bilingües.

insert into public.versiones_curriculares (codigo, nombre, fuente_url, vigente_desde)
values ('CNEB_2016',
        'Currículo Nacional de la Educación Básica (RM N.º 281-2016-MINEDU, modificada por RM N.º 159-2017-MINEDU)',
        'https://www.minedu.gob.pe/curriculo/pdf/curriculo-nacional-de-la-educacion-basica.pdf',
        '2017-01-01')
on conflict (codigo) do update
    set nombre = excluded.nombre, fuente_url = excluded.fuente_url;

insert into public.areas_curriculares (codigo, nombre) values
    ('MAT', 'Matemática'),
    ('COM', 'Comunicación'),
    ('PS',  'Personal Social'),
    ('CYT', 'Ciencia y Tecnología'),
    ('ING', 'Inglés'),
    ('AYC', 'Arte y Cultura'),
    ('EF',  'Educación Física'),
    ('ER',  'Educación Religiosa'),
    ('TOE', 'Tutoría y Orientación Educativa'),
    ('LD',  'Horas de libre disponibilidad')
on conflict (codigo) do update set nombre = excluded.nombre;

insert into public.areas_curriculares (codigo, nombre) values
    ('C2L', 'Castellano como segunda lengua')
on conflict (codigo) do update set nombre = excluded.nombre;

with v as (select id from public.versiones_curriculares where codigo = 'CNEB_2016')
insert into public.competencias (version_curricular_id, codigo, nombre, tipo)
select v.id, x.codigo, x.nombre, x.tipo from v, (values
    ('PS_IDENTIDAD','Construye su identidad','AREA'),
    ('PS_CONVIVE','Convive y participa democráticamente en la búsqueda del bien común','AREA'),
    ('PS_HISTORIA','Construye interpretaciones históricas','AREA'),
    ('PS_AMBIENTE','Gestiona responsablemente el espacio y el ambiente','AREA'),
    ('PS_ECONOMIA','Gestiona responsablemente los recursos económicos','AREA'),
    ('EF_MOTRICIDAD','Se desenvuelve de manera autónoma a través de su motricidad','AREA'),
    ('EF_VIDA_SALUDABLE','Asume una vida saludable','AREA'),
    ('EF_SOCIOMOTRIZ','Interactúa a través de sus habilidades sociomotrices','AREA'),
    ('AYC_APRECIA','Aprecia de manera crítica manifestaciones artístico-culturales','AREA'),
    ('AYC_CREA','Crea proyectos desde los lenguajes artísticos','AREA'),
    ('COM_ORAL','Se comunica oralmente en su lengua materna','AREA'),
    ('COM_LEE','Lee diversos tipos de textos escritos en su lengua materna','AREA'),
    ('COM_ESCRIBE','Escribe diversos tipos de textos en su lengua materna','AREA'),
    ('C2L_ORAL','Se comunica oralmente en castellano como segunda lengua','AREA'),
    ('C2L_LEE','Lee diversos tipos de textos escritos en castellano como segunda lengua','AREA'),
    ('C2L_ESCRIBE','Escribe diversos tipos de textos en castellano como segunda lengua','AREA'),
    ('ING_ORAL','Se comunica oralmente en inglés como lengua extranjera','AREA'),
    ('ING_LEE','Lee diversos tipos de textos en inglés como lengua extranjera','AREA'),
    ('ING_ESCRIBE','Escribe diversos tipos de textos en inglés como lengua extranjera','AREA'),
    ('MAT_CANTIDAD','Resuelve problemas de cantidad','AREA'),
    ('MAT_REGULARIDAD','Resuelve problemas de regularidad, equivalencia y cambio','AREA'),
    ('MAT_FORMA','Resuelve problemas de forma, movimiento y localización','AREA'),
    ('MAT_DATOS','Resuelve problemas de gestión de datos e incertidumbre','AREA'),
    ('CYT_INDAGA','Indaga mediante métodos científicos para construir sus conocimientos','AREA'),
    ('CYT_EXPLICA','Explica el mundo natural y artificial basándose en conocimientos sobre los seres vivos, materia y energía, biodiversidad, Tierra y universo','AREA'),
    ('CYT_DISENA','Diseña y construye soluciones tecnológicas para resolver problemas de su entorno','AREA'),
    ('ER_IDENTIDAD','Construye su identidad como persona humana, amada por Dios, digna, libre y trascendente','AREA'),
    ('ER_ENCUENTRO','Asume la experiencia del encuentro personal y comunitario con Dios en su proyecto de vida en coherencia con su creencia religiosa','AREA'),
    ('CT_APRENDIZAJE','Gestiona su aprendizaje de manera autónoma','TRANSVERSAL'),
    ('CT_ENTORNOS','Se desenvuelve en entornos virtuales generados por las TIC con responsabilidad y ética','TRANSVERSAL')
) as x(codigo, nombre, tipo)
on conflict (version_curricular_id, codigo) do update
    set nombre = excluded.nombre, tipo = excluded.tipo;

insert into public.competencias_areas (competencia_id, area_curricular_id)
select c.id, a.id from (values
    ('PS_IDENTIDAD','PS'),
    ('PS_CONVIVE','PS'),
    ('PS_HISTORIA','PS'),
    ('PS_AMBIENTE','PS'),
    ('PS_ECONOMIA','PS'),
    ('EF_MOTRICIDAD','EF'),
    ('EF_VIDA_SALUDABLE','EF'),
    ('EF_SOCIOMOTRIZ','EF'),
    ('AYC_APRECIA','AYC'),
    ('AYC_CREA','AYC'),
    ('COM_ORAL','COM'),
    ('COM_LEE','COM'),
    ('COM_ESCRIBE','COM'),
    ('C2L_ORAL','C2L'),
    ('C2L_LEE','C2L'),
    ('C2L_ESCRIBE','C2L'),
    ('ING_ORAL','ING'),
    ('ING_LEE','ING'),
    ('ING_ESCRIBE','ING'),
    ('MAT_CANTIDAD','MAT'),
    ('MAT_REGULARIDAD','MAT'),
    ('MAT_FORMA','MAT'),
    ('MAT_DATOS','MAT'),
    ('CYT_INDAGA','CYT'),
    ('CYT_EXPLICA','CYT'),
    ('CYT_DISENA','CYT'),
    ('ER_IDENTIDAD','ER'),
    ('ER_ENCUENTRO','ER')
) as m(cod, area)
join public.competencias c on c.codigo = m.cod
     and c.version_curricular_id = (select id from public.versiones_curriculares where codigo='CNEB_2016')
join public.areas_curriculares a on a.codigo = m.area
on conflict do nothing;

create table if not exists public.referencias_competencia (
    competencia_id bigint primary key references public.competencias(id),
    documento varchar(140) not null,
    pagina_impresa varchar(10) not null,
    verificado_el date not null default current_date
);

comment on table public.referencias_competencia is
    'Página impresa verificada donde el Programa Curricular desarrolla la competencia. '
    'Si una competencia no tiene fila aquí, la aplicación cita la fuente sin página.';

insert into public.referencias_competencia (competencia_id, documento, pagina_impresa)
select c.id, 'Programa Curricular de Educación Primaria, edición publicada de 396 pp.', m.pag
from (values
    ('PS_IDENTIDAD','78'),
    ('PS_CONVIVE','86'),
    ('PS_HISTORIA','93'),
    ('PS_AMBIENTE','99'),
    ('PS_ECONOMIA','107'),
    ('EF_MOTRICIDAD','115'),
    ('EF_VIDA_SALUDABLE','121'),
    ('EF_SOCIOMOTRIZ','127'),
    ('AYC_APRECIA','135'),
    ('AYC_CREA','141'),
    ('COM_ORAL','152'),
    ('COM_LEE','165'),
    ('COM_ESCRIBE','175'),
    ('C2L_ORAL','187'),
    ('C2L_LEE','195'),
    ('C2L_ESCRIBE','201'),
    ('ING_ORAL','212'),
    ('ING_LEE','220'),
    ('ING_ESCRIBE','227'),
    ('MAT_CANTIDAD','238'),
    ('MAT_REGULARIDAD','249'),
    ('MAT_FORMA','258'),
    ('MAT_DATOS','268'),
    ('CYT_INDAGA','279'),
    ('CYT_EXPLICA','288'),
    ('CYT_DISENA','298'),
    ('ER_IDENTIDAD','69'),
    ('ER_ENCUENTRO','315'),
    ('CT_APRENDIZAJE','7'),
    ('CT_ENTORNOS','329')
) as m(cod, pag)
join public.competencias c on c.codigo = m.cod
     and c.version_curricular_id = (select id from public.versiones_curriculares where codigo='CNEB_2016')
on conflict (competencia_id) do update
    set pagina_impresa = excluded.pagina_impresa, verificado_el = current_date;
