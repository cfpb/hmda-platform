# [HMDA Platform](../README.md)

The HMDA Platform is composed of the following modules:
### Parser (JS/JVM)

Module responsible for reading incoming data and making sure that it conforms to the HMDA File Specification

### Data Validation
Module responsible for validating incoming data by executing validation rules as per the Edit Checks documentation

### Persistence
Module responsible for persisting information into the system. It becomes the system of record for HMDA data

### Cluster
Module responsible for managing the various cluster roles, as well as starting the Hmda Platform

### API
This module contains both public APIs for HMDA data for general use by third party clients and web applications, as well as endpoints for receiving data and providing information about the filing process for Financial Institutions

### API Model
This module contains objects and JSON protocols for use by the API project

### Query
This module is responsible for interacting with the back-end database, as well as conversion between model objects and database objects.

### Panel
This module is responsible for parsing and persisting a CSV-format panel file

### Model (JS/JVM)
This module is responsible for maintaining the objects used in our platform

### Census
This module is responsible for geographic translation (e.g. state number -> state code)

### Publication
This module generates Aggregate and Disclosure reports, as required by HMDA statute.

### Docker run  
To run a container with the `HMDA Platform` filing application as a single node cluster:  
`docker run -e CASSANDRA_CLUSTER_HOSTS=localhost --rm -ti -p 8080:8080 -p 8081:8081 -p 8082:8082 hmda/hmda-platform` 

## Resources

### API Documentation

### Development
* [Local development](development/local-platform.md)
* [Kubernetes](development/kubernetes.md)
* [Specific MSK and Keyspaces configuration](development/platform-msk-keyspaces.md)
* [Postman configuration](development/postman.md)

### Data Specifications

* [TS File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/spec/Public_File_TS_Spec.csv)
* [LAR File Spec](https://github.com/cfpb/hmda-platform/blob/master/docs/spec/Public_File_LAR_Spec.csv)
* [Institution Data Model Spec](spec/2018_Institution_Data_Model_Spec.csv)

