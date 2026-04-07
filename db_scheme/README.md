# Database schema for Product Storage Service

To back up, use:

```bash
pg_dump --no-owner --no-privileges --format=plain -U postgres -h localhost -p 5432 product_storage_db > product_storage_db_backup.sql
```

To restore, use:

```bash
psql -U postgres -h localhost -p 5432 -d product_storage_db < product_storage_db_backup.sql
```
