INSERT INTO `user` (`id`, `firstname`, `lastname`, `username`, `password`, `role`, `workplace`, `email`)
VALUES (UUID_TO_BIN('567219c1-525a-44b1-93d3-383008a5a029'), 'John', 'Doe', 'testuser', '$2a$12$74wpAVSp3FZQbaKtdRMKH.rtuBknF8MA/0uNNCmwcrxRKGGt90EUi', 'SECRETARIAT', 'fav', 'johndoe@zcu.cz'),
(UUID_TO_BIN('117219c1-525a-44b1-93d3-383008a5a029'), 'John', 'Doe', 'testuser2', '$2a$12$74wpAVSp3FZQbaKtdRMKH.rtuBknF8MA/0uNNCmwcrxRKGGt90EUi', 'SECRETARIAT', 'fav', 'johndoe@zcu.cz');


INSERT INTO `project` (`id`, `name`, `manager_id`, `time_from`, `time_to`, `description`)
VALUES (UUID_TO_BIN('75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f'), 'Test project', UUID_TO_BIN('567219c1-525a-44b1-93d3-383008a5a029'), '2022-1-1', '2023-1-1', 'Test project description'),
(UUID_TO_BIN('22ae2ef7-8cf5-48d3-b03f-d137a5d43b1f'), 'Test project2', UUID_TO_BIN('567219c1-525a-44b1-93d3-383008a5a029'), '2022-1-1', '2023-1-1', 'Test project description');


INSERT INTO `assignment` (`id`, `worker_id`, `job_id`, `scope`, `time_from`, `time_to`, `note`, `status`)
VALUES (UUID_TO_BIN('99ae2ef7-8cf5-48d3-b03f-d137a5d43b1f'), UUID_TO_BIN('567219c1-525a-44b1-93d3-383008a5a029'), UUID_TO_BIN('75ae2ef7-8cf5-48d3-b03f-d137a5d43b1f'), 40, '2022-1-1', '2023-1-1', 'Test assignment of test user', 'Active');
