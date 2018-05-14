package de.hpi.matcher.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class ShopOffer {

    private String offerKey;
    private boolean isMatched;
    private Long shopId;
    private String brandName;
    private List<String> categoryPaths;
    private String productSearchtext;
    private String ean;
    private String han;
    private String sku;
    private Map<String, String> titles;
    private Map<String, Double> prices;
    private Map<String, String> descriptions;
    private Map<String, String> urls;
    private List<String> hans;
    private List<String> eans;
    private Map<String, String> smallPicture;
    private Map<String, List<String>> imageUrls;
    private String productKey;
    private String mappedCatalogCategory;
    private byte phase;
    private String imageId;
}
