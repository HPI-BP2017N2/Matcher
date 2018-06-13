package de.hpi.matcher.api;

import de.hpi.matcher.dto.ErrorResponse;
import de.hpi.matcher.dto.SuccessResponse;
import de.hpi.matcher.properties.MatcherProperties;
import de.hpi.matcher.services.MatcherService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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


    @ApiOperation(value = "Match parsed offers of shop with unique identifiers", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully matched shop."),
            @ApiResponse(code = 403, message = "REST interface was disabled for matching from queue.")})
    @RequestMapping(value = "/matchUnique/{shopId}", method = RequestMethod.POST)
    public ResponseEntity<Object> matchUnique(@PathVariable long shopId) throws Exception {
        if(getProperties().isCollectTrainingData()) {
            getService().matchShop(shopId, (byte) 0);
            return new SuccessResponse().send();
        }

        return new ErrorResponse().send();

    }

    @ApiOperation(value = "Match parsed offers of shop with Machine Learning", response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully matched shop."),
            @ApiResponse(code = 403, message = "REST interface was disabled for matching from queue.")})
    @RequestMapping(value = "/matchNonUnique/{shopId}", method = RequestMethod.POST)
    public ResponseEntity<Object> matchNonUnique(@PathVariable long shopId) throws Exception {
        if(getProperties().isCollectTrainingData()) {
            getService().matchShop(shopId, (byte) 1);
            return new SuccessResponse().send();
        }

        return new ErrorResponse().send();

    }
}
