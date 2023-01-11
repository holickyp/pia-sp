CREATE TABLE IF NOT EXISTS `user` (
  `id` binary(16) NOT NULL,
  `firstname` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `username` varchar(255) NOT NULL,
  `password` varchar(64) NOT NULL,
  `role` varchar(255) NOT NULL,
  `workplace` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY `id` (`id`),
  UNIQUE `UK_username` (`username`)
);

CREATE TABLE IF NOT EXISTS `subordinate` (
  `id` binary(16) NOT NULL,
  `superior_id` binary(16) NOT NULL,
  `subordinate_id` binary(16) NOT NULL,
   PRIMARY KEY `id` (`id`),
   UNIQUE `UK_subordinate` (`superior_id`, `subordinate_id`),
   CONSTRAINT `FK_superior` FOREIGN KEY (`superior_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
   CONSTRAINT `FK_subordinate` FOREIGN KEY (`subordinate_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `project` (
  `id` binary(16) NOT NULL,
  `name` varchar(255) NOT NULL,
  `manager_id` binary(16) NOT NULL,
  `time_from` date NOT NULL,
  `time_to` date NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY `id` (`id`),
  UNIQUE `UK_name` (`name`),
  CONSTRAINT `FK_project_manager` FOREIGN KEY (`manager_id`) REFERENCES `user` (`id`)
);

CREATE TABLE IF NOT EXISTS `assignment` (
  `id` binary(16) NOT NULL,
  `worker_id` binary(16) NOT NULL,
  `job_id` binary(16) NOT NULL,
  `scope` double NOT NULL CHECK (`scope` BETWEEN 0 and 40),
  `time_from` date NOT NULL,
  `time_to` date NOT NULL,
  `note` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  PRIMARY KEY `id` (`id`),
  UNIQUE `UK_assignment` (`worker_id`,`job_id`),
  CONSTRAINT `FK_worker` FOREIGN KEY (`worker_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_project` FOREIGN KEY (`job_id`) REFERENCES `project` (`id`) ON DELETE CASCADE
);

-- insert user if missing, otherwise ignore
INSERT INTO `user` (`id`, `firstname`, `lastname`, `username`, `password`, `role`, `workplace`, `email`)
VALUES (UUID_TO_BIN(UUID()), 'John', 'Doe', 'admin', '$2a$12$74wpAVSp3FZQbaKtdRMKH.rtuBknF8MA/0uNNCmwcrxRKGGt90EUi', 'SECRETARIAT', 'fav', 'johndoe@zcu.cz'),
(UUID_TO_BIN(UUID()), 'Jane', 'Doe', 'dmanager', '$2a$12$ghF5wb9ygV0h6guFztcCeOE5wRg5KNNPrFnLMain9li/fjbEPy3Cu', 'DEPARTMENT-MANAGER', 'fav', 'janedoe@zcu.cz'),
(UUID_TO_BIN(UUID()), 'Robert', 'Smith', 'pmanager', '$2a$12$ghF5wb9ygV0h6guFztcCeOE5wRg5KNNPrFnLMain9li/fjbEPy3Cu', 'PROJECT-MANAGER', 'fav', 'robertsmith@zcu.cz'),
(UUID_TO_BIN(UUID()), 'Briskit', 'Himchin', 'superior', '$2a$12$ghF5wb9ygV0h6guFztcCeOE5wRg5KNNPrFnLMain9li/fjbEPy3Cu', 'SUPERIOR', 'fav', 'briskithimchin@zcu.cz'),
(UUID_TO_BIN(UUID()), 'Emma', 'Williams', 'default', '$2a$12$ghF5wb9ygV0h6guFztcCeOE5wRg5KNNPrFnLMain9li/fjbEPy3Cu', 'REGULAR-USER', 'fav', 'emmawilliams@zcu.cz'),
(UUID_TO_BIN(UUID()), 'Jack', 'Porter', 'default2', '$2a$12$ghF5wb9ygV0h6guFztcCeOE5wRg5KNNPrFnLMain9li/fjbEPy3Cu', 'REGULAR-USER', 'fav', 'jackporter@zcu.cz')
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

-- insert project if missing, otherwise ignore
INSERT INTO `project` (`id`, `name`, `manager_id`, `time_from`, `time_to`, `description`)
VALUES (UUID_TO_BIN(UUID()), 'Default project', (SELECT `id` FROM `user` WHERE `username` = 'dmanager'), '2022-1-1', '2023-1-1', 'default project description'),
(UUID_TO_BIN(UUID()), 'Second default project', (SELECT `id` FROM `user` WHERE `username` = 'pmanager'), '2023-1-1', '2023-12-31', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- insert assignment if missing, otherwise ignore
INSERT INTO `assignment` (`id`, `worker_id`, `job_id`, `scope`, `time_from`, `time_to`, `note`, `status`)
VALUES (UUID_TO_BIN(UUID()), (SELECT `id` FROM `user` WHERE `username` = 'default'), (SELECT `id` FROM `project` WHERE `name` = 'Default project'), 40, '2022-1-1', '2023-1-1', 'default assignment of basic user', 'Active'),
(UUID_TO_BIN(UUID()), (SELECT `id` FROM `user` WHERE `username` = 'dmanager'), (SELECT `id` FROM `project` WHERE `name` = 'Default project'), 16, '2022-1-1', '2023-1-1', 'default assignment of department manager', 'Active'),
(UUID_TO_BIN(UUID()), (SELECT `id` FROM `user` WHERE `username` = 'pmanager'), (SELECT `id` FROM `project` WHERE `name` = 'Second default project'), 20, '2022-1-1', '2023-1-1', 'default assignment of project manager', 'Active')
ON DUPLICATE KEY UPDATE `note` = VALUES(`note`);

-- insert subordinate if missing, otherwise ignore
INSERT INTO `subordinate` (`id`, `superior_id`, `subordinate_id`)
VALUES (UUID_TO_BIN(UUID()), (SELECT `id` FROM `user` WHERE `username` = 'pmanager'), (SELECT `id` FROM `user` WHERE `username` = 'default2')),
(UUID_TO_BIN(UUID()), (SELECT `id` FROM `user` WHERE `username` = 'superior'), (SELECT `id` FROM `user` WHERE `username` = 'default'))
ON DUPLICATE KEY UPDATE `id` = VALUES(`id`);
