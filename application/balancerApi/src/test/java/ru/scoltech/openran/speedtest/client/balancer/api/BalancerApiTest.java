/*
 * Balancer API
 * Speedtest load balancer
 *
 * OpenAPI spec version: 0.1.0
 * Contact: dev@5gst.ru
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ru.scoltech.openran.speedtest.client.balancer.api;

import ru.scoltech.openran.speedtest.client.balancer.model.FiveGstToken;
import ru.scoltech.openran.speedtest.client.balancer.model.IperfMeasurementResult;
import ru.scoltech.openran.speedtest.client.balancer.model.ServerAddressRequest;
import ru.scoltech.openran.speedtest.client.balancer.model.ServerAddressResponse;
import org.junit.Test;
import org.junit.Ignore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for BalancerApi
 */
@Ignore
public class BalancerApiTest {

    private final BalancerApi api = new BalancerApi();

    
    /**
     * 
     *
     * Acquires service for further iperf tests
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void acquireServiceTest() throws Exception {
        ServerAddressResponse response = api.acquireService();

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Save measurement results
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void createTest() throws Exception {
        IperfMeasurementResult data = null;
        api.create(data);

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Log in to 5Gst service
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void loginTest() throws Exception {
        FiveGstToken response = api.login();

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Log out from 5Gst service
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void logoutTest() throws Exception {
        api.logout();

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Check that server is up
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void pingTest() throws Exception {
        api.ping();

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Return measurement results
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void readTest() throws Exception {
        Integer measurementId = null;
        IperfMeasurementResult response = api.read(measurementId);

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Register caller as service
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void registerServiceTest() throws Exception {
        ServerAddressRequest data = null;
        ServerAddressRequest response = api.registerService(data);

        // TODO: test validations
    }
    
    /**
     * 
     *
     * Unregister caller as service
     *
     * @throws Exception
     *          if the Api call fails
     */
    @Test
    public void unregisterServiceTest() throws Exception {
        ServerAddressRequest data = null;
        ServerAddressRequest response = api.unregisterService(data);

        // TODO: test validations
    }
    
}
