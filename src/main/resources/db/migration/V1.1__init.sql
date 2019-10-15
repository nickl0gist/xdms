insert into roles
  (description, is_active, name)
values
  ("bla bla", true, "ADMIN_ROLE"),
  ("bla bla", true, "USER_ROLE");

insert into users
  (email, first_name, last_name, password, username, role_id)
values
  ("booo@bo.com", "bobo", "bobo", "123wsd", "BoBo", 1),
  ("yuammy@yu.me", "youm", "youm", "123wsd", "YMmm", 2);