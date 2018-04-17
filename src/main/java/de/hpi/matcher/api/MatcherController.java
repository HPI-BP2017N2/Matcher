package de.hpi.matcher.api;

import de.hpi.matcher.services.Cache;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MatcherController {

    private final MatcherService matcherService;
    private final Cache cache;

    @RequestMapping(value = "/doSth/{shopID}", method = RequestMethod.GET, produces = "application/json")
    public void doSth(@PathVariable long shopID){
        getCache().warmup(shopID);
        getMatcherService().matchShop(shopID, (byte)0);

    }

}
