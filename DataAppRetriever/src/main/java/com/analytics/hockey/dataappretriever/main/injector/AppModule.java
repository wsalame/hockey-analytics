package com.analytics.hockey.dataappretriever.main.injector;

import com.analytics.hockey.dataappretriever.controller.external.elasticsearch.ElasticsearchWriteController;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.DefaultHockeyScrapperController;
import com.analytics.hockey.dataappretriever.controller.external.hockeyscrapper.HockeyScrapper;
import com.analytics.hockey.dataappretriever.controller.external.messagebroker.MessageConsumer;
import com.analytics.hockey.dataappretriever.controller.external.messagebroker.RabbitMqConsumerController;
import com.analytics.hockey.dataappretriever.service.InitServices;
import com.google.inject.AbstractModule;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ElasticsearchWriteController.class).asEagerSingleton();
		bind(MessageConsumer.class).to(RabbitMqConsumerController.class).asEagerSingleton();
		bind(HockeyScrapper.class).to(DefaultHockeyScrapperController.class).asEagerSingleton();
		bind(InitServices.class);
	}

}
