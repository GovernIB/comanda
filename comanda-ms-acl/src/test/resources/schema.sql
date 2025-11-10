-- H2 schema for integration tests

-- Spring Security ACL tables (unprefixed)
CREATE TABLE IF NOT EXISTS acl_class (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
  class VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS acl_sid (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
  principal BOOLEAN NOT NULL,
  sid VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS acl_object_identity (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
  object_id_class BIGINT NOT NULL,
  object_id_identity BIGINT NOT NULL,
  parent_object BIGINT,
  owner_sid BIGINT,
  entries_inheriting BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS acl_entry (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
-- id BIGINT AUTO_INCREMENT PRIMARY KEY,
  acl_object_identity BIGINT NOT NULL,
  ace_order INTEGER NOT NULL,
  sid BIGINT NOT NULL,
  mask INTEGER NOT NULL,
  granting BOOLEAN NOT NULL,
  audit_success BOOLEAN NOT NULL,
  audit_failure BOOLEAN NOT NULL
);

CREATE INDEX IF NOT EXISTS acl_entry_idx_oi ON acl_entry(acl_object_identity);
CREATE INDEX IF NOT EXISTS acl_entry_idx_sid ON acl_entry(sid);

-- Business map table for ACL entries
CREATE TABLE IF NOT EXISTS com_acl_entry_map (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
--   id BIGINT AUTO_INCREMENT PRIMARY KEY,
  subject_type VARCHAR(16) NOT NULL,
  subject_value VARCHAR(128) NOT NULL,
  resource_type VARCHAR(32) NOT NULL,
  resource_id BIGINT NOT NULL,
  action VARCHAR(16) NOT NULL,
  effect VARCHAR(8) NOT NULL,
  created_by VARCHAR(64) NOT NULL,
  created_date TIMESTAMP NOT NULL,
  lastmod_by VARCHAR(64),
  lastmod_date TIMESTAMP,
  v BIGINT
);

CREATE INDEX IF NOT EXISTS com_acl_entry_map_idx_subject ON com_acl_entry_map(subject_type, subject_value);
CREATE INDEX IF NOT EXISTS com_acl_entry_map_idx_resource ON com_acl_entry_map(resource_type, resource_id);
