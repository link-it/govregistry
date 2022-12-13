
    alter table govhub_assignable_roles 
       drop constraint FKhibyeyvi0hcnqj0abi71v4bpr;

    alter table govhub_assignable_roles 
       drop constraint FKhn9ny7ah3jhy6pvl4cv5pgwr4;

    alter table govhub_auth_organizations 
       drop constraint FKgxmwbdowwv5epo9vw4c921rj4;

    alter table govhub_auth_organizations 
       drop constraint FKib4rr02po89yyjufqjkxfxmkf;

    alter table govhub_auth_services 
       drop constraint FKrskc3e6hnbg3i3silh74djwnb;

    alter table govhub_auth_services 
       drop constraint FKp2cmsa6rj69resrgjemt6y1bi;

    alter table govhub_authorizations 
       drop constraint FK230g1y4m4hd9wiymu8pnxa7fu;

    alter table govhub_authorizations 
       drop constraint FKlikt40mn09sy578se55fn8u17;

    drop table if exists govhub_assignable_roles cascade;

    drop table if exists govhub_auth_organizations cascade;

    drop table if exists govhub_auth_services cascade;

    drop table if exists govhub_authorizations cascade;

    drop table if exists govhub_organizations cascade;

    drop table if exists govhub_roles cascade;

    drop table if exists govhub_services cascade;

    drop table if exists govhub_users cascade;

    drop sequence if exists seq_govhub_authorizations;

    drop sequence if exists seq_govhub_organizations;

    drop sequence if exists seq_govhub_roles;

    drop sequence if exists seq_govhub_services;

    drop sequence if exists seq_govhub_users;
