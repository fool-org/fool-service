package org.fool.framework.auth.api;

import org.fool.framework.auth.business.Order;
import org.fool.framework.dao.DaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private DaoService daoService;

    @GetMapping("/test")
    @ResponseBody
    public Object getTest() {
        return daoService.getAllList(Order.class);
    }
}
