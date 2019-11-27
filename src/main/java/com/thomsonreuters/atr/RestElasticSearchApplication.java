package com.thomsonreuters.atr;
import com.thomsonreuters.atr.messaging.config.FixDomainJSONConfiguration;
import com.thomsonreuters.atr.messaging.config.IFixDomainConfiguration;
import com.thomsonreuters.atr.messaging.exceptions.FixDomainException;
import com.thomsonreuters.atr.service.ElasticSearchService;
import com.thomsonreuters.atr.util.ConfigStrings;
import com.thomsonreuters.atr.service.LogStashPubSub;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Slf4j
@EnableCaching //enables Spring Caching functionality
public class RestElasticSearchApplication{


    public static void main(String[] args) throws Exception{
        log.info("Starting ElasticServiceApplication");
        IFixDomainConfiguration configuration;
        if (args.length > 0) {
            ConfigStrings.ConfigFilesPath = args[0];
        }
        configuration = openConfig(ConfigStrings.ConfigFilesPath);
        initPostgresConfig(configuration);
        System.setProperty("server.port", configuration.getConfigurationString("/ElasticService/ListenPort","8016"));
        ApplicationContext applicationContext = SpringApplication.run(RestElasticSearchApplication.class, args);
        ElasticSearchService service =  applicationContext.getBean(ElasticSearchService.class);
        service.setTemplate(getRestTemplate());  //To Decouple RestTemplate from ElasticService for mocking in tests
        service.init(configuration);
        log.trace("Finished PostGres read in and now starting Solace");
        LogStashPubSub pubSubService = applicationContext.getBean(LogStashPubSub.class);
        pubSubService.init(configuration,service);
    }

    private static IFixDomainConfiguration openConfig(String fileName) throws FixDomainException {

        if (fileName != null) {
            return new FixDomainJSONConfiguration(fileName + "/config/RESTElasticService.yml");
        } else {
            return new FixDomainJSONConfiguration( ConfigStrings.ConfigFilesPath + "/config/RESTElasticService.yml");
        }
    }

    private static RestTemplate getRestTemplate(){
        CloseableHttpClient httpClient
                = HttpClients.custom()
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

    private static void initPostgresConfig(IFixDomainConfiguration configuration) {

        // Postgres Properties
        System.setProperty("spring.datasource.url",configuration.getConfigurationString(ConfigStrings.PostgresURL,"jdbc:postgresql://localhost:5432/atrdev"));
        System.setProperty("spring.datasource.username", configuration.getConfigurationString(ConfigStrings.PostgresUser,"autex"));
        System.setProperty("spring.datasource.password", configuration.getConfigurationString(ConfigStrings.PostgresPassword,"autex123"));
        System.setProperty("spring.datasource.platform", configuration.getConfigurationString(ConfigStrings.PostgresPlatform,"postgresql"));
        System.setProperty("spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults","false");
        System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQL9Dialect");
    }


}
