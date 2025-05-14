insert tbl_role (role) values ('admin');
insert tbl_role (role) values ('student');

insert tbl_user (user_name, user_password, user_fk_role)
       values ('admin', '$2a$12$gfTMWrXUwBU.eVPVYbz9C.dPg9kFfRCfL8oYa1TOZg63QCD8nKi1C', 1 );

insert tbl_user (user_name, user_password, user_fk_role)
       values ('ana', '$2a$12$Q6gFWzwrEUUiaF4kD1M3tOqvuV1N1txnf9hxZtkAk8jLb3U5Gjv.O', 2 );