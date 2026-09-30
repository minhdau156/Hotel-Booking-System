-- data.sql (or V2__seed_data.sql if you use Flyway)

-- 1) Room types (4)
INSERT INTO room_types (name, base_price, capacity, description) VALUES
  ('Standard', 50.00,  2, 'Queen bed, city view, free Wi-Fi'),
  ('Deluxe',   80.00,  2, 'King bed, balcony, minibar'),
  ('Family',  120.00,  4, 'Two double beds, living area'),
  ('Suite',   200.00,  3, 'King bed, separate lounge, bathtub');

-- 2) Rooms (10), mapped to the types by name so it doesn't depend on generated IDs
INSERT INTO rooms (room_number, room_type_id, status) VALUES
  ('101', (SELECT id FROM room_types WHERE name = 'Standard'), 'AVAILABLE'),
  ('102', (SELECT id FROM room_types WHERE name = 'Standard'), 'AVAILABLE'),
  ('103', (SELECT id FROM room_types WHERE name = 'Standard'), 'AVAILABLE'),
  ('201', (SELECT id FROM room_types WHERE name = 'Deluxe'),   'AVAILABLE'),
  ('202', (SELECT id FROM room_types WHERE name = 'Deluxe'),   'AVAILABLE'),
  ('203', (SELECT id FROM room_types WHERE name = 'Deluxe'),   'MAINTENANCE'),
  ('301', (SELECT id FROM room_types WHERE name = 'Family'),   'AVAILABLE'),
  ('302', (SELECT id FROM room_types WHERE name = 'Family'),   'AVAILABLE'),
  ('401', (SELECT id FROM room_types WHERE name = 'Suite'),    'AVAILABLE'),
  ('402', (SELECT id FROM room_types WHERE name = 'Suite'),    'AVAILABLE');

-- 3) One ADMIN staff row (password: Admin@123, BCrypt-hashed)
INSERT INTO staff (username, password_hash, full_name, role, active) VALUES
  ('admin',
   '$2a$10$z/HGe3yXTMXwDB2ktzoDkunvuYhVwfBz8aP7Hk.u7/8lYJde.zyiS',
   'System Administrator',
   'ADMIN',
   TRUE);