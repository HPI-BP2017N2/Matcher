package de.hpi.matcher.api;

import de.hpi.matcher.persistence.repo.ModelRepository;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.springframework.web.bind.annotation.PathVariable;
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

    private final ModelRepository modelRepository;

    @RequestMapping(value = "/doSth/{shopID}", method = RequestMethod.GET, produces = "application/json")
    public void doSth(@PathVariable long shopID) throws IOException {
        //getMatcherService().matchShop(shopID, (byte)0);

        ParagraphVectors model = getModelRepository().getCategoryClassifier().getNeuralNetwork();
        System.out.println(model);
    }

}
