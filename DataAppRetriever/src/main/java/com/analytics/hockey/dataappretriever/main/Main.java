package com.analytics.hockey.dataappretriever.main;

import com.analytics.hockey.dataappretriever.main.injector.AppModule;
import com.analytics.hockey.dataappretriever.main.injector.GuiceInjector;
import com.analytics.hockey.dataappretriever.service.InitServices;

public class Main {

	public static void main(String[] args) throws Exception {
		GuiceInjector.getInstance().install(new AppModule());
		GuiceInjector.get(InitServices.class).init();
	}

}
