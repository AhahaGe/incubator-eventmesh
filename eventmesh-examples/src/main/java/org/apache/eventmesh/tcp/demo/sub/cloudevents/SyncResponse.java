/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.tcp.demo.sub.cloudevents;

import org.apache.eventmesh.client.tcp.EventMeshTCPClient;
import org.apache.eventmesh.client.tcp.EventMeshTCPClientFactory;
import org.apache.eventmesh.client.tcp.common.MessageUtils;
import org.apache.eventmesh.client.tcp.common.ReceiveMsgHook;
import org.apache.eventmesh.client.tcp.conf.EventMeshTCPClientConfig;
import org.apache.eventmesh.common.protocol.SubscriptionMode;
import org.apache.eventmesh.common.protocol.SubscriptionType;
import org.apache.eventmesh.common.protocol.tcp.Command;
import org.apache.eventmesh.common.protocol.tcp.EventMeshMessage;
import org.apache.eventmesh.common.protocol.tcp.Package;
import org.apache.eventmesh.common.protocol.tcp.UserAgent;
import org.apache.eventmesh.tcp.common.EventMeshTestUtils;
import org.apache.eventmesh.util.Utils;

import io.cloudevents.CloudEvent;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

@Slf4j
public class SyncResponse implements ReceiveMsgHook<CloudEvent> {

    public static SyncResponse handler = new SyncResponse();

    private static EventMeshTCPClient<CloudEvent> client;

    public static void main(String[] agrs) throws Exception {
        Properties properties = Utils.readPropertiesFile("application.properties");
        final String eventMeshIp = properties.getProperty("eventmesh.ip");
        final int eventMeshTcpPort = Integer.parseInt(properties.getProperty("eventmesh.tcp.port"));
        UserAgent userAgent = EventMeshTestUtils.generateClient2();
        EventMeshTCPClientConfig eventMeshTcpClientConfig = EventMeshTCPClientConfig.builder()
            .host(eventMeshIp)
            .port(eventMeshTcpPort)
            .userAgent(userAgent)
            .build();
        try {
            client = EventMeshTCPClientFactory
                .createEventMeshTCPClient(eventMeshTcpClientConfig, CloudEvent.class);
            client.init();

            client.subscribe("TEST-TOPIC-TCP-SYNC", SubscriptionMode.CLUSTERING, SubscriptionType.SYNC);
            // Synchronize RR messages
            client.registerSubBusiHandler(handler);

            client.listen();

        } catch (Exception e) {
            log.warn("SyncResponse failed", e);
        }
    }

    @Override
    public Optional<CloudEvent> handle(CloudEvent event) {
        String content = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
        log.info("receive sync rr msg================{}|{}", event, content);
        return Optional.of(event);
    }

}
