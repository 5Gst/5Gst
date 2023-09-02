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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import ru.scoltech.openran.speedtest.client.balancer.model.IperfProbeResults;

/**
 * IperfMeasurementResult
 */

public class IperfMeasurementResult {
  @SerializedName("start_timestamp")
  private OffsetDateTime startTimestamp = null;

  @SerializedName("probes")
  private List<IperfProbeResults> probes = new ArrayList<>();

  public IperfMeasurementResult startTimestamp(OffsetDateTime startTimestamp) {
    this.startTimestamp = startTimestamp;
    return this;
  }

   /**
   * Get startTimestamp
   * @return startTimestamp
  **/
  @ApiModelProperty(required = true, value = "")
  public OffsetDateTime getStartTimestamp() {
    return startTimestamp;
  }

  public void setStartTimestamp(OffsetDateTime startTimestamp) {
    this.startTimestamp = startTimestamp;
  }

  public IperfMeasurementResult probes(List<IperfProbeResults> probes) {
    this.probes = probes;
    return this;
  }

  public IperfMeasurementResult addProbesItem(IperfProbeResults probesItem) {
    this.probes.add(probesItem);
    return this;
  }

   /**
   * Get probes
   * @return probes
  **/
  @ApiModelProperty(required = true, value = "")
  public List<IperfProbeResults> getProbes() {
    return probes;
  }

  public void setProbes(List<IperfProbeResults> probes) {
    this.probes = probes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IperfMeasurementResult iperfMeasurementResult = (IperfMeasurementResult) o;
    return Objects.equals(this.startTimestamp, iperfMeasurementResult.startTimestamp) &&
        Objects.equals(this.probes, iperfMeasurementResult.probes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startTimestamp, probes);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IperfMeasurementResult {\n");
    
    sb.append("    startTimestamp: ").append(toIndentedString(startTimestamp)).append("\n");
    sb.append("    probes: ").append(toIndentedString(probes)).append("\n");
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
