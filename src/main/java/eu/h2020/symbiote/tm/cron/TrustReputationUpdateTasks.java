package eu.h2020.symbiote.tm.cron;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.converters.Auto;
import eu.h2020.symbiote.cloud.monitoring.model.AggregatedMetrics;
import eu.h2020.symbiote.tm.interfaces.rest.AuthManager;
import eu.h2020.symbiote.tm.models.QoS;
import eu.h2020.symbiote.tm.models.ResourceQosDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.h2020.symbiote.cloud.trust.model.TrustEntry;
import eu.h2020.symbiote.cloud.trust.model.TrustEntry.Type;
import eu.h2020.symbiote.tm.repositories.TrustRepository;
import eu.h2020.symbiote.tm.services.TrustAMQPService;
import eu.h2020.symbiote.tm.services.TrustCalculationService;
import org.springframework.web.client.RestTemplate;

/**
 * @author RuggenthalerC
 * 
 *         Handles the scheduled updates for all trust/reputation values.
 *
 */
@Component("tasks")
public class TrustReputationUpdateTasks {
	private static final Logger logger = LoggerFactory.getLogger(TrustReputationUpdateTasks.class);

	@Value("${symbIoTe.trust.update.interval}")
	private Integer interval;

	@Value("${platform.id}")
	private String ownPlatformId;

//	@Value("${symbIoTe.core.aams.url}")
//	private String symbioteAamsUrl;

	@Value("${baas.base.url}")
	private String baasUrl;

	@Value("${symbIoTe.monitoring.url}")
	private String monitoringUrl;

	@Autowired
	private ObjectMapper objectMapper;

//	@Autowired
	private TrustAMQPService amqpService;

//	@Autowired
	private TrustCalculationService trustService;

//	@Autowired
	private TrustRepository trustRepository;

	@Autowired
	private RestTemplate restTemplate;

//	@Autowired
//	private AuthManager authManager;

	//@Scheduled(cron = "${symbIoTe.trust.resource_trust.period}")
//	public void scheduleResourceTrustUpdate() {
//		List<TrustEntry> entries = trustRepository.findEntriesUpdatedAfterByPlatform(getUpdateInterval(), Type.RESOURCE_TRUST, ownPlatformId);
//		logger.debug("Resource Trust update triggered for {} entries", entries.size());
//
//		entries.forEach(entry -> {
//			Double curVal = entry.getValue();
//
//			entry.updateEntry(trustService.calcResourceTrust(entry.getResourceId()));
//			trustRepository.save(entry);
//
//			if (shouldPublishUpdate(curVal, entry.getValue())) {
//				amqpService.publishResourceTrustUpdate(entry);
//				logger.debug("Resource Trust for resource {} updated: {} to {}", entry.getResourceId(), curVal, entry.getValue());
//			}
//		});
//	}

	//@Scheduled(cron = "${symbIoTe.trust.platform_reputation.period}")
//	public void schedulePlatformReputationUpdate() {
//		List<TrustEntry> entries = trustRepository.findEntriesUpdatedAfter(getUpdateInterval(), Type.PLATFORM_REPUTATION);
//		logger.debug("Platform Reputation update triggered for {} entries", entries.size());
//
//		entries.forEach(entry -> {
//			Double curVal = entry.getValue();
//
//			entry.updateEntry(trustService.calcPlatformReputation(entry.getPlatformId()));
//			trustRepository.save(entry);
//
//			if (shouldPublishUpdate(curVal, entry.getValue())) {
//				amqpService.publishPlatformReputationUpdate(entry);
//				logger.debug("Platform Reputation for platform {} updated: {} to {}", entry.getPlatformId(), curVal, entry.getValue());
//			}
//		});
//	}

	//@Scheduled(cron = "${symbIoTe.trust.adaptive_resource_trust.period}")
	public void scheduleAdaptiveResourceTrustUpdate() {
		List<TrustEntry> entries = trustRepository.findEntriesUpdatedAfter(getUpdateInterval(), Type.ADAPTIVE_RESOURCE_TRUST);
		logger.debug("Adaptive Resource Trust update triggered for {} entries", entries.size());

		entries.forEach(entry -> {
			Double curVal = entry.getValue();

			entry.updateEntry(trustService.calcAdaptiveResourceTrust(curVal, entry.getResourceId(), entry.getPlatformId()));
			trustRepository.save(entry);

			if (shouldPublishUpdate(curVal, entry.getValue())) {
				amqpService.publishAdaptiveResourceTrustUpdate(entry);
				logger.debug("Adaptive Resource Trust for resource {} updated: {} to {}", entry.getResourceId(), curVal, entry.getValue());
			}
		});
	}

	@Scheduled(cron = "${symbIoTe.qos.period}")
	public void getQualityOfServiceMetrics(){
		logger.info("Sending Quality of Service metrics to BaaS");
		List<ResourceQosDto> qos = new ArrayList<>();
		ResponseEntity<String> resp = restTemplate.exchange(monitoringUrl+"?deviceId=&metric=&operation=avg&", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
				new ParameterizedTypeReference<String>() {
				});
		try{
			List<LinkedHashMap> itemList = objectMapper.readValue(resp.getBody(), List.class);
			Map<String, Double> statistics;
			for(Map item: itemList){
				statistics = (Map<String, Double>) itemList.get(0).get("statistics");
				double avg = statistics.get("avg");
				qos.add(new ResourceQosDto(item.get("deviceId").toString(), new QoS(avg, 0.0, 0.0)));
			}
//			Map<String, Double> statistics= (Map<String, Double>) itemList.get(0).get("statistics");
//			ResourceQosDto res = new ResourceQosDto(itemList.get(0).get("deviceId").toString(), new QoS(statistics.get("avg"), 0.0, 0.0));
			System.out.println(objectMapper.writeValueAsString(qos));
			resp = restTemplate.exchange(baasUrl+"/products/update_resource_qos", HttpMethod.PATCH, new HttpEntity<>(objectMapper.writeValueAsString(qos), new HttpHeaders()),
					new ParameterizedTypeReference<String>() {
					});
			logger.info("QoS submitted: {}", resp.getStatusCode());
		}
		catch(Exception e){
			logger.info(e.getMessage());
		}
	}

	private Date getUpdateInterval() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -interval);
		return cal.getTime();
	}

	private boolean shouldPublishUpdate(Double curVal, Double newVal) {
		if (curVal != null) {
			return !curVal.equals(newVal);
		}

		return newVal != null;
	}
}