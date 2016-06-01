
package sdkjavatest;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: VersionInput</p>
 * <pre>
 * version is input a number of seconds to run for
 * </pre>
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "ws",
    "num_seconds"
})
public class VersionInput {

    @JsonProperty("ws")
    private String ws;
    @JsonProperty("num_seconds")
    private Long numSeconds;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ws")
    public String getWs() {
        return ws;
    }

    @JsonProperty("ws")
    public void setWs(String ws) {
        this.ws = ws;
    }

    public VersionInput withWs(String ws) {
        this.ws = ws;
        return this;
    }

    @JsonProperty("num_seconds")
    public Long getNumSeconds() {
        return numSeconds;
    }

    @JsonProperty("num_seconds")
    public void setNumSeconds(Long numSeconds) {
        this.numSeconds = numSeconds;
    }

    public VersionInput withNumSeconds(Long numSeconds) {
        this.numSeconds = numSeconds;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ((((((("VersionInput"+" [ws=")+ ws)+", numSeconds=")+ numSeconds)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
