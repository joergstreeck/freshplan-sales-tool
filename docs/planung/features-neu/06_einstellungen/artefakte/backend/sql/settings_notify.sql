-- settings_notify.sql (Performance Layer)
CREATE OR REPLACE FUNCTION notify_settings_changed() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE sig text;
BEGIN
  IF (TG_OP = 'DELETE') THEN
    sig := md5(coalesce(OLD.tenant_id::text,'')||'|'||coalesce(OLD.territory,'')||'|'||coalesce(OLD.account_id::text,'')||'|'||coalesce(OLD.contact_role::text,'')||'|'||coalesce(OLD.contact_id::text,'')||'|'||coalesce(OLD.user_id::text,''));
  ELSE
    sig := md5(coalesce(NEW.tenant_id::text,'')||'|'||coalesce(NEW.territory,'')||'|'||coalesce(NEW.account_id::text,'')||'|'||coalesce(NEW.contact_role::text,'')||'|'||coalesce(NEW.contact_id::text,'')||'|'||coalesce(NEW.user_id::text,''));
  END IF;
  PERFORM pg_notify('settings_changed', sig);
  RETURN COALESCE(NEW, OLD);
END $$;

DROP TRIGGER IF EXISTS trg_settings_store_notify ON settings_store;
CREATE TRIGGER trg_settings_store_notify
AFTER INSERT OR UPDATE OR DELETE ON settings_store
FOR EACH ROW EXECUTE FUNCTION notify_settings_changed();
