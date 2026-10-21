-- Una sesión planificada puede quedar asignada a un bloque del horario en una
-- fecha concreta: así el inicio sabe qué sesión corresponde a "Matemática del
-- martes 21 a las 8:00" y si ya está lista, en borrador o sin preparar.
-- Las sesiones sin fecha siguen siendo válidas: son las de la unidad que el
-- docente todavía no ubicó en su horario.

alter table public.sesiones_planificadas
    add column if not exists horario_id bigint references public.horarios_seccion(id),
    add column if not exists fecha date;

alter table public.sesiones_planificadas
    drop constraint if exists sesiones_planificadas_horario_fecha_check;
alter table public.sesiones_planificadas
    add constraint sesiones_planificadas_horario_fecha_check
        check ((horario_id is null) = (fecha is null));

-- Un bloque, en una fecha, tiene a lo sumo una sesión.
create unique index if not exists uq_sesion_en_bloque_y_fecha
    on public.sesiones_planificadas (horario_id, fecha)
    where horario_id is not null;

comment on column public.sesiones_planificadas.horario_id is
    'Bloque del horario de la sección en el que se dicta; nulo si aún no tiene fecha.';
comment on column public.sesiones_planificadas.fecha is
    'Día en que se dicta. Debe caer en el día de la semana del bloque (lo valida el backend).';
