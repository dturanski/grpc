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
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.app.grpc.test.support.ProcessorServer;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author David Turanski
 **/
@SpringBootTest(classes = GrpcProcessorTests.TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RunWith(SpringRunner.class)
public abstract class GrpcProcessorTests {
	private static ProcessorServer server;
	private static ManagedChannel inProcessChannel;
	private static String serverName = UUID.randomUUID().toString();

	@BeforeClass
	public static void setUp() throws Exception {

		server = new ProcessorServer(InProcessServerBuilder.forName(serverName).directExecutor());
		server.start();
		inProcessChannel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		inProcessChannel.shutdownNow();
		server.stop();
	}

	public static class ProcessorTests extends GrpcProcessorTests {

		@Autowired
		private MessageCollector messageCollector;

		@Autowired
		private Processor processor;

		@Test
		public void test() throws InterruptedException {
			processor.input().send(new GenericMessage<String>("hello"));
			Message<?> message = messageCollector.forChannel(processor.output()).poll(2, TimeUnit.SECONDS);
			assertThat(message.getPayload()).isEqualTo("HELLO");
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(GrpcProcessorConfiguration.class)
	static class TestConfiguration {
		@Bean
		public Channel channel() {
			return inProcessChannel;
		}
	}
}

