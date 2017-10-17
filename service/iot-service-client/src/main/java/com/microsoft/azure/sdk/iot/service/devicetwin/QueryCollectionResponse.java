/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.QueryResponseParser;

import java.io.IOException;
import java.util.Iterator;

public class QueryCollectionResponse<Object>
{
    private Iterator<?> responseElementsIterator;
    private String continuationToken;

    QueryCollectionResponse(String jsonString, String continuationToken) throws IOException
    {
        if (jsonString == null || jsonString.length() == 0)
        {
            //Codes_SRS_QUERY_RESPONSE_25_002: [If the jsonString is null or empty, the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("response cannot be null or empty");
        }

        //Codes_SRS_QUERY_RESPONSE_25_001: [The constructor shall parse the json response using QueryResponseParser and set the iterator.]
        QueryResponseParser responseParser = new QueryResponseParser(jsonString);
        this.responseElementsIterator = responseParser.getJsonItems().iterator();

        this.continuationToken = continuationToken;
    }

    QueryCollectionResponse(Iterator<?> responseElementsIterator, String continuationToken) throws IOException
    {
        this.responseElementsIterator = responseElementsIterator;
        this.continuationToken = continuationToken;
    }

    public String getContinuationToken()
    {
        return this.continuationToken;
    }

    public Iterator<?> getCollection()
    {
        return this.responseElementsIterator;
    }
}
