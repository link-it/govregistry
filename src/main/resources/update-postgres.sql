
-- PATCH 08-03-2023 - Renaming foreign key govregistry

alter table govhub_roles rename constraint govhub_roles_id_govhub_application_fkey TO GovhubRole_GovhubApplication;

alter table govhub_assignable_roles rename constraint govhub_assignable_roles_role_id_fkey TO GovhubAssRole_GovhubRole;
alter table govhub_assignable_roles rename constraint govhub_assignable_roles_assignable_role_id_fkey TO GovhubAssRole_AssignedGovhubRole;

alter table govhub_authorizations rename constraint govhub_authorizations_id_govhub_role_fkey TO GovhubAuth_GovhubRole;
alter table govhub_authorizations rename constraint govhub_authorizations_id_govhub_user_fkey TO GovhubAuth_GovhubUser;


alter table govhub_auth_organizations rename constraint govhub_auth_organizations_id_govhub_authorization_fkey TO GovhubAuthOrganization_GovhubAuth;
alter table govhub_auth_organizations rename constraint govhub_auth_organizations_id_govhub_organization_fkey TO GovhubAuthOrganization_GovhubOrganization;

alter table govhub_auth_services rename constraint govhub_auth_services_id_govhub_authorization_fkey TO GovhubAuthService_GovhubAuth;
alter table govhub_auth_services rename constraint govhub_auth_services_id_govhub_service_fkey TO GovhubAuthService_GovhubService;


-- PATCH 08-03-2023

alter table govhub_services alter column name set not null;


-- PATCH 13-03-2023

alter table govhub_services add column logo_miniature_media_type varchar(255);
alter table govhub_services add column logo_media_type varchar(255);

alter table govhub_organizations add column logo_miniature_media_type varchar(255);
alter table govhub_organizations add column logo_media_type varchar(255);

	
alter table govhub_applications alter column logo_type type character varying(255);
update govhub_applications set logo_type = 'IMAGE' where logo_type='0';
update govhub_applications set logo_type = 'SVG' where logo_type='1';
update govhub_applications set logo_type = 'BOOTSTRAP' where logo_type='2';
update govhub_applications set logo_type = 'MATERIAL' where logo_type='3';

-- PATCH 7-04-2024

alter table govhub_authorizations alter column id_govhub_user set not null;

alter table govhub_authorizations alter column id_govhub_role set not null;

-- PATCH 8-05-2023 Il logo nella tabella application diventa un json

alter table govhub_applications drop column logo_bg_color;
alter table govhub_applications drop column logo_color;
alter table govhub_applications drop column logo_type;
alter table govhub_applications drop column logo_url;
alter table govhub_applications drop column bg_color;
alter table govhub_applications drop column logo;

alter table govhub_applications add column logo TEXT;
