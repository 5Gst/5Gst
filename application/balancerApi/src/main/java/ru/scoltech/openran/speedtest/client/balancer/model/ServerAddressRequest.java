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


package ru.scoltech.openran.speedtest.client.balancer.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.IOException;

/**
 * ServerAddressRequest
 */

public class ServerAddressRequest {
  @SerializedName("ip")
  private String ip = null;

  @SerializedName("port")
  private Integer port = null;

  @SerializedName("port_iperf")
  private Integer portIperf = null;

  public ServerAddressRequest ip(String ip) {
    this.ip = ip;
    return this;
  }

   /**
   * Get ip
   * @return ip
  **/
  @ApiModelProperty(required = true, value = "")
  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public ServerAddressRequest port(Integer port) {
    this.port = port;
    return this;
  }

   /**
   * Get port
   * minimum: 0
   * maximum: 65535
   * @return port
  **/
  @ApiModelProperty(required = true, value = "")
  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public ServerAddressRequest portIperf(Integer portIperf) {
    this.portIperf = portIperf;
    return this;
  }

   /**
   * Get portIperf
   * minimum: 0
   * maximum: 65535
   * @return portIperf
  **/
  @ApiModelProperty(required = true, value = "")
  public Integer getPortIperf() {
    return portIperf;
  }

  public void setPortIperf(Integer portIperf) {
    this.portIperf = portIperf;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServerAddressRequest serverAddressRequest = (ServerAddressRequest) o;
    return Objects.equals(this.ip, serverAddressRequest.ip) &&
        Objects.equals(this.port, serverAddressRequest.port) &&
        Objects.equals(this.portIperf, serverAddressRequest.portIperf);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ip, port, portIperf);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServerAddressRequest {\n");
    
    sb.append("    ip: ").append(toIndentedString(ip)).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
    sb.append("    portIperf: ").append(toIndentedString(portIperf)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

