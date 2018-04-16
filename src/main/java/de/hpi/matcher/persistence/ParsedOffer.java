package de.hpi.matcher.persistence;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedOffer {

    private long crawlingTimestamp;
    private String url;
    private String ean;
    private String sku;
    private String han;
    private String title;
    private double price;
    private String description;
    private String brandName;
    private String category;
}
