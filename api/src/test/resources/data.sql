INSERT INTO public.govhub_applications (id, application_id, name, deployed_uri) VALUES (1, 'govregistry', 'GovRegistry', 'http://localhost/govregistry');

-- Utenze

INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_viewer', 'Lurker Skywalker', 'user_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_editor', 'User Editor', 'user_admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'org_viewer', 'Visore Antonio', 'org_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'org_editor', 'Giovanni Mele', 'org_editor@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'service_viewer', 'Luca Viso', 'service_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'service_editor', 'Angelo Monti', 'service_editor@govhub.it', true);

-- ALTER SEQUENCE SEQ_GOVHUB_USERS RESTART WITH 9;

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_users_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_users_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_user');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_organizations_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_organizations_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_services_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_services_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), (SELECT id FROM govhub_applications WHERE application_id='govregistry'), 'govhub_ruolo_non_assegnabile');

-- ALTER SEQUENCE SEQ_GOVHUB_ROLES RESTART WITH 10;

-- Organizations

INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (nextval('public.seq_govhub_organizations'), '12345678901', 'Ente Creditore');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name) VALUES (nextval('public.seq_govhub_organizations'), '12345678902', 'Ente Creditore 2');

-- ALTER SEQUENCE SEQ_GOVHUB_ORGANIZATIONS RESTART WITH 3;

-- Services

INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Servizio Generico', 'Esempio di servizio');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Servizio senza autorizzazioni', 'Servizio non autorizzato');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'SUAP-Integrazione', 'Service for customer management');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'IMU-ImpostaMunicipaleUnica', 'Imposta municipale unica');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'TARI', 'Tassa sui rifiuti');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Portale ZTL', 'Servizio di registrazione accessi ZTL comunale');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Variazione Residenza', 'Richieste di variazione residenza');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'Servizi Turistici', 'Portale di riferimento per i turisti');

-- ALTER SEQUENCE SEQ_GOVHUB_SERVICES RESTART WITH 9;

-- Autorizzazioni

-- amministratore -> govhub_sysadmin
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='amministratore'), (SELECT id FROM public.govhub_roles WHERE name='govhub_sysadmin'));

-- user_viewer -> govhub_users_viewer
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_viewer'), (SELECT id FROM public.govhub_roles WHERE name='govhub_users_viewer'));

-- user_editor -> govhub_users_editor
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='user_editor'), (SELECT id FROM public.govhub_roles WHERE name='govhub_users_editor'));

-- org_viewer -> govhub_organizations_viewer
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='org_viewer'), (SELECT id FROM public.govhub_roles WHERE name='govhub_organizations_viewer'));

-- org_editor -> govhub_organizations_editor
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='org_editor'), (SELECT id FROM public.govhub_roles WHERE name='govhub_organizations_editor'));

-- service_viewer -> govhub_services_viewer
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='service_viewer'), (SELECT id FROM public.govhub_roles WHERE name='govhub_services_viewer'));

-- service_editor -> govhub_services_editor
INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (nextval('public.seq_govhub_authorizations'), (SELECT id FROM public.govhub_users WHERE principal='service_editor'), (SELECT id FROM public.govhub_roles WHERE name='govhub_services_editor'));

-- ALTER SEQUENCE SEQ_GOVHUB_AUTHORIZATIONS RESTART WITH 8;

-- Ruoli assegnabili da altri ruoli

-- govhub_users_editor puo' assegnare govhub_users_viewer
INSERT INTO public.govhub_assignable_roles (role_id, assignable_role_id) VALUES ((SELECT id FROM public.govhub_roles WHERE name='govhub_users_editor'), (SELECT id FROM public.govhub_roles WHERE name='govhub_users_viewer'));
-- govhub_users_editor puo' assegnare govhub_user
INSERT INTO public.govhub_assignable_roles (role_id, assignable_role_id) VALUES ((SELECT id FROM public.govhub_roles WHERE name='govhub_users_editor'), (SELECT id FROM public.govhub_roles WHERE name='govhub_user'));



