package com.thomsonreuters.atr.dao;


import com.thomsonreuters.atr.entity.FixTag;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
@Slf4j
public class FixTagDaoTest {

    @Mock
    private DataSource ds;

    @Mock
    private FixTagDaoImpl fixTagDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMockCreation(){
        assertNotNull(ds);
        assertNotNull(fixTagDao);
    }

    @Test
    public void testFixTag(){
        List<FixTag> tagList = new ArrayList<>();
        when(fixTagDao.getTags(
        )).thenReturn(tagList);
        fixTagDao.setFixTags();
        Assert.assertEquals(fixTagDao.getTags(),tagList);
    }

}

