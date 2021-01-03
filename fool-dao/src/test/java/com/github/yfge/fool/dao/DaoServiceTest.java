package com.github.yfge.fool.dao;

import com.github.yfge.fool.dao.business.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = Application.class)
public class DaoServiceTest {

    @Autowired
    private DaoService daoService;

    @Test
    public void getAllList() {
        var items = daoService.getAllList(Order.class);
        for (var i : items
        ) {
//            log.info("{}", i);

        }
    }

    @Test
    public void getOneByKey() {
    }

    @Test
    public void getByPage() {
    }

    @Test
    public void save() {

        Order order = new Order();
        order.setId("613");
        order.setCreateAt(LocalDateTime.now());
        order.setOrderPrice(BigDecimal.valueOf(333333));
        order.setOrderStopPrice(BigDecimal.valueOf(99999));
        order.setOrderSymbol("testUSD");
        daoService.save(order);
    }

    @Test
    public void delete() {


        Order order = daoService.getOneByKey(613, Order.class);
        log.info("get one by key :{}", order);
    }
}
