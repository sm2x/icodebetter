package iwb;

import java.util.Map;
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
		
			
		}
		
		Map<String, String> envMap = System.getenv();
		if(envMap!=null) {
			if(envMap.get("INFLUXDB")!=null)FrameworkSetting.argMap.put("influxdb",envMap.get("INFLUXDB"));
			if(envMap.get("logType")!=null)FrameworkSetting.argMap.put("logType",envMap.get("logType"));
			if(envMap.get("PROJECT")!=null)FrameworkSetting.argMap.put("project",envMap.get("PROJECT"));
		}
		
		
		String influxdb = FrameworkSetting.argMap.get("influxdb");
		if(influxdb!=null && !influxdb.equals("0")) {
			FrameworkSetting.log2tsdb = true;
			FrameworkSetting.log2tsdbUrl = influxdb.equals("1") ? "influxdb:8086" : influxdb;
			if(!FrameworkSetting.log2tsdbUrl.startsWith("http"))FrameworkSetting.log2tsdbUrl = "http://"+FrameworkSetting.log2tsdbUrl;
			FrameworkSetting.argMap.put("influxdb", FrameworkSetting.log2tsdbUrl);
		}
		String logType = FrameworkSetting.argMap.get("logType");
		if(logType!=null)FrameworkSetting.logType = GenericUtil.uInt(logType);
		
		if(FrameworkSetting.argMap.get("timer")!=null)FrameworkSetting.localTimer=true;
		
		if(FrameworkSetting.argMap.get("project")!=null)FrameworkSetting.projectId=FrameworkSetting.argMap.get("project");
		

		ConfigurableApplicationContext appContext = SpringApplication.run(FrameworkApplication.class, args);
		
		
		FrameworkService service = (FrameworkService)appContext.getBean("frameworkService");
		VcsService vcsService = (VcsService)appContext.getBean("vcsService");

		boolean oldLogVcs = FrameworkSetting.logVcs;
		FrameworkSetting.logVcs = false;
		if(GenericUtil.uInt(FrameworkSetting.argMap.get("metadata"))!=0) {
			FrameworkSetting.systemStatus=0;
			if(FrameworkSetting.projectId == null) FrameworkSetting.projectId = FrameworkSetting.devUuid;
			vcsService.importProjectMetadata("http://code2.io/app/export/" + FrameworkSetting.projectId + ".zip");//"classpath:projects/"+FrameworkSetting.projectId+".zip"
			FrameworkSetting.projectSystemStatus.put(FrameworkSetting.projectId, 0);
			FrameworkSetting.metadata = true;
		} else {
			if(FrameworkSetting.projectId!=null && GenericUtil.uInt(FrameworkSetting.argMap.get("noupdate"))==0) {
				vcsService.icbVCSUpdateSqlAndFields();

				boolean b = vcsService.projectVCSUpdate(FrameworkSetting.devUuid);
				if(b && FrameworkSetting.projectId!=null) {
	//				W5Project po = FrameworkCache.getProject(FrameworkSetting.projectId);
					if(FrameworkSetting.projectId.length()==36) {
						boolean clean = GenericUtil.uInt(FrameworkSetting.argMap.get("clean"))!=0;
						if(clean)vcsService.deleteProject(FrameworkSetting.projectId);
						vcsService.projectVCSUpdate(FrameworkSetting.projectId);
					}
				}
			}
			else
				if(FrameworkSetting.logVcs)vcsService.vcsCheck4VCSLogSchema();

			service.reloadCache(-1);
		}
		FrameworkSetting.logVcs = oldLogVcs;
		if(FrameworkSetting.logVcs)vcsService.vcsCheck4VCSLogSchema();
		
		if(FrameworkSetting.localTimer) {
			TimerTask timerTask = new GenericTimer((TaskExecutor)appContext.getBean("taskExecutor")
					, service);
	        //running timer task as daemon thread
	        Timer timer = new Timer(true);
	        timer.scheduleAtFixedRate(timerTask, 0, 60*1000); //every minute
		}
		
		if(FrameworkSetting.log2tsdb)LogUtil.activateInflux4Log();
		if(FrameworkSetting.logType==2)LogUtil.activateMQ4Log();
		

	}
}
