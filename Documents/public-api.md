# HMDA Platform Public API

This documenatation describes de public HMDA Platform HTTP API

## Institutions

### Search

* `/institutions?domain=<domain>`

   * `GET` - Returns a list of institutions filtered by their email domain. If none are found, an HTTP 404 error code (not found) is returned

   Example response, with HTTP code 200:

   ```json
   {
     "institutions":
     [
        {
          "id": "0",
          "name": "Bank 0",
          "domains": ["test@bank0.com"],
          "externalIds":[
            {
              "name": "1234",
              "value": "occ-charter-id"
            },
            {
              "name": "1234",
              "value": "ncua-charter-id"
            }
          ]
        }
     ]
   ```

