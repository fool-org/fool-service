package org.fool.framework.view.api;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.view.Application;
import org.fool.framework.view.TestReadAuthorization;
import org.fool.framework.view.dto.QueryDataRequest;
import org.fool.framework.view.service.ReadAuthorizationEnforcer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class DataControllerTest {

    @Autowired
    private DataController dataController;
    @MockBean
    private ReadAuthorizationEnforcer authorizationEnforcer;

    @Before
    public void configureAuthorization() {
        TestReadAuthorization.configure(authorizationEnforcer);
    }

    @Test
    public void queryViewDataList() {


        PageNavigator pageNavigator = new PageNavigator();
        pageNavigator.setPageSize(20);
        pageNavigator.setPageIndex(1);
        QueryDataRequest dataRequest = new QueryDataRequest();
        dataRequest.setFilter(null);
        dataRequest.setPageInfo(pageNavigator);
        dataRequest.setViewId(100L);


        var resp = dataController.queryViewDataList(dataRequest);
        log.info("resp:{}", resp);
    }


}
