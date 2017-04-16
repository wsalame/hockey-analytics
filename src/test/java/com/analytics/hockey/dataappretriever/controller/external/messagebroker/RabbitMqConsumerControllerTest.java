package com.analytics.hockey.dataappretriever.controller.external.messagebroker;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.analytics.hockey.dataappretriever.main.PropertyConstant;
import com.analytics.hockey.dataappretriever.model.OnMessageConsumption;
import com.analytics.hockey.dataappretriever.model.PropertyLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMqConsumerControllerTest {

	@Mock
	Channel mockedChannel;
	@Mock
	Connection mockedConnection;

	@Spy
	RabbitMqConsumerController rmq;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		rmq.setChannel(mockedChannel);
		rmq.setConnection(mockedConnection);
	}

	@Test
	public void consume_verifyCorrectQueueConsumed() throws IOException {
		/*** given ***/
		String taskQueueName = "fakeQueue";
		OnMessageConsumption<Void> onMessageConsumption = (x, y) -> {
			return null;
		};

		Consumer consumer = new DefaultConsumer(mockedChannel);
		when(rmq.createConsumer(mockedChannel, onMessageConsumption)).thenReturn(consumer);

		/*** when ***/
		rmq.consume(taskQueueName, onMessageConsumption);

		/*** then ***/
		// Checking it's the same queue we passed as a parameter that is consumed
		verify(mockedChannel, times(1)).basicConsume(taskQueueName, consumer);
	}

	@Test
	public void consume_verifyQueueIsDeclaredBeforeConsuming() throws IOException {
		/*** given ***/
		String taskQueueName = "fakeQueue";
		OnMessageConsumption<Void> onMessageConsumption = (x, y) -> {
			return null;
		};

		Consumer consumer = new DefaultConsumer(mockedChannel);

		when(rmq.createConsumer(mockedChannel, onMessageConsumption)).thenReturn(consumer);

		/*** when ***/
		rmq.consume(taskQueueName, onMessageConsumption);

		/*** then ***/
		// Verifying the order, and that the same queue is used
		InOrder inOrder = Mockito.inOrder(mockedChannel);
		ArgumentCaptor<String> queueNameCaptor = ArgumentCaptor.forClass(String.class);

		inOrder.verify(mockedChannel).queueDeclare(queueNameCaptor.capture(), anyBoolean(), anyBoolean(), anyBoolean(),
		        any());
		inOrder.verify(mockedChannel).basicConsume(taskQueueName, consumer);
		assertEquals(queueNameCaptor.getValue(), taskQueueName);
	}

	@Test
	public void addClientShutDownHook_runtimeIsAddingShutDownHook() {
		/*** given ***/
		Runtime mockedRuntime = Mockito.mock(Runtime.class);
		when(rmq.getRuntime()).thenReturn(mockedRuntime);

		/*** when ***/
		rmq.addClientShutDownHook();

		/*** then ***/
		verify(mockedRuntime).addShutdownHook(any());
	}

	@Test
	public void closeConnections_isClosingChannelAndConnection() throws IOException, TimeoutException {
		/*** when ***/
		rmq.closeConnections();

		/*** then ***/
		verify(mockedChannel).close();
		verify(mockedConnection).close(anyInt());
	}

	@Test
	public void handleDelivery_ackIsStillSentAfterException() throws Exception {
		/*** given ***/
		@SuppressWarnings("unchecked")
		OnMessageConsumption<Void> action = Mockito.mock(OnMessageConsumption.class);

		byte[] body = "".getBytes();
		Envelope envelope = Mockito.mock(Envelope.class);
		ArgumentCaptor<Long> envelopeCaptor = ArgumentCaptor.forClass(Long.class);

		doThrow(Exception.class).when(action).execute(body);
		when(envelope.getDeliveryTag()).thenReturn(1234L);

		/*** when ***/
		rmq.handleDelivery(action, body, envelope);

		/*** then ***/
		verify(mockedChannel).basicAck(envelopeCaptor.capture(), anyBoolean());
		assertEquals(envelope.getDeliveryTag(), envelopeCaptor.getValue().longValue());
	}

	@Test
	public void start_hostAndPortAreSetFromPropertyLoader_changeDefaultValues() throws IOException, TimeoutException {
		/*** given ***/
		rmq.setChannel(null);
		rmq.setConnection(null);

		PropertyLoader propertyLoader = Mockito.mock(PropertyLoader.class);
		rmq.setPropertyLoader(propertyLoader);

		ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
		String host = "localhost";
		int port = 1993;

		// if it returns a value, it means it's a defined property, therefore,
		// we should set the host/port
		when(propertyLoader.getProperty(PropertyConstant.RMQ_HOST.toString())).thenReturn(host);
		when(propertyLoader.getPropertyAsInteger(PropertyConstant.RMQ_PORT.toString())).thenReturn(port);

		// create channel and connection
		when(rmq.createConnectionFactory()).thenReturn(connectionFactory);
		when(connectionFactory.newConnection()).thenReturn(mockedConnection);
		when(mockedConnection.createChannel()).thenReturn(mockedChannel);

		/*** when ***/

		rmq.start();

		/*** then ***/
		verify(connectionFactory).setHost(host);
		verify(connectionFactory).setPort(port);
	}

	@Test
	public void start_hostAndPortAreMissingFromPropertyLoader_shouldNotChangeDefaultValues()
	        throws IOException, TimeoutException {
		/*** given ***/
		rmq.setChannel(null);
		rmq.setConnection(null);

		PropertyLoader propertyLoader = Mockito.mock(PropertyLoader.class);
		rmq.setPropertyLoader(propertyLoader);

		ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
		/*** when ***/
		// properties

		// if it returns null, it means it's not a defined property, therefore,
		// we shouldn't set the host/port
		when(propertyLoader.getProperty(PropertyConstant.RMQ_HOST.toString())).thenReturn(null);
		when(propertyLoader.getPropertyAsInteger(PropertyConstant.RMQ_PORT.toString())).thenReturn(null);

		// create channel and connection
		when(rmq.createConnectionFactory()).thenReturn(connectionFactory);
		when(connectionFactory.newConnection()).thenReturn(mockedConnection);
		when(mockedConnection.createChannel()).thenReturn(mockedChannel);

		rmq.start();

		/*** then ***/
		verify(connectionFactory, never()).setHost(anyString());
		verify(connectionFactory, never()).setPort(anyInt());
	}

	@Test
	public void awaitInitialization_readyAtThirdTry() {
		/** given **/
		int maxRetries = 3;

		when(mockedChannel.isOpen()).thenReturn(false, false, true);
		when(rmq.getMaxRetries()).thenReturn(maxRetries);

		/*** when ***/
		rmq.awaitInitialization();
		/*** then ***/

		// Might fail if RabbitMqConsumerController#MAX_RETRIES < 3
		verify(mockedChannel, times(maxRetries)).isOpen();
	}

	@Test
	public void awaitInitialization_notReadyInTime() {
		/** given **/
		when(rmq.getMaxRetries()).thenReturn(5);
		when(mockedChannel.isOpen()).thenReturn(false, new Boolean[] { false, false, false });

		/*** when ***/
		rmq.awaitInitialization();
		/*** then ***/
		verify(mockedChannel, atLeast(rmq.getMaxRetries() + 1)).isOpen();
	}

	@Test
	public void handleDelivery_ackIsSent() throws IOException {
		/*** given ***/
		@SuppressWarnings("unchecked")
		OnMessageConsumption<Void> action = Mockito.mock(OnMessageConsumption.class);

		byte[] body = "".getBytes();
		Envelope envelope = Mockito.mock(Envelope.class);

		ArgumentCaptor<Long> envelopeCaptor = ArgumentCaptor.forClass(Long.class);

		/*** when ***/
		when(envelope.getDeliveryTag()).thenReturn(1234L);
		rmq.handleDelivery(action, body, envelope);

		/*** then ***/
		verify(mockedChannel).basicAck(envelopeCaptor.capture(), anyBoolean());
		assertEquals(envelope.getDeliveryTag(), envelopeCaptor.getValue().longValue());
	}
}