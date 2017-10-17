# QueryCollection Requirements

## Overview

A QueryCollection is used to send query request to IotHub for twins, jobs, raw request or device jobs. 
This class also parses a response to query. It returns the full page of results for a query.

## References

## Exposed API

```java
public class QueryCollection
{
    protected QueryCollection(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException;
    protected QueryCollection(int pageSize, QueryType requestQueryType) throws IllegalArgumentException;

    protected QueryCollectionResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                          URL url,
                                          HttpMethod method,
                                          Long timeoutInMs,
                                          QueryOptions options) throws IOException, IotHubException;

    protected boolean hasNext() throws IOException, IotHubException;
    protected QueryCollectionResponse<?> next() throws IOException, IotHubException, NoSuchElementException;

    protected boolean hasNext(QueryOptions options) throws IOException, IotHubException;
    protected QueryCollectionResponse<?> next(QueryOptions options) throws IOException, IotHubException, NoSuchElementException;

    protected String getContinuationToken();
}
```

### QueryCollection

```java
protected QueryCollection(String query, int pageSize, QueryType requestQueryType);
```

**SRS_QUERYCOLLECTION_34_001: [**If the provided query string is invalid or does not contain both SELECT and FROM, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_002: [**If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_004: [**If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_006: [**This function shall save the provided query, pageSize, and requestQueryType.**]**

**SRS_QUERYCOLLECTION_34_008: [**The constructed QueryCollection will be a sql query type.**]**


```java
protected QueryCollection(int pageSize, QueryType requestQueryType);
```

**SRS_QUERYCOLLECTION_34_003: [**If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_005: [**If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_007: [**This function shall save the provided pageSize, and requestQueryType.**]**

**SRS_QUERYCOLLECTION_34_009: [**The constructed QueryCollection will not be a sql query type.**]**


### sendQueryRequest

```java
protected QueryCollectionResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString, URL url, HttpMethod method, Long timeoutInMs, QueryOptions options)
```

**SRS_QUERYCOLLECTION_34_010: [**If the provided connection string, url, or method is null, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_011: [**If the provided query options is not null and contains a continuation token, it shall be put in the query headers to continue the query.**]**

**SRS_QUERYCOLLECTION_34_012: [**If a continuation token is not provided from the passed in query options, but there is a continuation token saved in the latest queryCollectionResponse, that token shall be put in the query headers to continue the query.**]**

**SRS_QUERYCOLLECTION_34_013: [**If the provided query options is not null, the query option's page size shall be included in the query headers.**]**

**SRS_QUERYCOLLECTION_34_014: [**If the provided query options is null, this object's page size shall be included in the query headers.**]**

**SRS_QUERYCOLLECTION_34_015: [**If this is a sql query, the payload of the query message shall be set to the json bytes representation of this object's query string.**]**

**SRS_QUERYCOLLECTION_34_016: [**If this is not a sql query, the payload of the query message shall be set to empty bytes.**]**

**SRS_QUERYCOLLECTION_34_017: [**This function shall send an HTTPS request using DeviceOperations.**]**

**SRS_QUERYCOLLECTION_34_018: [**The method shall read the continuation token (x-ms-continuation) and reponse type (x-ms-item-type) from the HTTP Headers and save it.**]**

**SRS_QUERYCOLLECTION_34_019: [**If the response type is Unknown or not found then this method shall throw IOException.**]**

**SRS_QUERYCOLLECTION_34_020: [**If the request type and response does not match then the method shall throw IOException.**]**

**SRS_QUERYCOLLECTION_34_021: [**The method shall create a QueryResponse object with the contents from the response body and save it.**]**


### getContinuationToken

```java
private String getContinuationToken();   
```

**SRS_QUERYCOLLECTION_34_022: [**If this object's queryCollectionResponse is null, this function shall return null.**]**

**SRS_QUERYCOLLECTION_34_023: [**This function shall return this object's queryCollectionResponse's continuation token.**]**


### hasNext

```java
protected boolean hasNext();
```

**SRS_QUERYCOLLECTION_34_024: [**If this object's latest query retrieval has not been given to the user yet, this function shall return true.**]**

**SRS_QUERYCOLLECTION_34_025: [**If this object's queryCollectionResponse object has a continuation token, this function shall continue that query and return true if it still has results left.**]**

**SRS_QUERYCOLLECTION_34_026: [**If this object's queryCollectionResponse object has no continuation token, this function shall return false.**]**


```java
protected boolean hasNext(QueryOptions options);
```

**SRS_QUERYCOLLECTION_34_030: [**If the provided query options are null, an IllegalArgumentException shall be thrown.**]**

**SRS_QUERYCOLLECTION_34_027: [**If this object's latest query retrieval has not been given to the user yet, this function shall return true.**]**

**SRS_QUERYCOLLECTION_34_028: [**If the provided query options has a continuation token, this function shall continue that query and return true if it still has results left.**]**

**SRS_QUERYCOLLECTION_34_031: [**If this object's queryCollectionResponse object has a continuation token, this function shall continue that query and return true if it still has results left.**]**

**SRS_QUERYCOLLECTION_34_029: [**In all other cases, this function shall return false.**]**


### next

```java
protected QueryCollectionResponse<?> next();
```

**SRS_QUERYCOLLECTION_34_032: [**If this object has a next set to return, this function shall return it.**]**

**SRS_QUERYCOLLECTION_34_033: [**If this object does not have a next set to return, this function shall return null.**]**


```java
protected QueryCollectionResponse<?> next(QueryOptions options);
```

**SRS_QUERYCOLLECTION_34_034: [**If this object has a next set to return using the provided query options, this function shall return it.**]**

**SRS_QUERYCOLLECTION_34_035: [**If this object does not have a next set to return, this function shall return null.**]**
