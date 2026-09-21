-- Runs automatically the first time the MySQL container starts.
-- One schema per microservice, per the brief's requirement that each
-- hazard service owns its own database schema.

CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS flood_db;
CREATE DATABASE IF NOT EXISTS drought_db;
CREATE DATABASE IF NOT EXISTS fire_db;
CREATE DATABASE IF NOT EXISTS zoonotic_db;
CREATE DATABASE IF NOT EXISTS mining_db;
CREATE DATABASE IF NOT EXISTS report_db;
CREATE DATABASE IF NOT EXISTS alert_db;
CREATE DATABASE IF NOT EXISTS dashboard_db;

-- One shared MySQL user, full access to all of the above, used by
-- every service's application.yml. Fine for a class project; in a
-- real deployment each service would get its own least-privilege user.
CREATE USER IF NOT EXISTS 'dpdms'@'%' IDENTIFIED BY 'dpdms_pass';
GRANT ALL PRIVILEGES ON auth_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON flood_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON drought_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON fire_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON zoonotic_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON mining_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON report_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON alert_db.* TO 'dpdms'@'%';
GRANT ALL PRIVILEGES ON dashboard_db.* TO 'dpdms'@'%';
FLUSH PRIVILEGES;
