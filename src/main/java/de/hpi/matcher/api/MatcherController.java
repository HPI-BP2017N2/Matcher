package de.hpi.matcher.api;

import de.hpi.matcher.dto.ScoredModel;
import de.hpi.matcher.persistence.repo.ModelGenerator;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class MatcherController {

    private final MatcherService matcherService;

    private final ModelGenerator modelGenerator;

    @RequestMapping(value = "/doSth/{shopID}", method = RequestMethod.GET, produces = "application/json")
    public void doSth(@PathVariable long shopID){
        //getMatcherService().matchShop(shopID, (byte)0);

        ScoredModel model = getModelGenerator().getModel();
        System.out.println(model.getModelType());
        System.out.println(model.getModel());


    }

}
