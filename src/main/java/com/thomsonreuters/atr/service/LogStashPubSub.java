package com.thomsonreuters.atr.service;
import com.thomsonreuters.atr.framework.client.MessageClient;
import com.thomsonreuters.atr.framework.events.RequestEvent;
import com.thomsonreuters.atr.framework.exception.FrameworkException;
import com.thomsonreuters.atr.messaging.config.IFixDomainConfiguration;
import com.thomsonreuters.atr.messaging.messages.*;
import lombok.extern.slf4j.Slf4j;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class LogStashPubSub {

    // Connection to Solace
    private MessageClient messageClient;
    private ElasticSearchService service;
    public void init(IFixDomainConfiguration configuration, ElasticSearchService service) throws FrameworkException {
        log.info("Initating solace connection with Heimdall");
        this.service = service;
        // Create our messageClient
        messageClient = new MessageClient(configuration);
        log.trace("newMessageClient was created");
        // Expect requests on request topic
        String requestTopic = messageClient.getTopicApplication() + "Admin/*/Requests";
        log.trace("Config Request topic: {}", requestTopic);

        messageClient.addSubscriber(this);
        messageClient.openTopic(requestTopic);

        try {
            messageClient.startStatusFile();
            messageClient.start();
        } catch (FrameworkException fe) {
            log.error("Error starting Elastic Service ", fe);
        }
    }

    /** Shutdown */
    public void stop() {
        log.error("Elastic  is going to Valhalla!");
        messageClient.stop();
        messageClient.stopStatusFile();
    }


    /**
     * The only request handler
     *
     * @param requestEvent request message
     **/

    @SuppressWarnings("unused")
    @Handler(delivery = Invoke.Asynchronously)
    public void processConfigMessage(RequestEvent requestEvent) {
        try {
            log.info(
                    "Received request: {} on {} from requestID {}",
                    requestEvent.getMessage(),
                    requestEvent.getMessage().getDestination(),
                    requestEvent.getRequestId()
            );
            LogstashMappingMessage mappedMessage = new LogstashMappingMessage();
            mappedMessage.setMsgtypeFields(service.msgTypeToTagsMapping());
            log.trace("Sending message back via solace ");
            messageClient.submitResponse(requestEvent.getRequestId(), mappedMessage);
        } catch (FrameworkException fe) {
            log.error("Error processing request: ", fe);
        }
    }
}

