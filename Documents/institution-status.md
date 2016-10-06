## Institution status

Every filing will store a `InstitutionStatus` object with the following structure:

* `status`: `String`

For example:

```json
{
      "institution": {
      "id": "12345",
      "name": "First Bank",
      "status": "active"
    }
```

In order to track the status of a financial institution, the following states are captured by the backend:

* `1`: `Active` - The institution is on the 2017 Panel
* `2`: `Inactive` - The institution is not on the 2017 Panel
