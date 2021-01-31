package org.fool.framework.view.api;

import lombok.extern.slf4j.Slf4j;
import org.fool.framework.view.dto.ViewDataRequest;
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
public class ViewControllerTest {


    @Autowired
    private ViewController viewController;

    @Test
    public void TestGetView() {
        String name = "CarOwnerList";
        ViewDataRequest req = new ViewDataRequest();
        req.setViewName(name);
        log.info("{}", viewController.getViewData(req));

    }
}
