/*
 * Copyright 2017 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.springframework.cloud.stream.app.grpc.processor;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author David Turanski
 **/
@Configuration
@ConditionalOnProperty(name = "grpc.host")
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcChannelConfiguration {

	@Bean
	public Channel grpcChannel(GrpcProperties properties) {
		ManagedChannelBuilder<?> managedChannelBuilder = ManagedChannelBuilder
				.forAddress(properties.getHost(), properties.getPort()).usePlaintext(properties.isPlainText())
				.directExecutor();
		if (properties.getIdleTimeout() > 0) {
			managedChannelBuilder = managedChannelBuilder.idleTimeout(properties.getIdleTimeout(), TimeUnit.SECONDS);
		}
		if (properties.getMaxMessageSize() > 0) {
			managedChannelBuilder = managedChannelBuilder.maxInboundMessageSize(properties.getMaxMessageSize());
		}
		return managedChannelBuilder.build();
	}
}
