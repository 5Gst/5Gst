/*
 * Service API
 * Speedtest iperf service
 *
 * OpenAPI spec version: 0.1.0
 * Contact: dev@5gst.ru
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package ru.fivegst.speedtest.client.service.auth;

import ru.fivegst.speedtest.client.service.Pair;

import java.util.Map;
import java.util.List;

public interface Authentication {
    /**
     * Apply authentication settings to header and query params.
     *
     * @param queryParams List of query parameters
     * @param headerParams Map of header parameters
     */
    void applyToParams(List<Pair> queryParams, Map<String, String> headerParams);
}