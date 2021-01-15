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
import java.util.List;

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


        Order order = daoService.getOneByKey(Order.class, 613);
        log.info("get one by key :{}", order);
    }

    @Test
    public void create() {
        Order order = daoService.getOneByKey(Order.class, 613);
        order.setOrderSymbol("testOrder");
        order.setOrderStopPrice(BigDecimal.ONE);
        order.setOrderPrice(BigDecimal.ONE);
        daoService.create(order);
        log.info("order created:{}", order);
    }

    @Test
    public void selectList() {

        String sql = "select * from `market_order` where id>? and id < ?";

        List<Order> orderList = daoService.selectList(Order.class, sql, 100, 110);
        for (var i : orderList) {
            log.info("{}", i);
        }
    }

    @Test
    public void testSelectList() {

        String sql = "select * from `market_order` where id>100 and id < 110";

        List<Order> orderList = daoService.selectList(Order.class, sql);
        for (var i : orderList) {
            log.info("{}", i);
        }
    }
}
