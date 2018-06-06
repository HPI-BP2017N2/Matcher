package de.hpi.matcher.api;

import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Controller {

    private final MatcherService service;

    @RequestMapping(value = "/match/{shopId}", method = RequestMethod.GET)
    public void generateCategoryClassifier(@PathVariable long shopId) throws Exception {
        getService().matchShop(shopId, (byte)0);

    }
}
