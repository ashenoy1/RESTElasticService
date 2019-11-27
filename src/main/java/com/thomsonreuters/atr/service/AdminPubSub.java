package com.thomsonreuters.atr.service;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thomsonreuters.atr.framework.client.MessageClient;
import com.thomsonreuters.atr.framework.events.RequestEvent;
import com.thomsonreuters.atr.framework.exception.FrameworkException;
import com.thomsonreuters.atr.messaging.config.IFixDomainConfiguration;
import com.thomsonreuters.atr.messaging.messages.GroupIdsList;
import com.thomsonreuters.atr.messaging.messages.JSONMessage;
import lombok.extern.slf4j.Slf4j;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AdminPubSub {
    private MessageClient messageClient;
    private int responseTimeout = 9000;
    private LoadingCache<String, List<Integer>> groupIdCache;



    public AdminPubSub(IFixDomainConfiguration configuration) throws FrameworkException{
        log.info("Opening connection to AdminService via Solace");
        messageClient = new MessageClient(configuration);
        String requestTopic = messageClient.getTopicApplication() + "Elastic/*/Requests";
        log.trace("Config Request topic: {}", requestTopic);
        messageClient.addSubscriber(this);
        messageClient.openTopic(requestTopic);
        try {
            messageClient.startStatusFile();
            messageClient.start();
        } catch (FrameworkException fe) {
            log.error("Error starting Admin Service ", fe);
        }
        log.trace("newMessageClient was created for AdminPubSub");

        buildCache();
    }


    public List<Integer> synchronousRequestGroupId(String uuId) {
        String configRequestTopic =  messageClient.getTopicPrefix() + "AdminService/Admin/GROUPID/Requests";
        List<Integer> groupIdlist = new ArrayList<>();
        GroupIdsList request = new GroupIdsList();
        request.setUuId(uuId);

        try {
            log.debug("Submitting config request to {} for {}. Timeout: {}", configRequestTopic, request, responseTimeout);
            JSONMessage responseMessage = (JSONMessage)messageClient
                    .submitRequest(configRequestTopic, request, responseTimeout)
                    .orElseThrow(() -> new FrameworkException("Failed to retrieve GroupId from Admin Service"));

            if (responseMessage != null) {
                if (responseMessage instanceof GroupIdsList ) {
                    log.trace("Received message from admin service");
                    GroupIdsList groupIds = (GroupIdsList) responseMessage;
                    groupIdlist = groupIds.getGroupIds();
                }
            } else {
                log.warn("responseMessage from admin service is null");
            }
        } catch (FrameworkException fe) {
            log.warn("Error requesting groupIDs from admin service, FrameworkException ", fe);
        }
        return groupIdlist;
    }

    @SuppressWarnings("unused")
    @Handler(delivery = Invoke.Asynchronously)
    public void updateGroupIdCache(RequestEvent requestEvent) {
        try {
            log.info(
                    "Received request: {} on {} from requestID {}",
                    requestEvent.getMessage(),
                    requestEvent.getMessage().getDestination(),
                    requestEvent.getRequestId()
            );
            //GroupIdsList list = new GroupIdsList();
            if (requestEvent != null) {
                if (requestEvent.getMessage() instanceof GroupIdsList) {
                    GroupIdsList list = (GroupIdsList) requestEvent.getMessage();
                    evictCacheValue(list.getUuId());
                    messageClient.submitResponse(requestEvent.getRequestId(), list);
                }
            } else {
                log.error("Request Event for update Cache received from Solace is null");
            }

        } catch (FrameworkException fe) {
            log.error("Error processing request: ", fe);
        }
    }

    public List<Integer> getGroupIdList(String uuId){
        log.trace("Fetching Groupid for the uuId: {}", uuId);
        List<Integer> list = null;
        try {
            list = groupIdCache.get(uuId);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return list;
    }


    //Update upon solace request
    public void evictCacheValue(String uuId){
        log.info("Request to evict uuId {} from Admin service ",uuId);
        groupIdCache.invalidate(uuId);
    }


    private void buildCache() {
        groupIdCache =
                CacheBuilder.newBuilder()
                        .maximumSize(10000)                             // maximum 10000 records can be cached
                        .expireAfterAccess(30, TimeUnit.MINUTES) // cache will expire after 30 minutes of access
                        .build(new CacheLoader<String, List<Integer>>() {  // build the cacheloader

                            @Override
                            public List<Integer> load(String uuId) throws Exception {
                                log.trace("Cache miss for the uuId {}", uuId);
                                return synchronousRequestGroupId(uuId);
                            }
                        });
        log.trace("Built cache");
    }
}
