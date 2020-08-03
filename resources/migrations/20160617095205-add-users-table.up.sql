CREATE TABLE csv_list
(id BIGSERIAL PRIMARY KEY,
 uuid TEXT NOT NULL,
 date_time TEXT NOT NULL,
 input_file_path TEXT NOT NULL,
 output_file_path TEXT NOT NULL,
 input_file_name TEXT NOT NULL,
 output_file_name TEXT NOT NULL,
 status TEXT NOT NULL);