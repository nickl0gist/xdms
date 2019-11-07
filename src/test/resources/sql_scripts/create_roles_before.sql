insert into roles
  (id, description, is_active, name)
values
  (1,'bla bla', true, 'ADMIN_ROLE'),
  (2,'bla bla', true, 'USER_ROLE'),
  (3,'bla bla', true, 'GUEST_ROLE'),
  (4,'bla bla', true, 'PLANNER_ROLE'),
  (5,'bla bla', true, 'TRAFFIC_ROLE');

insert into users
  (id,username, first_name, last_name, email, password, role_id)
VALUES
  (1,'Admin', 'Adm Name', 'Adm Last Name', 'admin@bla.pl', 'admin_pass', 1),
  (2,'User', 'User Name', 'User Last Name', 'user@bla.pl', 'user_pass', 2);