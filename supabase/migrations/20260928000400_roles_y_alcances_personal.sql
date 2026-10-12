-- Roles de acceso y alcance del personal escolar.

create schema if not exists extensions;
create extension if not exists pgcrypto with schema extensions;

insert into public.roles (codigo, nombre)
values ('SECRETARIA', 'Secretaría')
on conflict (codigo) do update set nombre = excluded.nombre;

alter table public.asignaciones_roles
    add column nivel_educativo_id smallint
        references public.niveles_educativos(id),
    add column reporta_a_cuenta_usuario_id bigint
        references public.cuentas_usuario(id);

alter table public.asignaciones_docentes
    add column funcion varchar(20) not null default 'DOCENTE'
        check (funcion in ('DOCENTE', 'AUXILIAR'));

create index idx_asignaciones_roles_nivel_vigente
    on public.asignaciones_roles (nivel_educativo_id, cuenta_usuario_id)
    where vigente_hasta is null;
