package iwb;

import java.util.Timer;
import java.util.TimerTask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import iwb.cache.FrameworkSetting;
import iwb.service.FrameworkService;
import iwb.service.VcsService;
import iwb.timer.GenericTimer;
import iwb.util.GenericUtil;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import iwb.util.LogUtil;

@SpringBootApplication
@ServletComponentScan
//@EnableRedisHttpSession
public class FrameworkApplication {

	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setCorePoolSize(5);
		threadPoolTaskExecutor.setMaxPoolSize(10);
		threadPoolTaskExecutor.setQueueCapacity(25);
		return threadPoolTaskExecutor;
	}
	
	
	public static void main(String[] args) {
		
		//PropertyConfigurator.configure("./log4j.properties");
		
		if(args!=null && args.length>0) {
			for(int qi=0;qi<args.length;qi++){

				String arg = args[qi];
				if(!GenericUtil.isEmpty(arg)){
					String key = arg, value="1";
					if(arg.indexOf('=')>-1) {
						String[] oo = arg.replace('=', ',').split(",");
						key = oo[0];
						value = oo[1];
					}
					FrameworkSetting.argMap.put(key, value);
				}
			}
		
			String influxdb = FrameworkSetting.argMap.get("influxdb");
			if(influxdb!=null && !influxdb.equals("0")) {
				FrameworkSetting.log2tsdb = true;
				FrameworkSetting.log2tsdbUrl = influxdb.equals("1") ? "influxdb:8086" : influxdb;
				if(!FrameworkSetting.log2tsdbUrl.startsWith("http"))FrameworkSetting.log2tsdbUrl = "http://"+FrameworkSetting.log2tsdbUrl;
			}
			String logType = FrameworkSetting.argMap.get("logType");
			if(logType!=null)FrameworkSetting.logType = GenericUtil.uInt(logType);
			
			if(FrameworkSetting.argMap.get("timer")!=null)FrameworkSetting.localTimer=true;
			
			if(FrameworkSetting.argMap.get("project")!=null)FrameworkSetting.projectId=FrameworkSetting.argMap.get("project");
		}

		ConfigurableApplicationContext appContext = SpringApplication.run(FrameworkApplication.class, args);
		
		
		FrameworkService service = (FrameworkService)appContext.getBean("frameworkService");
		VcsService vcsService = (VcsService)appContext.getBean("vcsService");
		
		if(FrameworkSetting.localTimer) {
			TimerTask timerTask = new GenericTimer((TaskExecutor)appContext.getBean("taskExecutor")
					, service);
	        //running timer task as daemon thread
	        Timer timer = new Timer(true);
	        timer.scheduleAtFixedRate(timerTask, 0, 60*1000); //every minute
		}
		
		if(FrameworkSetting.projectId!=null) {
			vcsService.icbVCSUpdateSqlAndFields();
			boolean b = vcsService.projectVCSUpdate("067e6162-3b6f-4ae2-a221-2470b63dff00");
			if(b && FrameworkSetting.projectId!=null) {
//				W5Project po = FrameworkCache.getProject(FrameworkSetting.projectId);
				if(FrameworkSetting.projectId.length()==36) {
					boolean clean = GenericUtil.uInt(FrameworkSetting.argMap.get("clean"))!=0;
					if(clean)vcsService.deleteProject(FrameworkSetting.projectId);
					vcsService.projectVCSUpdate(FrameworkSetting.projectId);
				}
			}
		}
		if(FrameworkSetting.log2tsdb)LogUtil.activateInflux4Log();
		if(FrameworkSetting.logType==2)LogUtil.activateMQ4Log();
		
		service.reloadCache(-1);
	}
}
