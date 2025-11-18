create or replace function current_value(p_table_name varchar2) return number is
  v_next number;
begin
  for rec in (select sequence_name from user_tab_identity_cols where table_name = p_table_name) loop
    execute immediate 'select ' || rec.sequence_name || '.currval from dual' into v_next;
    return v_next;
  end loop;
  return null;
end current_value;
/