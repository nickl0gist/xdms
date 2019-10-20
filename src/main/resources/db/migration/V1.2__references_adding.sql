insert into reference
  (number, name, hs_code, pcs_perhu, pcs_perpu, weight,
   is_active, pallet_height, pallet_length, pallet_width, pallet_weight, 
    stackability, weightpu, weight_hu, designationen, designationru) 
values
  ("1234XXX", "Head Rest", "80090905", 1000, 100, 1.0,
   true, 1000, 1200, 800, 500.5, 2, 1.0, 15,
   "Head Rest", "Подголовник"),
  ("880055X-CO", "Anchor", "71190900", 2000, 100, 1,
   true, 1000, 1200, 800, 500.5, 2, 1, 15,
   "Head Rest", "Ручник");
