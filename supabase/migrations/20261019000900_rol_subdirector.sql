-- Una IE de primaria con suficientes secciones tiene subdirector además de director.
insert into public.roles (codigo, nombre) values ('SUBDIRECTOR', 'Subdirector')
on conflict (codigo) do update set nombre = excluded.nombre;
