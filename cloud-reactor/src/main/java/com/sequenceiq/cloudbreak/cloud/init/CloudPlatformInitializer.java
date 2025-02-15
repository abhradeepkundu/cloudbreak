package com.sequenceiq.cloudbreak.cloud.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.event.CloudPlatformRequest;
import com.sequenceiq.cloudbreak.cloud.handler.CloudPlatformEventHandler;
import com.sequenceiq.cloudbreak.eventbus.EventBus;

@Component
public class CloudPlatformInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CloudPlatformInitializer.class);

    @Resource
    private List<CloudPlatformEventHandler<?>> handlers;

    @Inject
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        validateSelectors();
        LOGGER.debug("Registering CloudPlatformEventHandlers");
        for (CloudPlatformEventHandler<?> handler : handlers) {
            String selector = CloudPlatformRequest.selector(handler.type());
            LOGGER.debug("Registering handler [{}] for selector [{}]", handler.getClass(), selector);
            eventBus.on(selector, handler);
        }
    }

    private void validateSelectors() {
        LOGGER.debug("There are {} handlers suitable for registering", handlers.size());
        Map<Class<?>, CloudPlatformEventHandler<?>> handlerMap = new HashMap<>();
        for (CloudPlatformEventHandler<?> handler : handlers) {
            CloudPlatformEventHandler<?> entry = handlerMap.put(handler.type(), handler);
            if (null != entry) {
                LOGGER.error("Duplicate handlers! actual: {}, existing: {}", handler, entry);
                throw new IllegalStateException("Duplicate handlers! first: " + handler + " second: " + entry);
            }
        }
    }
}
