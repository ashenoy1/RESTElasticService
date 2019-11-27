package com.thomsonreuters.atr.service;
import com.thomsonreuters.atr.dao.FixTagDao;
import com.thomsonreuters.atr.entity.Query;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertNotNull;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchServiceTest {

    @Mock
    private FixTagDao daoMock;

    @Mock
    private RestTemplate template;


    @Mock
    private FixParser fixParser;

    @InjectMocks
    private ElasticSearchService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMockCreation(){
        assertNotNull(service);
        assertNotNull(daoMock);
        assertNotNull(template);
    }
    @Test
    @Ignore
    public void testRestTemplate() {
        Query query = new Query();
        query.setQuery("msgtype = D");
        query.setOffset(0);
        query.setUUid("786901");
        String JSONString = service.createJsonString(query.parseQuery(fixParser), query.getOffset(), false);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity(JSONString,headers);
        String testUrl = "";
        service.setTemplate(template);
        service.setAwsMsgURL(testUrl);
        service.getQuery(query,true);
        Mockito.verify(template,Mockito.times(1))
                .exchange(
                        Mockito.anyString(),
                        Mockito.eq(HttpMethod.POST),
                        Mockito.eq(request),
                        Mockito.eq(String.class));

    }
}
