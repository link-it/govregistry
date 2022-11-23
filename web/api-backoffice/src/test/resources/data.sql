INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (1, 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (2, 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (3, 'user_viewer', 'Lurker Skywalker', 'user_viewer@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (4, 'user_editor', 'User Editor', 'user_admin@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (5, 'org_viewer', 'Visore Antonio', 'org_viewer@govhub.it', true);
INSERT INTO public.users (id, principal, full_name, email, enabled) VALUES (6, 'org_editor', 'Giovanni Mele', 'org_editor@govhub.it', true);

ALTER SEQUENCE SEQ_USERS RESTART WITH 7;


INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (1, 1, 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (2, 1, 'govhub_users_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (3, 1, 'govhub_users_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (4, 1, 'govhub_user');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (5, 1, 'govhub_organizations_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (6, 1, 'govhub_organizations_viewer');

ALTER SEQUENCE SEQ_GOVHUB_ROLES RESTART WITH 7;


INSERT INTO public.organizations (id, tax_code, legal_name) VALUES (1, '12345678901', 'Ente Creditore');
INSERT INTO public.organizations (id, tax_code, legal_name) VALUES (2, '12345678902', 'Ente Creditore 2');

ALTER SEQUENCE SEQ_ORGANIZATIONS RESTART WITH 3;

INSERT INTO public.govhub_services (id, name, description) VALUES (1, 'Servizio Generico', 'Esempio di servizio');
INSERT INTO public.govhub_services (id, name, description) VALUES (2, 'Servizio senza autorizzazioni', 'Servizio non autorizzato');
INSERT INTO public.govhub_services (id, name, description) VALUES (3, 'SUAP-Integrazione', 'Service for customer management');
INSERT INTO public.govhub_services (id, name, description) VALUES (4, 'IMU-ImpostaMunicipaleUnica', 'Imposta municipale unica');
INSERT INTO public.govhub_services (id, name, description) VALUES (5, 'TARI', 'Tassa sui rifiuti');
INSERT INTO public.govhub_services (id, name, description) VALUES (6, 'Portale ZTL', 'Servizio di registrazione accessi ZTL comunale');
INSERT INTO public.govhub_services (id, name, description) VALUES (7, 'Variazione Residenza', 'Richieste di variazione residenza');
INSERT INTO public.govhub_services (id, name, description) VALUES (8, 'Servizi Turistici', 'Portale di riferimento per i turisti');

ALTER SEQUENCE SEQ_GOVHUB_SERVICES RESTART WITH 9;

-- amministratore -> govhub_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (1, 1, 1);

-- user_viewer -> govhub_user_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (2, 3, 3);

ALTER SEQUENCE SEQ_GOVHUB_AUTHORIZATIONS RESTART WITH 3;

