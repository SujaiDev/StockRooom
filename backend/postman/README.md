# Postman API Checks

Import `Stockroom.postman_collection.json` into Postman. Optionally import and select `Stockroom-Local.postman_environment.json`.

The collection is ordered as an end-to-end demo: demo-admin login, expected signup/profile restrictions, category/warehouse/location/product setup, stock movements, receipts/deliveries/transfers/adjustments, dashboard reads, then product cleanup. It captures the bearer token and created resource IDs as it runs. Generated resource names include a per-run timestamp where needed, so the collection can be run repeatedly against the same H2 process.

## Local Demo Server

From `backend/`, start the API on the H2 test database:

```powershell
.\gradlew.bat bootTestRun
```

The built-in `admin` / `1234` account has no database user row, so password-reset verification is not available for it. Signup and profile edits are deliberately rejected in this single-user demo. For real accounts and password reset, configure persistent database-backed users and an email delivery provider. Never deploy the hardcoded demo credential.

The application uses an ephemeral JWT signing key when `JWT_SECRET` is unset. Tokens are valid only until the API process restarts. Configure a stable secret of at least 32 bytes for longer sessions.

## cURL Import Example

To import one request from cURL in Postman, use **Import > Raw text** and paste:

```sh
curl --request GET \
  --url http://localhost:8080/api/health \
  --header 'Accept: application/json'
```

For the full endpoint suite, import the collection JSON instead of importing requests one at a time.