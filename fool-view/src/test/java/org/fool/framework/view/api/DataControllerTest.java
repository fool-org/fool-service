package org.fool.framework.view.api;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.view.Application;
import org.fool.framework.view.dto.QueryDataRequest;
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
public class DataControllerTest {

    @Autowired
    private DataController dataController;


    @Test
    public void queryViewDataList() {


        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageSize(20);
        pageNavigator.setPageIndex(1);
        QueryDataRequest dataRequest = new QueryDataRequest();
        dataRequest.setFilter(null);
        dataRequest.setPageInfo(pageNavigator);
        dataRequest.setViewName("CarOwnerList");


        var resp = dataController.queryViewDataList(dataRequest);
        log.info("resp:{}", resp);
    }


}
