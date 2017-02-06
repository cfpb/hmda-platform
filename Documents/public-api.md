# HMDA Platform Public API

This documenatation describes de public HMDA Platform HTTP API

## Institutions

### Search

* `/institutions?domain=<domain>`

   * `GET` - Returns a list of institutions filtered by their email domain. If none are found, an HTTP 404 error code (not found) is returned

   Example response, with HTTP code 200:

   ```json
   [
      {
        "id": "0",
        "name": "Bank 0",
        "domains": ["test@bank0.com"],
        "externalIds":[
          {
            "id": "1234",
            "idType": "occ-charter-id"
          },
          {
            "id": "1234",
            "idType": "ncua-charter-id"
          }
        ]
      }
   ]
   ```

