package org.fool.framework.view.service;

import lombok.extern.slf4j.Slf4j;
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
public class ViewDataServiceTest {


    @Autowired
    private ViewDataService viewDataService;

    @Test
    public void getViewData() {
        String name = "CarOwnerList";

        var view = viewDataService.getViewData(name, "");

        log.info("{}", view);
    }
}
