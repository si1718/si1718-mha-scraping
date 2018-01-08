package data.scraping.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import data.scraping.utils.MongoUtils;

public class TestBatch {
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void updateProjects() {
		final Runnable projects = new Runnable() {
			public void run() {
				System.out.println("UPDATING PROJECTS ("+new Date()+")");
				try {
					MongoUtils.updateProjects();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(projects, 0, 1, TimeUnit.DAYS);
		
	}
	
	public static void main(String... args) {
		//MongoUtils.initialize();
		new TestBatch().updateProjects();
	}
}