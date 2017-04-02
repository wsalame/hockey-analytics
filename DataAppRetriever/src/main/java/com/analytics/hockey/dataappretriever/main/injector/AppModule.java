package com.analytics.hockey.dataappretriever.main.injector;

import com.google.inject.AbstractModule;

public class AppModule extends AbstractModule {
	@Override
	protected void configure() {
		//TODO
//		bind(PropertyLoader.class).to(DefaultPropertyLoader.class).asEagerSingleton();
//		bind(MessageConsumer.class).to(RabbitMqConsumerController.class).asEagerSingleton();
//		bind(HockeyScrapper.class).to(DefaultHockeyScrapperController.class).asEagerSingleton();
//		bind(ElasticsearchWriteController.class).asEagerSingleton(); //TODO interface
//		bind(InitServices.class); // TODO test sans cette ligne
	}
}
