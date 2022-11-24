-- Utenti

INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (1, 'amministratore', 'Amministratore Vanguard', 'admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (2, 'ospite', 'Ospite Calmo', 'ospite@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (3, 'user_viewer', 'Lurker Skywalker', 'user_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (4, 'user_editor', 'User Editor', 'user_admin@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (5, 'org_viewer', 'Visore Antonio', 'org_viewer@govhub.it', true);
INSERT INTO public.govhub_users (id, principal, full_name, email, enabled) VALUES (6, 'org_editor', 'Giovanni Mele', 'org_editor@govhub.it', true);

ALTER SEQUENCE SEQ_GOVHUB_USERS RESTART WITH 7;


-- Ruoli

INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (1, 1, 'govhub_sysadmin');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (2, 1, 'govhub_users_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (3, 1, 'govhub_users_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (4, 1, 'govhub_user');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (5, 1, 'govhub_organizations_editor');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (6, 1, 'govhub_organizations_viewer');
INSERT INTO public.govhub_roles (id, id_govhub_application, name) VALUES (7, 1, 'govhub_role_assigner');

ALTER SEQUENCE SEQ_GOVHUB_ROLES RESTART WITH 8;


-- Organizzazioni

INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (1, '00000000000', 'org-0', 'PoloTecnologico', 'Via Strali 10', 'org-0@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (2, '00000000001', 'org-1', 'PoloTecnologico', 'Via Strali 10', 'org-1@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (3, '00000000002', 'org-2', 'PoloTecnologico', 'Via Strali 10', 'org-2@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (4, '00000000003', 'org-3', 'PoloTecnologico', 'Via Strali 10', 'org-3@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (5, '00000000004', 'org-4', 'PoloTecnologico', 'Via Strali 10', 'org-4@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (6, '00000000005', 'org-5', 'PoloTecnologico', 'Via Strali 10', 'org-5@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (7, '00000000006', 'org-6', 'PoloTecnologico', 'Via Strali 10', 'org-6@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (8, '00000000007', 'org-7', 'PoloTecnologico', 'Via Strali 10', 'org-7@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (9, '00000000008', 'org-8', 'PoloTecnologico', 'Via Strali 10', 'org-8@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (10, '00000000009', 'org-9', 'PoloTecnologico', 'Via Strali 10', 'org-9@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (11, '00000000010', 'org-10', 'PoloTecnologico', 'Via Strali 10', 'org-10@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (12, '00000000011', 'org-11', 'PoloTecnologico', 'Via Strali 10', 'org-11@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (13, '00000000012', 'org-12', 'PoloTecnologico', 'Via Strali 10', 'org-12@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (14, '00000000013', 'org-13', 'PoloTecnologico', 'Via Strali 10', 'org-13@zion.ix');
INSERT INTO public.govhub_organizations (id, tax_code, legal_name, office_at, office_address, office_email_address) VALUES (15, '00000000014', 'org-14', 'PoloTecnologico', 'Via Strali 10', 'org-14@zion.ix');

ALTER SEQUENCE SEQ_GOVHUB_ORGANIZATIONS RESTART WITH 16;


-- Servizi

INSERT INTO public.govhub_services (id, name, description) VALUES (1, 'service-1', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (2, 'service-2', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (3, 'service-3', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (4, 'service-4', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (5, 'service-5', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (6, 'service-6', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (7, 'service-7', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (8, 'service-8', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (9, 'service-9', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (10, 'service-10', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (11, 'service-11', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (12, 'service-12', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (13, 'service-13', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (14, 'service-14', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (15, 'service-15', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (16, 'service-16', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (17, 'service-17', 'Servizio per fare cose');
INSERT INTO public.govhub_services (id, name, description) VALUES (18, 'service-18', 'Servizio per fare cose');

ALTER SEQUENCE SEQ_GOVHUB_SERVICES RESTART WITH 19;

INSERT INTO public.govhub_assignable_roles (role_id, assignable_role_id) VALUES (7, 6);

-- amministratore -> govhub_sysadmin

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (1, 1, 1);

-- user_viewer -> govhub_user_viewer

INSERT INTO public.govhub_authorizations (id, id_govhub_user, id_govhub_role) VALUES (2, 3, 3);

ALTER SEQUENCE SEQ_GOVHUB_AUTHORIZATIONS RESTART WITH 3;

