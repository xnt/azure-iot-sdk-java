/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenSoftwareAuthenticationProvider;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests for IotHubSasTokenSoftwareAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubSasTokenSoftwareAuthenticationProviderTest
{
    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";
    private static String expectedDeviceKey = "deviceKey";
    private static String expectedSasToken = "sasToken";
    private static long expectedExpiryTime = 3601;

    @Mocked IotHubSasToken mockSasToken;
    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked SSLContext mockSSLContext;

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_002: [This constructor shall save the provided hostname, device id, deviceKey, and sharedAccessToken.]
    @Test
    public void constructorSavesArguments()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;

                mockSasToken.toString();
                result = expectedSasToken;
            }
        };

        //act
        IotHubSasTokenSoftwareAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //assert
        String acutalDeviceId = Deencapsulation.getField(sasAuth, "deviceId");
        String acutalHostName = Deencapsulation.getField(sasAuth, "hostname");
        String acutalDeviceKey = Deencapsulation.getField(sasAuth, "deviceKey");
        IotHubSasToken acutalSasToken = Deencapsulation.getField(sasAuth, "sasToken");

        assertEquals(expectedDeviceId, acutalDeviceId);
        assertEquals(expectedHostname, acutalHostName);
        assertEquals(expectedDeviceKey, acutalDeviceKey);
        assertEquals(expectedSasToken, acutalSasToken.toString());
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_004: [If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.]
    @Test
    public void getRenewedSasTokenAutoRenews(@Mocked final System mockSystem) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                System.currentTimeMillis();
                result = 0;
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
                System.currentTimeMillis();
                result = 0;
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, expectedHostname, expectedDeviceId, expectedDeviceKey, null, expectedExpiryTime);
                result = mockSasToken;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        String actualSasToken = Deencapsulation.invoke(sasAuth, "getRenewedSasToken");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, expectedHostname, expectedDeviceId, expectedDeviceKey, null, expectedExpiryTime);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_005: [This function shall return the saved sas token.]
    @Test
    public void getSasTokenReturnsSavedValue() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new Expectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;
                mockSasToken.toString();
                result = "some token";
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        String actualSasToken = Deencapsulation.invoke(sasAuth, "getRenewedSasToken");

        //assert
        assertEquals(mockSasToken.toString(), actualSasToken);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsTrueWhenTokenHasExpiredAndNoDeviceKeyIsPresent() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, null, expectedSasToken);

        //act
        boolean needsToRenew = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertTrue(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsFalseDeviceKeyPresent() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new StrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, anyString, anyString, anyString, anyString, anyLong);
                result = mockSasToken;
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        boolean needsToRenew = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertFalse(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsFalseWhenTokenHasNotExpired() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        boolean needsToRenew = Deencapsulation.invoke(sasAuth, "isRenewalNecessary");

        //assert
        assertFalse(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_059: [This function shall save the provided pathToCertificate.]
    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setPathToCertificateWorks() throws IOException
    {
        //arrange
        IotHubSasTokenAuthenticationProvider auth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        String pathToCert = "somePath";

        //act
        auth.setPathToIotHubTrustedCert(pathToCert);

        //assert
        String actualPathToCert = Deencapsulation.getField(auth, "pathToIotHubTrustedCert");
        assertEquals(pathToCert, actualPathToCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_064: [This function shall save the provided userCertificateString.]
    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setCertificateWorks() throws IOException
    {
        //arrange
        IotHubSasTokenAuthenticationProvider auth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        String cert = "somePath";

        //act
        auth.setIotHubTrustedCert(cert);

        //assert
        String actualCert = Deencapsulation.getField(auth, "iotHubTrustedCert");
        assertEquals(cert, actualCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCert() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        final String expectedCert = "someTrustedCert";
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        sasAuth.setIotHubTrustedCert(expectedCert);

        //act
        Deencapsulation.invoke(sasAuth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, expectedCert, false);
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCertPath() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        final String expectedCertPath = "someTrustedCertPath";
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        sasAuth.setPathToIotHubTrustedCert(expectedCertPath);

        //act
        Deencapsulation.invoke(sasAuth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, expectedCertPath, true);
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.]
    @Test
    public void generateSSLContextGeneratesDefaultIotHubSSLContext() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        Deencapsulation.invoke(sasAuth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_006: [If a CertificateException, NoSuchAlgorithmException, KeyManagementException, or KeyStoreException is thrown during this function, this function shall throw an IOException.]
    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_007: [If this object's ssl context has not been generated yet or if it needs to be re-generated, this function shall regenerate the ssl context.]
    @Test (expected = IOException.class)
    public void getSSLContextWrapsExceptions() throws IOException
    {
        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = new CertificateException();
            }
        };

        //act
        sasAuth.getSSLContext();
    }

    //Tests_SRS_IOTHUBSASTOKENSOFTWAREAUTHENTICATION_34_008: [This function shall return the generated IotHubSSLContext.]
    @Test
    public void getSSLContextSuccess() throws IOException
    {
        //arrange
        IotHubSasTokenAuthenticationProvider sasAuth = new IotHubSasTokenSoftwareAuthenticationProvider(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                result = mockIotHubSSLContext;

                Deencapsulation.invoke(mockIotHubSSLContext, "getSSLContext");
                result = mockSSLContext;
            }
        };

        //act
        SSLContext actualSSLContext = sasAuth.getSSLContext();

        //assert
        assertEquals(mockSSLContext, actualSSLContext);
    }
}
