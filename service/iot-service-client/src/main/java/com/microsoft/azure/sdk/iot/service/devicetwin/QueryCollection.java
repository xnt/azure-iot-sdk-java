/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;


public class QueryCollection
{
    private static final String CONTINUATION_TOKEN_KEY = "x-ms-continuation";
    private static final String ITEM_TYPE_KEY = "x-ms-item-type";
    private static final String PAGE_SIZE_KEY = "x-ms-max-item-count";

    private int pageSize;
    private String query;
    private boolean isSqlQuery;

    private QueryType requestQueryType;
    private QueryType responseQueryType;

    private QueryCollectionResponse queryCollectionResponse;

    private IotHubConnectionString iotHubConnectionString;
    private URL url;
    private HttpMethod httpMethod;
    private long timeout;

    private boolean wasCollectionReturnedAlready = false;

    protected QueryCollection(String query, int pageSize, QueryType requestQueryType) throws IllegalArgumentException
    {
        //Codes_SRS_QUERYCOLLECTION_34_001: [If the provided query string is invalid or does not contain both SELECT and FROM, an IllegalArgumentException shall be thrown.]
        ParserUtility.validateQuery(query);

        //Codes_SRS_QUERYCOLLECTION_34_002: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        //Codes_SRS_QUERYCOLLECTION_34_004: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }

        //Codes_SRS_QUERYCOLLECTION_34_006: [This function shall save the provided query, pageSize, and requestQueryType.]
        this.pageSize = pageSize;
        this.query = query;
        this.requestQueryType = requestQueryType;

        this.responseQueryType = QueryType.UNKNOWN;
        this.queryCollectionResponse = null;

        //Codes_SRS_QUERYCOLLECTION_34_008: [The constructed QueryCollection will be a sql query type.]
        this.isSqlQuery = true;
    }

    protected QueryCollection(int pageSize, QueryType requestQueryType) throws IllegalArgumentException
    {
        //Codes_SRS_QUERYCOLLECTION_34_003: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("Page Size cannot be zero or negative");
        }

        //Codes_SRS_QUERYCOLLECTION_34_005: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
        if (requestQueryType == null || requestQueryType == QueryType.UNKNOWN)
        {
            throw new IllegalArgumentException("Cannot process a unknown type query");
        }

        //Codes_SRS_QUERYCOLLECTION_34_007: [This function shall save the provided pageSize, and requestQueryType.]
        this.pageSize = pageSize;
        this.requestQueryType = requestQueryType;

        this.query = null;
        this.responseQueryType = QueryType.UNKNOWN;
        this.queryCollectionResponse = null;

