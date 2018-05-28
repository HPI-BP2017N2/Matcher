package de.hpi.matcher.services;

import de.hpi.machinelearning.PictureIdFinder;
import de.hpi.matcher.persistence.ParsedOffer;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Getter(AccessLevel.PRIVATE)
public class PictureIdFinderTest {

    @Getter(AccessLevel.PRIVATE) private static String EXAMPLE_URL = "http://example.com/123";
    @Getter(AccessLevel.PRIVATE) private static String EXAMPLE_IMAGE_URL1 = "http://example.com/124";
    @Getter(AccessLevel.PRIVATE) private static String EXAMPLE_IMAGE_URL2 = "http://example.com/125";
    @Getter(AccessLevel.PRIVATE) private static String[] EXPECTED_URL_PARTS = {"http:", "example", "com", "123"};
    @Getter(AccessLevel.PRIVATE) private static int[] EXPECTED_IMAGE_IDS = {3};

    private final List<ParsedOffer> offers = new LinkedList<>();

    @Before
    public void setup() {

        ParsedOffer offer1 = new ParsedOffer();
        ParsedOffer offer2 = new ParsedOffer();
        offer1.setImageUrl(getEXAMPLE_IMAGE_URL1());
        offer2.setImageUrl(getEXAMPLE_IMAGE_URL2());

        getOffers().add(offer1);
        getOffers().add(offer2);
    }

    @Test
    public void findPictureId() {
        List<Integer> imageIds = PictureIdFinder.findPictureId(getOffers());

        assertEquals(getEXPECTED_IMAGE_IDS().length, imageIds.size());

        for(int i = 0; i < getEXPECTED_IMAGE_IDS().length; i++) {
            assertEquals(getEXPECTED_IMAGE_IDS()[i], (int)imageIds.get(i));
        }
    }


}