INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (next value for seq_users, 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (next value for seq_users, 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (next value for seq_users, 'user_viewer', 'Lurker Skywalker', 'user_viewer@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (next value for seq_users, 'user_editor', 'User Editor', 'user_admin@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (next value for seq_users, 'org_viewer', 'Visore Antonio', 'org_viewer@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (next value for seq_users, 'org_editor', 'Giovanni Mele', 'org_editor@govhub.it', true);



INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (next value for seq_govhub_roles, 1, 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (next value for seq_govhub_roles, 1, 'govhub_users_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (next value for seq_govhub_roles, 1, 'govhub_users_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (next value for seq_govhub_roles, 1, 'govhub_user');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (next value for seq_govhub_roles, 1, 'govhub_organizations_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (next value for seq_govhub_roles, 1, 'govhub_organizations_viewer');
