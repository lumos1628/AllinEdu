-- Autoría de las sesiones (Regla 0a de AGENTS.md): el sistema conserva qué
-- propuso un generador y qué escribió o cambió una persona, y quién aprobó.
-- Todo lo que ya existe se considera escrito por el docente.

alter table public.sesiones_planificadas
    add column if not exists origen varchar(12) not null default 'DOCENTE'
        check (origen in ('DOCENTE', 'GENERADA')),
    add column if not exists editada_por bigint references public.cuentas_usuario(id),
    add column if not exists editada_at timestamptz,
    add column if not exists aprobada_por bigint references public.cuentas_usuario(id),
    add column if not exists aprobada_at timestamptz;

alter table public.actividades_sesion
    add column if not exists origen varchar(12) not null default 'DOCENTE'
        check (origen in ('DOCENTE', 'GENERADA')),
    add column if not exists editada_at timestamptz;

comment on column public.sesiones_planificadas.origen is
    'GENERADA si el borrador lo propuso un generador; DOCENTE si lo escribió una persona.';
comment on column public.actividades_sesion.origen is
    'Pasa a DOCENTE cuando el docente cambia el texto de una actividad generada.';
