package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.view.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class DataQueryServiceTest {

    @Autowired
    private DataQueryService dataQueryService;


    @Test
    public void queryViewDataList() {
        String name = "CarOwnerList";
        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageIndex(1);
        pageNavigator.setPageSize(20);
        var data = dataQueryService.queryViewDataList(name, null, pageNavigator);
        log.info("data:{}", data);
    }

}
