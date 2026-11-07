-- Lectura pública requerida por la app Android, que usa la clave sb_publishable
-- y consulta PostgREST como rol anon. Ejecutar en Supabase SQL Editor.
-- No incluye contraseñas ni claves privadas.

grant usage on schema public to anon;
grant select on table public.medicamentos, public.inventario, public.farmacias to anon;

alter table public.medicamentos enable row level security;
alter table public.inventario enable row level security;
alter table public.farmacias enable row level security;

drop policy if exists "Android anon read medicamentos" on public.medicamentos;
create policy "Android anon read medicamentos"
    on public.medicamentos
    for select
    to anon
    using (true);

drop policy if exists "Android anon read inventario" on public.inventario;
create policy "Android anon read inventario"
    on public.inventario
    for select
    to anon
    using (true);

drop policy if exists "Android anon read farmacias" on public.farmacias;
create policy "Android anon read farmacias"
    on public.farmacias
    for select
    to anon
    using (true);
