package de.hpi.machinelearning;
import de.hpi.matcher.persistence.ParsedOffer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PictureIdFinder {

    public static List<Integer> findPictureId(List<ParsedOffer> parsedOffers) {
        Set<String> partSet = new HashSet<>();
        int length = 0;
        Set<Integer> nonUniqueIndice= new HashSet<>();
        for (ParsedOffer offer : parsedOffers) {
            String imageUrl = offer.getImageUrl();
            if (imageUrl == null) {
                continue;
            }
            String[] urlParts = splitUrl(imageUrl);
            length = urlParts.length;
            for (int index = 0; index < length; index++ ) {
                if(partSet.contains(urlParts[index])){
                    nonUniqueIndice.add(index);
                }
                partSet.add(urlParts[index]);
            }

        }
        List<Integer> uniqueIndex = new ArrayList<>();
        for (int index = 0; index < length; index++ ) {
            if (nonUniqueIndice.contains(index)) {
                continue;
            }
            uniqueIndex.add(index);
        }
        return uniqueIndex;
    }

    private static String[] splitUrl(String url) {
        url = url.replace("//", "/");
        return url.split("[/.]");
    }

    public static String getImageId(String url, List<Integer> indices) {
        String[] urlParts = PictureIdFinder.splitUrl(url);
        String uniqueParts = "";
        for (int position : indices) {
            uniqueParts = uniqueParts.concat(urlParts[position]);
        }

        return uniqueParts.equals("")? null : uniqueParts;
    }
}
