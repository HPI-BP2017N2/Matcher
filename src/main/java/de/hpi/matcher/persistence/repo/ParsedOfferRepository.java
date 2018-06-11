package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Getter(AccessLevel.PRIVATE)
@Repository
public class ParsedOfferRepository {

    @Autowired
    @Qualifier("parsedOfferTemplate")
    private MongoTemplate mongoTemplate;

    public boolean eanFound(long shopId) {
        return getMongoTemplate().count(query(where("ean").regex(".+")), ParsedOffer.class, Long.toString(shopId)) > 0;
    }

    public boolean hanFound(long shopId) {
        return getMongoTemplate().count(query(where("han").regex(".+")), ParsedOffer.class, Long.toString(shopId)) > 0;
    }

    public ParsedOffer getByEan(long shopId, String ean) {
        return getByIdentifier(shopId, "ean", ean);
    }

    public ParsedOffer getByEanWithVariation(long shopId, String ean) {
        return(getMongoTemplate().findOne(query(where("ean").regex("^[\\D]*" + ean + "[\\D]*$")), ParsedOffer.class, Long.toString(shopId)));
    }

    public ParsedOffer getByHan(long shopId, String han) {
        return getByIdentifier(shopId, "han", han);
    }

    public void deleteParsedOffer(long shopId, String url) {
        getMongoTemplate().remove(query(where("_id").is(url)), Long.toString(shopId));
    }

    public List<ParsedOffer> getAllOffers(long shopId) {
        return getMongoTemplate().findAll(ParsedOffer.class, Long.toString(shopId));
    }

    public List<ParsedOffer> getOffersWithImageUrl(long shopId, int count) {
        return getMongoTemplate().find(query(where("imageUrl").ne("")).limit(count), ParsedOffer.class, Long.toString(shopId));
    }

    public List<ParsedOffer> getOffersWithEan(long shopId) {
        return getMongoTemplate().find(query(where("ean").ne("").and("ean").ne(null)), ParsedOffer.class, Long.toString(shopId));
    }

    private ParsedOffer getByIdentifier(long shopId, String identifier, String value) {
        return (value != null) ?
                getMongoTemplate().findOne(query(where(identifier).is(value)), ParsedOffer.class, Long.toString(shopId))
                : null;
    }

    public boolean collectionIsEmpty(long shopId) {
        return  getMongoTemplate().count(query(where("_id").exists(true)), ParsedOffer.class, Long.toString(shopId)) == 0;
    }
}
