## Filing status

Every filing will store a `FilingStatus` object with the following structure:

* `code`: `Integer`
* `message`: `String`

For example:

```json
{
  "id": 3,
  "status": {
    "code": 1,
    "message": "not-started"
  }
}
```

In order to track the status of a filing for a financial institution, the following states are captured by the backend:

* `1`: `not-started` - A filing has been opened but no submission has been submitted for the filing.
* `2`: `in-progress` - At least one submission has been submitted for the filing but no submission has the status of signed.
* `3`: `completed` - A signed submission has been submitted for this filing, signifying that the filing is complete.
* `-1`: `cancelled` - The filing has been cancelled, signifying that the filing is no longer required.
