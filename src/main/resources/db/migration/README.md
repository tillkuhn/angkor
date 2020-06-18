### drop all

```
drop table place; drop table region; drop table flyway_schema_history;
```

### add colums

```
ALTER TABLE place ADD COLUMN IF NOT EXISTS coordinates double precision[];
```
