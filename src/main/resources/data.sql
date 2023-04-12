INSERT INTO govhub_applications (application_id, deployed_uri, name) VALUES (
	'govregistry', 'http://localhost:8083/govregistry-app', 'GovRegistry');	

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_sysadmin', 
	'Autorizza ad operare senza alcuna limitazione in qualunque applicazione.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_users_editor', 
	'Autorizza alla consultazione e modifica delle utenze.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_users_viewer', 
	'Autorizza alla consultazione delle utenze.',
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_organizations_editor', 
	'Autorizza alla consultazione e modifica delle organizzazioni.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_organizations_viewer', 
	'Autorizza alla consultazione delle organizzazioni.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_role_assigner', 
	'Autorizza alla consultazione ed assegnazione di ruoli alle utenze.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_services_viewer', 
	'Autorizza alla consultazione e modifica dei servizi.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));

INSERT INTO govhub_roles (name, description, id_govhub_application) VALUES (
	'govhub_services_editor', 
	'Autorizza alla consultazione dei servizi.', 
	(SELECT id FROM govhub_applications WHERE application_id='govregistry'));
