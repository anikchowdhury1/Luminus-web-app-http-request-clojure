-- :name create-csv-list! :! :n
-- :doc creates a new csv list record
INSERT INTO csv_list
(uuid, date_time, input_file_path, output_file_path, input_file_name, output_file_name, status)
VALUES (:uuid, :date_time, :input_file_path, :output_file_path, :input_file_name, :output_file_name, :status)


-- :name get-all-csv-list  :n
-- :doc retrieves all csv list
SELECT * FROM csv_list
ORDER BY date_time DESC


-- :name get-csv-list-by-id :? :1
-- :doc retrieves all csv list
SELECT * FROM csv_list
WHERE uuid = :uuid


-- :name get-input-file-path-by-id :? :1
-- :doc get input file path for specific id
SELECT input_file_path FROM csv_list
WHERE id = :id


-- :name get-output-file-path-by-id :? :1
-- :doc get output file path for specific id
SELECT output_file_path FROM csv_list
WHERE id = :id

-- :name update-status :! :n
-- :doc update status
UPDATE csv_list
SET status = :status
WHERE uuid = :uuid;

-- :name get-N-csv-list :? :*
-- :doc retrieves N csv list
SELECT * FROM csv_list
ORDER BY date_time DESC LIMIT :n;

-- :name delete-csv-list! :! :n
-- :doc deletes a csv list record given the id
DELETE FROM csv_list
WHERE id = :id