package de.hpi.matcher.persistence.repo;

import de.hpi.matcher.persistence.ParsedOffer;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Getter(AccessLevel.PRIVATE)
@Repository
public class ParsedOfferRepositoryImpl implements ParsedOfferRepository {

    @Autowired
    @Qualifier("parsedOfferTemplate")
    private MongoTemplate mongoTemplate;

    @Override
    public boolean eanFound(long shopId) {
        return getMongoTemplate().count(query(where("ean").regex(".+")), ParsedOffer.class, Long.toString(shopId)) > 0;
    }

    @Override
    public boolean hanFound(long shopId) {
        return getMongoTemplate().count(query(where("han").regex(".+")), ParsedOffer.class, Long.toString(shopId)) > 0;
    }

    @Override
    public ParsedOffer getByEan(long shopId, String ean) {
        return getByIdentifier(shopId, "ean", ean);
    }

    @Override
    public ParsedOffer getByEanWithVariation(long shopId, String ean) {
        return(getMongoTemplate().findOne(query(where("ean").regex("^[\\D]*" + ean + "[\\D]*$")), ParsedOffer.class, Long.toString(shopId)));
    }

    @Override
    public ParsedOffer getByHan(long shopId, String han) {
        return getByIdentifier(shopId, "han", han);
    }

    @Override
    public void deleteParsedOffer(long shopId, String url) {
        getMongoTemplate().remove(query(where("_id").is(url)), Long.toString(shopId));
    }

    private ParsedOffer getByIdentifier(long shopId, String identifier, String value) {
        return (value != null)?
                getMongoTemplate().findOne(query(where(identifier).is(value)), ParsedOffer.class, Long.toString(shopId))
                : null;
    }
}
