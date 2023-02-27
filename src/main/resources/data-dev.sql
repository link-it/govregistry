-- Applicazione

INSERT INTO public.govhub_applications(id, application_id, name, deployed_uri, logo_type, logo_color, bg_color) VALUES (1, 'govregistry', 'GovRegistry', 'http://localhost:10001', 'SVG', '#FFFF00', '#0000FF');

-- Utenti

INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_viewer', 'Lurker Skywalker', 'user_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'user_editor', 'User Editor', 'user_admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'org_viewer', 'Visore Antonio', 'org_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'org_editor', 'Giovanni Mele', 'org_editor@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (nextval('public.seq_govhub_users'), 'service_viewer', 'Antonio Servizio', 'service_viewer@govhub.it', true);

-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_users_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_users_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_user');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_organizations_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_organizations_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_role_assigner');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_services_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (nextval('public.seq_govhub_roles'), 1, 'govhub_services_editor');

-- Organizzazioni

INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000000', 'org-0', 'PoloTecnologico', 'Via Strali 10', 'org-0@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000001', 'org-1', 'PoloTecnologico', 'Via Strali 10', 'org-1@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000002', 'org-2', 'PoloTecnologico', 'Via Strali 10', 'org-2@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000003', 'org-3', 'PoloTecnologico', 'Via Strali 10', 'org-3@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000004', 'org-4', 'PoloTecnologico', 'Via Strali 10', 'org-4@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000005', 'org-5', 'PoloTecnologico', 'Via Strali 10', 'org-5@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000006', 'org-6', 'PoloTecnologico', 'Via Strali 10', 'org-6@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000007', 'org-7', 'PoloTecnologico', 'Via Strali 10', 'org-7@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000008', 'org-8', 'PoloTecnologico', 'Via Strali 10', 'org-8@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000009', 'org-9', 'PoloTecnologico', 'Via Strali 10', 'org-9@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000010', 'org-10', 'PoloTecnologico', 'Via Strali 10', 'org-10@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000011', 'org-11', 'PoloTecnologico', 'Via Strali 10', 'org-11@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000012', 'org-12', 'PoloTecnologico', 'Via Strali 10', 'org-12@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000013', 'org-13', 'PoloTecnologico', 'Via Strali 10', 'org-13@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (nextval('public.seq_govhub_organizations'), '00000000014', 'org-14', 'PoloTecnologico', 'Via Strali 10', 'org-14@zion.ix');


-- Servizi

INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-1', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-2', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-3', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-4', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-5', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-6', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-7', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-8', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-9', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-10', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-11', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-12', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-13', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-14', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-15', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-16', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-17', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (nextval('public.seq_govhub_services'), 'service-18', 'Servizio per fare cose');


INSERT INTO public.govhub_assignable_roles (role_id, assignable_role_id) VALUES ( 
	(SELECT id FROM public.govhub_roles WHERE name='govhub_role_assigner' ),
	(SELECT id FROM public.govhub_roles WHERE name='govhub_organizations_viewer' ) 
);

-- amministratore -> govhub_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_services'),
	(SELECT id FROM public.govhub_users WHERE principal='amministratore'),
	(SELECT id FROM public.govhub_roles WHERE name='govhub_sysadmin' )
);

-- user_viewer -> govhub_user_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_services'),
	(SELECT id FROM public.govhub_users WHERE principal='user_viewer'),
	(SELECT id FROM public.govhub_roles WHERE name='govhub_users_viewer')
);

-- service_viewer -> govhub_services_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_services'),
	(SELECT id FROM public.govhub_users WHERE principal='service_viewer'),
	(SELECT id FROM public.govhub_roles WHERE name='govhub_services_viewer')
);


-- organization_editor -> govhub_organizations_editor

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (
	nextval('public.seq_govhub_services'),
	(SELECT id FROM public.govhub_users WHERE principal='org_editor'),
	(SELECT id FROM public.govhub_roles WHERE name='govhub_organizations_editor')
);

-- Questo limita l'utente org_viewer a lavorare sulla organizzazione 1
-- INSERT INTO public.govhub_auth_organizations(id_govhub_authorization, id_govhub_organization) VALUES(4,1);

