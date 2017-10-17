/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.serializer.QueryRequestParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for QueryCollection.java
 * Methods:
 * Lines:
 */
public class QueryCollectionTest
{
    @Mocked
    ParserUtility mockParserUtility;

    @Mocked
    QueryRequestParser mockQueryRequestParser;

    @Mocked
    IotHubConnectionString iotHubConnectionString;

    @Mocked
    HttpResponse mockHttpResponse;

    @Mocked
    QueryCollectionResponse mockQueryCollectionResponse;

    @Mocked
    QueryOptions mockQueryOptions;

    @Mocked
    IotHubConnectionString mockConnectionString;

    @Mocked
    URL mockUrl;

    @Mocked
    HttpMethod mockHttpMethod;

    @Mocked
    DeviceOperations mockDeviceOperations;

    private static final String expectedContinuationToken = "someContinuationToken";
    private static final long expectedTimeout = 10000;
    private static HashMap<String, String> expectedHeaders;

    @BeforeClass
    public static void initializeExpectedValues()
    {
        expectedHeaders = new HashMap<>();
        expectedHeaders.put("x-ms-max-item-count", "0");
        expectedHeaders.put("x-ms-continuation", expectedContinuationToken);
    }

    //Tests_SRS_QUERYCOLLECTION_34_001: [If the provided query string is invalid or does not contain both SELECT and FROM, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForInvalidSqlQueryString()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockParserUtility.validateQuery((String) any);
                result = new IllegalArgumentException();
            }
        };

        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class},"anyString", 20, QueryType.DEVICE_JOB);
    }
    
    //Tests_SRS_QUERYCOLLECTION_34_002: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNegativePageSize()
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class},"anyString", -1, QueryType.DEVICE_JOB);
    }

    //Tests_SRS_QUERYCOLLECTION_34_003: [If the provided page size is not a positive integer, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForZeroPageSize()
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class},0, QueryType.DEVICE_JOB);

    }

    //Tests_SRS_QUERYCOLLECTION_34_004: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullQueryType()
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class},14, null);
    }

    //Tests_SRS_QUERYCOLLECTION_34_005: [If the provided QueryType is null or UNKNOWN, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForUnknownQueryType()
    {
        //act
        Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class},20, QueryType.UNKNOWN);
    }

    //Tests_SRS_QUERYCOLLECTION_34_006: [This function shall save the provided query, pageSize, and requestQueryType.]
    //Tests_SRS_QUERYCOLLECTION_34_008: [The constructed QueryCollection will be a sql query type.]
    @Test
    public void constructorSavesQueryPageSizeAndQueryType()
    {
        //arrange
        String expectedQuery = "someQuery";
        int expectedPageSize = 22;
        QueryType expectedQueryType = QueryType.JOB_RESPONSE;

        //act
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class},expectedQuery, expectedPageSize, expectedQueryType);

        //assert
        String actualQuery = Deencapsulation.getField(queryCollection, "query");
        int actualPageSize = Deencapsulation.getField(queryCollection, "pageSize");
        QueryType actualQueryType = Deencapsulation.getField(queryCollection, "requestQueryType");
        boolean actualIsSqlQuery = Deencapsulation.getField(queryCollection, "isSqlQuery");

        assertEquals(expectedQuery, actualQuery);
        assertEquals(expectedPageSize, actualPageSize);
        assertEquals(expectedQueryType, actualQueryType);
        assertTrue(actualIsSqlQuery);
    }

    //Tests_SRS_QUERYCOLLECTION_34_007: [This function shall save the provided pageSize, and requestQueryType.]
    //Tests_SRS_QUERYCOLLECTION_34_009: [The constructed QueryCollection will not be a sql query type.]
    @Test
    public void constructorSavesPageSizeAndQueryType()
    {
        //arrange
        int expectedPageSize = 22;
        QueryType expectedQueryType = QueryType.JOB_RESPONSE;

        //act
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {int.class, QueryType.class}, expectedPageSize, expectedQueryType);

        //assert
        int actualPageSize = Deencapsulation.getField(queryCollection, "pageSize");
        QueryType actualQueryType = Deencapsulation.getField(queryCollection, "requestQueryType");
        boolean actualIsSqlQuery = Deencapsulation.getField(queryCollection, "isSqlQuery");

        assertEquals(expectedPageSize, actualPageSize);
        assertEquals(expectedQueryType, actualQueryType);
        assertFalse(actualIsSqlQuery);
    }

    //Tests_SRS_QUERYCOLLECTION_34_010: [If the provided connection string, url, or method is null, an IllegalArgumentException shall be thrown.]
    //Tests_SRS_QUERYCOLLECTION_34_011: [If the provided query options is not null and contains a continuation token, it shall be put in the query headers to continue the query.]
    //Tests_SRS_QUERYCOLLECTION_34_012: [If a continuation token is not provided from the passed in query options, but there is a continuation token saved in the latest queryCollectionResponse, that token shall be put in the query headers to continue the query.]
    //Tests_SRS_QUERYCOLLECTION_34_013: [If the provided query options is not null, the query option's page size shall be included in the query headers.]
    //Tests_SRS_QUERYCOLLECTION_34_014: [If the provided query options is null, this object's page size shall be included in the query headers.]
    //Tests_SRS_QUERYCOLLECTION_34_015: [If this is a sql query, the payload of the query message shall be set to the json bytes representation of this object's query string.]
    //Tests_SRS_QUERYCOLLECTION_34_016: [If this is not a sql query, the payload of the query message shall be set to empty bytes.]
    //Tests_SRS_QUERYCOLLECTION_34_017: [This function shall send an HTTPS request using DeviceOperations.]
    //Tests_SRS_QUERYCOLLECTION_34_018: [The method shall read the continuation token (x-ms-continuation) and reponse type (x-ms-item-type) from the HTTP Headers and save it.]
    //Tests_SRS_QUERYCOLLECTION_34_019: [If the response type is Unknown or not found then this method shall throw IOException.]
    //Tests_SRS_QUERYCOLLECTION_34_020: [If the request type and response does not match then the method shall throw IOException.]
    //Tests_SRS_QUERYCOLLECTION_34_021: [The method shall create a QueryResponse object with the contents from the response body and save it.]

    //Tests_SRS_QUERYCOLLECTION_34_022: [If this object's queryCollectionResponse is null, this function shall return null.]
    @Test
    public void getContinuationTokenWithoutQueryCollectionResponse()
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", null);

        //act
        String actualContinuationToken = Deencapsulation.invoke(queryCollection, "getContinuationToken");

        //assert
        assertNull(actualContinuationToken);
    }

    //Tests_SRS_QUERYCOLLECTION_34_023: [This function shall return this object's queryCollectionResponse's continuation token.]
    @Test
    public void getContinuationTokenGetsFromQueryCollectionResponse()
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        new NonStrictExpectations()
        {
            {
                mockQueryCollectionResponse.getContinuationToken();
                result = expectedContinuationToken;
            }
        };

        //act
        String actualContinuationToken = Deencapsulation.invoke(queryCollection, "getContinuationToken");

        //assert
        assertEquals(expectedContinuationToken, actualContinuationToken);
    }

    //Tests_SRS_QUERYCOLLECTION_34_024: [If this object's latest query retrieval has not been given to the user yet, this function shall return true.]
    @Test
    public void hasNextReturnsTrueIfRetrievalNotGivenYet() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);

        Deencapsulation.setField(queryCollection, "wasCollectionReturnedAlready", false);

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext");

        //assert
        assertTrue(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_025: [If this object's queryCollectionResponse object has a continuation token, this function shall continue that query and return true if it still has results left.]
    @Test
    public void hasNextFetchesNextIfContinuationTokenPresent() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        //only mocking one method for QueryCollection, not the whole class
        new NonStrictExpectations(queryCollection)
        {
            {
                Deencapsulation.invoke(queryCollection, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, long.class, QueryOptions.class}, mockConnectionString, mockUrl, mockHttpMethod, expectedTimeout, mockQueryOptions);
                result = mockQueryCollectionResponse;
            }
        };

        new NonStrictExpectations()
        {
            {
                mockQueryCollectionResponse.getContinuationToken();
                result = null;
                DeviceOperations.request(mockConnectionString, mockUrl, mockHttpMethod, (byte[]) any, null, expectedTimeout);
                result = mockHttpResponse;
            }
        };

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext");

        //assert
        assertTrue(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_026: [If this object's queryCollectionResponse object has no continuation token, this function shall return false.]
    @Test
    public void hasNextReturnsFalseIfNoContinuationToken() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);
        Deencapsulation.setField(queryCollection, "wasCollectionReturnedAlready", true);

        new NonStrictExpectations()
        {
            {
                mockQueryCollectionResponse.getContinuationToken();
                result = null;
            }
        };

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext");

        //assert
        assertFalse(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_030: [If the provided query options are null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextWithOptionsThrowsForNullOptions() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);

        //act
        Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, null);
    }

    //Tests_SRS_QUERYCOLLECTION_34_027: [If this object's latest query retrieval has not been given to the user yet, this function shall return true.]
    @Test
    public void hasNextWithOptionsReturnsTrueIfRetrievalNotGivenYet() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);
        Deencapsulation.setField(queryCollection, "wasCollectionReturnedAlready", false);

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, new QueryOptions());

        //assert
        assertTrue(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_028: [If the provided query options has a continuation token, this function shall continue that query and return true if it still has results left.]
    @Test
    public void hasNextWithOptionsFetchesNextIfContinuationTokenPresentInOptions() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);
        Deencapsulation.setField(queryCollection, "wasCollectionReturnedAlready", true);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);
        Deencapsulation.setField(queryCollection, "iotHubConnectionString", mockConnectionString);
        Deencapsulation.setField(queryCollection, "url", mockUrl);
        Deencapsulation.setField(queryCollection, "httpMethod", mockHttpMethod);
        Deencapsulation.setField(queryCollection, "timeout", expectedTimeout);
        Deencapsulation.setField(queryCollection, "isSqlQuery", false);
        Deencapsulation.setField(queryCollection, "responseQueryType", QueryType.DEVICE_JOB);

        new NonStrictExpectations()
        {
            {
                new QueryOptions();
                result = mockQueryOptions;
                mockQueryOptions.getContinuationToken();
                result = expectedContinuationToken;
                DeviceOperations.request(mockConnectionString, mockUrl, mockHttpMethod, (byte[]) any, null, expectedTimeout);
                result = mockHttpResponse;
                Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {String.class, String.class}, anyString, anyString);
                result = mockQueryCollectionResponse;
            }
        };

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, new QueryOptions());

        //assert
        assertTrue(hasNext);
        boolean wasCollectionReturnedAlready = Deencapsulation.getField(queryCollection, "wasCollectionReturnedAlready");
        assertFalse(wasCollectionReturnedAlready);
        sentRequestVerifications();
    }

    //Tests_SRS_QUERYCOLLECTION_34_031: [If this object's queryCollectionResponse object has a continuation token, this function shall continue that query and return true if it still has results left.]
    @Test
    public void hasNextWithOptionsFetchesNextIfContinuationTokenPresentInResponseButNotOptions() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);
        Deencapsulation.setField(queryCollection, "wasCollectionReturnedAlready", true);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);
        Deencapsulation.setField(queryCollection, "iotHubConnectionString", mockConnectionString);
        Deencapsulation.setField(queryCollection, "url", mockUrl);
        Deencapsulation.setField(queryCollection, "httpMethod", mockHttpMethod);
        Deencapsulation.setField(queryCollection, "timeout", expectedTimeout);
        Deencapsulation.setField(queryCollection, "isSqlQuery", false);
        Deencapsulation.setField(queryCollection, "responseQueryType", QueryType.DEVICE_JOB);

        new NonStrictExpectations()
        {
            {
                new QueryOptions();
                result = mockQueryOptions;
                mockQueryCollectionResponse.getContinuationToken();
                result = expectedContinuationToken;
                DeviceOperations.request(mockConnectionString, mockUrl, mockHttpMethod, (byte[]) any, null, expectedTimeout);
                result = mockHttpResponse;
                Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {String.class, String.class}, anyString, anyString);
                result = mockQueryCollectionResponse;
            }
        };

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, new QueryOptions());

        //assert
        assertTrue(hasNext);
        boolean wasCollectionReturnedAlready = Deencapsulation.getField(queryCollection, "wasCollectionReturnedAlready");
        assertFalse(wasCollectionReturnedAlready);
        sentRequestVerifications();
    }

    //Tests_SRS_QUERYCOLLECTION_34_029: [In all other cases, this function shall return false.]
    @Test
    public void hasNextWithOptionsReturnsFalseIfNoContinuationTokenInOptionsOrQueryCollectionResponse() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.DEVICE_JOB);
        Deencapsulation.setField(queryCollection, "wasCollectionReturnedAlready", true);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        new NonStrictExpectations()
        {
            {
                mockQueryCollectionResponse.getContinuationToken();
                result = null;
            }
        };

        //act
        boolean hasNext = Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, new QueryOptions());

        //assert
        assertFalse(hasNext);
    }

    //Tests_SRS_QUERYCOLLECTION_34_032: [If this object has a next set to return, this function shall return it.]
    @Test
    public void nextReturnsNextSetIfItHasOne() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        //only mocking one method for QueryCollection, not the whole class
        new NonStrictExpectations(queryCollection)
        {
            {
                Deencapsulation.invoke(queryCollection, "hasNext");
                result = true;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next");

        //assert
        assertEquals(mockQueryCollectionResponse, actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_033: [If this object does not have a next set to return, this function shall return null.]
    @Test
    public void nextReturnsNullIfItDoesNotHaveNext() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        //only mocking one method for QueryCollection, not the whole class
        new NonStrictExpectations(queryCollection)
        {
            {
                Deencapsulation.invoke(queryCollection, "hasNext");
                result = false;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next");

        //assert
        assertNull(actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_034: [If this object has a next set to return using the provided query options, this function shall return it.]
    @Test
    public void nextWithOptionsReturnsNextSetIfItHasOne() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        //only mocking one method for QueryCollection, not the whole class
        new NonStrictExpectations(queryCollection)
        {
            {
                Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, mockQueryOptions);
                result = true;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next", new Class[] {QueryOptions.class}, mockQueryOptions);

        //assert
        assertEquals(mockQueryCollectionResponse, actualResponse);
    }

    //Tests_SRS_QUERYCOLLECTION_34_035: [If this object does not have a next set to return, this function shall return null.]
    @Test
    public void nextWithOptionsReturnsNullIfItDoesNotHaveNext() throws IOException, IotHubException
    {
        //arrange
        QueryCollection queryCollection = Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class}, "someQuery", 22, QueryType.JOB_RESPONSE);
        Deencapsulation.setField(queryCollection, "queryCollectionResponse", mockQueryCollectionResponse);

        //only mocking one method for QueryCollection, not the whole class
        new NonStrictExpectations(queryCollection)
        {
            {
                Deencapsulation.invoke(queryCollection, "hasNext", new Class[] {QueryOptions.class}, mockQueryOptions);
                result = false;
            }
        };

        //act
        QueryCollectionResponse actualResponse = Deencapsulation.invoke(queryCollection, "next", new Class[] {QueryOptions.class}, mockQueryOptions);

        //assert
        assertNull(actualResponse);
    }

    private void sentRequestVerifications() throws IOException, IotHubException
    {
        new Verifications()
        {
            {
                DeviceOperations.setHeaders(expectedHeaders);
                times = 1;

                DeviceOperations.request(mockConnectionString, mockUrl, mockHttpMethod, (byte[]) any, null, expectedTimeout);
                times = 1;
            }
        };
    }

}