        //Codes_SRS_QUERYCOLLECTION_34_009: [The constructed QueryCollection will not be a sql query type.]
        this.isSqlQuery = false;
    }

    private void continueQuery(QueryOptions options) throws IOException, IotHubException
    {
        sendQueryRequest(this.iotHubConnectionString, this.url, this.httpMethod, this.timeout, options);
    }

    protected QueryCollectionResponse sendQueryRequest(IotHubConnectionString iotHubConnectionString,
                                          URL url,
                                          HttpMethod method,
                                          Long timeoutInMs,
                                          QueryOptions options) throws IOException, IotHubException
    {
        //Codes_SRS_QUERYCOLLECTION_34_010: [If the provided connection string, url, or method is null, an IllegalArgumentException shall be thrown.]
        if (iotHubConnectionString == null || url == null || method == null)
        {
            throw new IllegalArgumentException("Input parameters cannot be null");
        }

        //this.iotHubConnectionString = iotHubConnectionString;
        this.url = url;
        this.httpMethod = method;
        this.timeout = timeoutInMs;

        byte[] payload = null;
        Map<String, String> queryHeaders = new HashMap<>();

        if (options != null && options.getContinuationToken() != null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_011: [If the provided query options is not null and contains a continuation token, it shall be put in the query headers to continue the query.]
            queryHeaders.put(CONTINUATION_TOKEN_KEY, options.getContinuationToken());
        }
        else if (this.queryCollectionResponse != null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_012: [If a continuation token is not provided from the passed in query options, but there is a continuation token saved in the latest queryCollectionResponse, that token shall be put in the query headers to continue the query.]
            queryHeaders.put(CONTINUATION_TOKEN_KEY, this.queryCollectionResponse.getContinuationToken());
        }

        if (options != null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_013: [If the provided query options is not null, the query option's page size shall be included in the query headers.]
            queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(options.getPageSize()));
        }
        else
        {
            //Codes_SRS_QUERYCOLLECTION_34_014: [If the provided query options is null, this object's page size shall be included in the query headers.]
            queryHeaders.put(PAGE_SIZE_KEY, String.valueOf(this.pageSize));
        }

        DeviceOperations.setHeaders(queryHeaders);


        if (isSqlQuery)
        {
            //Codes_SRS_QUERYCOLLECTION_34_015: [If this is a sql query, the payload of the query message shall be set to the json bytes representation of this object's query string.]
            payload = new QueryRequestParser(this.query).toJson().getBytes();
        }
        else
        {
            //Codes_SRS_QUERYCOLLECTION_34_016: [If this is not a sql query, the payload of the query message shall be set to empty bytes.]
            payload = new byte[0];
        }

        //Codes_SRS_QUERYCOLLECTION_34_017: [This function shall send an HTTPS request using DeviceOperations.]
        HttpResponse httpResponse = DeviceOperations.request(iotHubConnectionString, url, method, payload, null, timeoutInMs);

        //Codes_SRS_QUERYCOLLECTION_34_018: [The method shall read the continuation token (x-ms-continuation) and reponse type (x-ms-item-type) from the HTTP Headers and save it.]
        Map<String, String> headers = httpResponse.getHeaderFields();
        String newContinuationToken = null;
        for (Map.Entry<String, String> header : headers.entrySet())
        {
            switch (header.getKey())
            {
                case CONTINUATION_TOKEN_KEY:
                    newContinuationToken = header.getValue();
                    break;
                case ITEM_TYPE_KEY:
                    this.responseQueryType = QueryType.fromString(header.getValue());
                    break;
                default:
                    break;
            }
        }

        if (this.responseQueryType == null || this.responseQueryType == QueryType.UNKNOWN)
        {
            //Codes_SRS_QUERYCOLLECTION_34_019: [If the response type is Unknown or not found then this method shall throw IOException.]
            throw new IOException("Query response type is not defined by IotHub");
        }

        if (this.requestQueryType != this.responseQueryType)
        {
            //Codes_SRS_QUERYCOLLECTION_34_020: [If the request type and response does not match then the method shall throw IOException.]
            throw new IOException("Query response does not match query request");
        }

        //Codes_SRS_QUERYCOLLECTION_34_021: [The method shall create a QueryResponse object with the contents from the response body and save it.]
        this.queryCollectionResponse = new QueryCollectionResponse(new String(httpResponse.getBody()), newContinuationToken);
        return this.queryCollectionResponse;
    }

    protected boolean hasNext() throws IOException, IotHubException
    {
        //Codes_SRS_QUERYCOLLECTION_34_024: [If this object's latest query retrieval has not been given to the user yet, this function shall return true.]
        //Codes_SRS_QUERYCOLLECTION_34_025: [If this object's queryCollectionResponse object has a continuation token, this function shall continue that query and return true if it still has results left.]
        //Codes_SRS_QUERYCOLLECTION_34_026: [If this object's queryCollectionResponse object has no continuation token, this function shall return false.]
        return this.hasNext(new QueryOptions());
    }

    protected boolean hasNext(QueryOptions options) throws IOException, IotHubException
    {
        if (options == null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_030: [If the provided query options are null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("options cannot be null");
        }
        else if (!this.wasCollectionReturnedAlready)
        {
            //Codes_SRS_QUERYCOLLECTION_34_027: [If this object's latest query retrieval has not been given to the user yet, this function shall return true.]
            return true;
        }
        else if (options.getContinuationToken() != null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_028: [If the provided query options has a continuation token, this function shall continue that query and return true if it still has results left.]
            this.continueQuery(options);
            this.wasCollectionReturnedAlready = false;
            return (this.queryCollectionResponse != null);
        }
        else if (this.queryCollectionResponse.getContinuationToken() != null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_031: [If this object's queryCollectionResponse object has a continuation token, this function shall continue that query and return true if it still has results left.]
            QueryOptions newOptions = new QueryOptions();
            newOptions.setContinuationToken(this.queryCollectionResponse.getContinuationToken());
            this.continueQuery(newOptions);
            this.wasCollectionReturnedAlready = false;
            return (this.queryCollectionResponse != null);
        }

        //Codes_SRS_QUERYCOLLECTION_34_029: [In all other cases, this function shall return false.]
        return false;
    }

    protected QueryCollectionResponse<?> next() throws IOException, IotHubException, NoSuchElementException
    {
        if (this.hasNext())
        {
            //Codes_SRS_QUERYCOLLECTION_34_032: [If this object has a next set to return, this function shall return it.]
            wasCollectionReturnedAlready = true;
            return this.queryCollectionResponse;
        }
        else
        {
            //Codes_SRS_QUERYCOLLECTION_34_033: [If this object does not have a next set to return, this function shall return null.]
            return null;
        }
    }

    protected QueryCollectionResponse<?> next(QueryOptions options) throws IOException, IotHubException, NoSuchElementException
    {
        if (this.hasNext(options))
        {
            //Codes_SRS_QUERYCOLLECTION_34_034: [If this object has a next set to return using the provided query options, this function shall return it.]
            wasCollectionReturnedAlready = true;
            return this.queryCollectionResponse;
        }
        else
        {
            //Codes_SRS_QUERYCOLLECTION_34_035: [If this object does not have a next set to return, this function shall return null.]
            return null;
        }
    }

    protected String getContinuationToken()
    {
        if (this.queryCollectionResponse == null)
        {
            //Codes_SRS_QUERYCOLLECTION_34_022: [If this object's queryCollectionResponse is null, this function shall return null.]
            return null;
        }

        //Codes_SRS_QUERYCOLLECTION_34_023: [This function shall return this object's queryCollectionResponse's continuation token.]
        return this.queryCollectionResponse.getContinuationToken();
    }

    //todo add to devdoc an add test
    protected int getPageSize()
    {
        return this.pageSize;
    }
}
