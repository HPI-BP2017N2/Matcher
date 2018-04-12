package de.hpi.matcher.queue;

import de.hpi.matcher.dto.FinishedShop;
import de.hpi.matcher.services.MatcherService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter(AccessLevel.PRIVATE)
public class RabbitReceiver {

    @Autowired
    private MatcherService matcherService;

    @RabbitListener(queues = "#{@matcherQueue}")
    public void onMessage(FinishedShop request) {
        log.debug("Got an {}", request);
        try {
            getMatcherService().matchShop(request.getShopId());
        } catch (Exception e) {
            log.warn("Exception in amqp listener method", e);
        }
    }
}