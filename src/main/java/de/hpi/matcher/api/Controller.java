package de.hpi.matcher.api;

import de.hpi.matcher.dto.ErrorResponse;
import de.hpi.matcher.dto.SuccessResponse;
import de.hpi.matcher.properties.MatcherProperties;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Controller {

    private final MatcherService service;
    private final MatcherProperties properties;

    @RequestMapping(value = "/match/{shopId}", method = RequestMethod.GET)
    public ResponseEntity<Object> generateCategoryClassifier(@PathVariable long shopId) throws Exception {
        if(getProperties().isCollectTrainingData()) {
            getService().matchShop(shopId, (byte) 0);
            return new SuccessResponse().send();
        }

        return new ErrorResponse().send();

    }
}
