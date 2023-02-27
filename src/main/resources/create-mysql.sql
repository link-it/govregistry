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
  FOREIGN KEY (id_govhub_application) REFERENCES govhub_applications(id)
);

CREATE TABLE govhub_assignable_roles (
  role_id BIGINT NOT NULL,
  assignable_role_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, assignable_role_id),
  FOREIGN KEY (role_id) REFERENCES govhub_roles(id),
  FOREIGN KEY (assignable_role_id) REFERENCES govhub_roles(id)
);

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
  legal_name VARCHAR(80) NOT NULL,
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
  FOREIGN KEY (id_govhub_role) REFERENCES govhub_roles(id),
  FOREIGN KEY (id_govhub_user) REFERENCES govhub_users(id)
);

CREATE TABLE govhub_auth_organizations (
  id_govhub_authorization BIGINT NOT NULL,
  id_govhub_organization BIGINT NOT NULL,
  PRIMARY KEY (id_govhub_authorization, id_govhub_organization),
  FOREIGN KEY (id_govhub_authorization) REFERENCES govhub_authorizations(id),
  FOREIGN KEY (id_govhub_organization) REFERENCES govhub_organizations(id)
);

CREATE TABLE govhub_auth_services (
  id_govhub_authorization BIGINT NOT NULL,
  id_govhub_service BIGINT NOT NULL,
  PRIMARY KEY (id_govhub_authorization, id_govhub_service),
  FOREIGN KEY (id_govhub_authorization) REFERENCES govhub_authorizations(id),
  FOREIGN KEY (id_govhub_service) REFERENCES govhub_services(id)
);
