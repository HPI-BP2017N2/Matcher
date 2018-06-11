package de.hpi.matcher.queue;

import de.hpi.matcher.dto.FinishedShop;
import de.hpi.matcher.properties.MatcherProperties;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter(AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class RabbitReceiver {

    private final MatcherService matcherService;
    private final MatcherProperties properties;

    /**
     * This message starts matching one shop when receiving shop from queue.
     *
     * @param request Message from crawler containing the shop ID
     */
    @RabbitListener(queues = "#{@matcherQueue}")
    public void onMessage(FinishedShop request) throws Exception {
        log.info("Got request to match shop {}", request.getShopId());
        if (getProperties().isCollectTrainingData()) {
            log.info("Do nothing since collectTrainingData is set.");
        } else {
            getMatcherService().matchShop(request.getShopId(), (byte) 0);
        }
    }
}