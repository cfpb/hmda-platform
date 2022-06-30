# HMDA Quarterly Data Service

### This module serves as the backend API for the quarterly data graphs
* HMDA Quarterly Data Service module is driven by data originated from LAR (Loan Application Register) records.
As most of the data generated here are aggregation, and percentile based,
MV (materialized views) are created to do the calculations in advance.  
* For each data calculation needed for graphs there is a SQL script used to generate the MV,
they can be found [here](./src/main/resources/ddl).
These templates generate one MV per period / LAR table instead of combining all data for all periods / tables,
which allows for future expandability, and shorter data calculation time as historical data would not need to be re-calculated.

### Data filters
For the data calculated in this iteration, consumer LARs are given focus, and we filter out commercial and other non-standard LARs:
  * Property is secured by first lien
  * Property is principal residence
  * Property has 1 to 4 units
  * Property is site built
  * Property is not primarily for business or commercial purpose
  * LAR is not a reverse mortgage
  * LAR is not interest only payment
  * LAR is not negative amortization
  * LAR is not balloon payment
  * LAR is either open end or closed end credit

### Deployment configurations
To allow for future expandability, the MV templates used by this module is stored as a configmap [here](../kubernetes/hmda-quarterly-data-service/templates/quarterly-mv-configmap.yaml).
With values like
```
APP_VOL: applications_volume
APP_VOL_PERIODS: 2018,2019,2020,2021
```
which derive into:
* `applications_volume_2018`
* `applications_volume_2019`
* `applications_volume_2020`
* `applications_volume_2021`

The above are actual MVs expected by the module to exist within RDS.

### Routes and code structure
Each object in [route](./src/main/scala/hmda/quarterly/data/api/route) aside from `package.scala` serves one graph,
and the [package](./src/main/scala/hmda/quarterly/data/api/route/package.scala) object contains all the route objects.  
* Response from the `/graphs` endpoint serves as the index, listing out all the available graphs,
defined in the [package](./src/main/scala/hmda/quarterly/data/api/route/package.scala) object:
  * ```json
    {
      "graphs": [
        {
          "category": "quantity",
          "endpoint": "applications",
          "title": "How has the number of applications changed?"
        },
        ...
      ],
      ...
    }
    ```
* Appending the `endpoint` from one of the `graphs` in the response (e.g. `/graphs/applications`) would retrieve the data for that graph.

## References
* [Materialized View Templates](./src/main/resources/ddl)
* [Deployment Helm Chart](../kubernetes/hmda-quarterly-data-service)
* [Routes Package](./src/main/scala/hmda/quarterly/data/api/route/package.scala)