CREATE TABLE govhub_applications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  application_id VARCHAR(255) NOT NULL UNIQUE,
  deployed_uri VARCHAR(255) NOT NULL,
  logo_bg_color VARCHAR(255),
  logo_color VARCHAR(255),
  logo_name VARCHAR(255),
  logo_type VARCHAR(255),
  logo_url VARCHAR(255),
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE govhub_roles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  description VARCHAR(255),
  id_govhub_application BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY (id),
);
alter table govhub_roles 
       add constraint GovhubRole_GovhubApplication 
       foreign key (id_govhub_application) 
       references govhub_applications;


CREATE TABLE govhub_assignable_roles (
  role_id BIGINT NOT NULL,
  assignable_role_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, assignable_role_id),
);
alter table govhub_assignable_roles 
       add constraint GovhubAssRole_AssignedGovhubRole 
       foreign key (assignable_role_id) 
       references govhub_roles;

alter table govhub_assignable_roles 
       add constraint GovhubAssRole_GovhubRole 
       foreign key (role_id) 
       references govhub_roles;


CREATE TABLE govhub_users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  email VARCHAR(255),
  enabled BOOLEAN NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  principal VARCHAR(255) NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE govhub_organizations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  legal_name VARCHAR(80) NOT NULL UNIQUE,
  logo blob,
  logo_miniature blob,
  office_address VARCHAR(120),
  office_address_details VARCHAR(120),
  office_at VARCHAR(120),
  office_email_address VARCHAR(120),
  office_foreign_state VARCHAR(120),
  office_municipality VARCHAR(120),
  office_municipality_details VARCHAR(120),
  office_pec_address VARCHAR(120),
  office_phone_number VARCHAR(120),
  office_province VARCHAR(120),
  office_zip VARCHAR(120),
  tax_code VARCHAR(11) NOT NULL UNIQUE,
  PRIMARY KEY (id)
);

CREATE TABLE govhub_services (
  id BIGINT NOT NULL AUTO_INCREMENT,
  description TEXT,
  name VARCHAR(255) UNIQUE,
  logo oid,
  logo_miniature oid,
  PRIMARY KEY (id)
);

CREATE TABLE govhub_authorizations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  expiration_date timestamp,
  id_govhub_role BIGINT NOT NULL,
  id_govhub_user BIGINT NOT NULL,
  PRIMARY KEY (id),
);
alter table govhub_authorizations 
   add constraint GovhubAuth_GovhubRole 
   foreign key (id_govhub_role) 
   references govhub_roles;

alter table govhub_authorizations 
   add constraint GovhubAuth_GovhubUser 
   foreign key (id_govhub_user) 
   references govhub_users;



CREATE TABLE govhub_auth_organizations (
  id_govhub_authorization BIGINT NOT NULL,
  id_govhub_organization BIGINT NOT NULL,
  PRIMARY KEY (id_govhub_authorization, id_govhub_organization),
);
alter table govhub_auth_organizations 
   add constraint GovhubAuthOrganization_GovhubOrganization 
   foreign key (id_govhub_organization) 
   references govhub_organizations;

alter table govhub_auth_organizations 
   add constraint GovhubAuthOrganization_GovhubAuth 
   foreign key (id_govhub_authorization) 
   references govhub_authorizations;



CREATE TABLE govhub_auth_services (
  id_govhub_authorization BIGINT NOT NULL,
  id_govhub_service BIGINT NOT NULL,
  PRIMARY KEY (id_govhub_authorization, id_govhub_service),
);
alter table govhub_auth_services 
   add constraint GovhubAuthOrganization_GovhubService 
   foreign key (id_govhub_service) 
   references govhub_services;

alter table govhub_auth_services 
   add constraint GovhubAuthService_GovhubAuth 
   foreign key (id_govhub_authorization) 
   references govhub_authorizations;


