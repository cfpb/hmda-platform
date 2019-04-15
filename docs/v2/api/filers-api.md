## Filers API

### Filters

* `/filers/<filing year>`

   * `GET` - Returns a list of filers for the filing year defined in the URI.

Note : This endpoint is currently only supported for the 2018 filing year

Example response:

```json
{  
   "institutions":[  
      {  
         "lei":"12345677654321",
         "name":"Test Bank 1",
         "period":"2018"
      },
      {  
         "lei":"5734315621455",
         "name":"Test Bank 12",
         "period":"2018"
      }
   ]
}
```

* `/filers/<filing year>/<lei>/msaMds`

   * `GET` - Returns all MsaMds for the specified filer

Example response:

```json
{  
   "institution":{  
      "lei":"12345677654321",
      "name":"Test Bank 1",
      "period":"2018"
   },
   "msaMds":[  
      {  
         "id":"47664",
         "name":"FARMINGTON HILLS,MI"
      },
      {  
         "id":"19180",
         "name":"DANVILLE,IL"
      }
   ]
}
```