package de.hpi.matcher.api;

import de.hpi.matcher.services.MatcherService;
import de.hpi.matcher.services.ModelGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Slf4j
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MatcherController {

    private final MatcherService matcherService;

    private final ModelGenerator modelGenerator;

    @RequestMapping(value = "/doSth", method = RequestMethod.GET, produces = "application/json")
    public void doSth() throws IOException {
        //getMatcherService().matchShop(shopID, (byte)0);
        getMatcherService().classify();
    }

}
