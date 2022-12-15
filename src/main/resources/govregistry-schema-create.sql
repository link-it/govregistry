create sequence seq_govhub_authorizations start 1 increment 1;
create sequence seq_govhub_organizations start 1 increment 1;
create sequence seq_govhub_roles start 1 increment 1;
create sequence seq_govhub_services start 1 increment 1;
create sequence seq_govhub_users start 1 increment 1;

    create table govhub_assignable_roles (
       role_id int8 not null,
        assignable_role_id int8 not null,
        primary key (role_id, assignable_role_id)
    );

    create table govhub_auth_organizations (
       id_govhub_authorization int8 not null,
        id_govhub_organization int8 not null,
        primary key (id_govhub_authorization, id_govhub_organization)
    );

    create table govhub_auth_services (
       id_govhub_authorization int8 not null,
        id_govhub_service int8 not null,
        primary key (id_govhub_authorization, id_govhub_service)
    );

    create table govhub_authorizations (
       id int8 not null,
        expiration_date timestamp,
        id_govhub_role int8,
        id_govhub_user int8,
        primary key (id)
    );

    create table govhub_organizations (
       id int8 not null,
        legal_name varchar(80) not null,
        logo oid,
        logo_miniature oid,
        office_address varchar(120),
        office_address_details varchar(120),
        office_at varchar(120),
        office_email_address varchar(120),
        office_foreign_state varchar(120),
        office_municipality varchar(120),
        office_municipality_details varchar(120),
        office_pec_address varchar(120),
        office_phone_number varchar(120),
        office_province varchar(120),
        office_zip varchar(120),
        tax_code varchar(11) not null,
        primary key (id)
    );

    create table govhub_roles (
       id int8 not null,
        description varchar(255),
        id_govhub_application int8 not null,
        name varchar(255) not null,
        primary key (id)
    );

    create table govhub_services (
       id int8 not null,
        description varchar(255),
        name varchar(255),
        primary key (id)
    );

    create table govhub_users (
       id int8 not null,
        email varchar(255),
        enabled boolean not null,
        full_name varchar(255) not null,
        principal varchar(255) not null,
        primary key (id)
    );

    alter table govhub_organizations 
       add constraint UK_3tku2orbbyp0o0c8qq56cqbou unique (legal_name);

    alter table govhub_organizations 
       add constraint UK_r8n43m06apkfslppxa4yd5977 unique (tax_code);

    alter table govhub_roles 
       add constraint UK_lsmgbd03vdg496rorn3in2ntj unique (name);

    alter table govhub_users 
       add constraint UK_flp48uw93dwjm43yjdbf8mppd unique (principal);

    alter table govhub_assignable_roles 
       add constraint FKhibyeyvi0hcnqj0abi71v4bpr 
       foreign key (assignable_role_id) 
       references govhub_roles;

    alter table govhub_assignable_roles 
       add constraint FKhn9ny7ah3jhy6pvl4cv5pgwr4 
       foreign key (role_id) 
       references govhub_roles;

    alter table govhub_auth_organizations 
       add constraint FKgxmwbdowwv5epo9vw4c921rj4 
       foreign key (id_govhub_organization) 
       references govhub_organizations;

    alter table govhub_auth_organizations 
       add constraint FKib4rr02po89yyjufqjkxfxmkf 
       foreign key (id_govhub_authorization) 
       references govhub_authorizations;

    alter table govhub_auth_services 
       add constraint FKrskc3e6hnbg3i3silh74djwnb 
       foreign key (id_govhub_service) 
       references govhub_services;

    alter table govhub_auth_services 
       add constraint FKp2cmsa6rj69resrgjemt6y1bi 
       foreign key (id_govhub_authorization) 
       references govhub_authorizations;

    alter table govhub_authorizations 
       add constraint FK230g1y4m4hd9wiymu8pnxa7fu 
       foreign key (id_govhub_role) 
       references govhub_roles;

    alter table govhub_authorizations 
       add constraint FKlikt40mn09sy578se55fn8u17 
       foreign key (id_govhub_user) 
       references govhub_users;
