# Database schema for Identity Service

To back up, use:

```bash
    pg_dump --no-owner --no-privileges --format=plain -U postgres -h localhost -p 5432 mydb > mydb_backup.sql
```

To restore, use:

```bash
    psql -U postgres -h localhost -p 5432 -d mydb < mydb_backup.sql
```
