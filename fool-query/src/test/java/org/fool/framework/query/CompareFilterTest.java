//package org.fool.framework.query;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@Slf4j
////@RunWith(SpringRunner.class)
//@SpringBootApplication
////@SpringBootTest(classes = Application.class)
//public class CompareFilterTest {
//
//
//    @Test
//    public void revert() {
//        CompareFilter.LinkedNode<Integer> current = new CompareFilter.LinkedNode<Integer>();
//        CompareFilter.LinkedNode<Integer> head = current;
//        for (Integer i = 0; i < 10; i++) {
//            current.setData(i);
//            current.setNext(new CompareFilter.LinkedNode<Integer>());
//            current = current.getNext();
//        }
//        current.setNext(null);
//        var node = head;
//        while (node != null) {
//            log.info("current value :{}", node.getData());
//            node = node.getNext();
//        }
//        node = CompareFilter.revert(head);
//        log.info("the revert result");
//        while (node != null) {
//            log.info("current value :{}", node.getData());
//            node = node.getNext();
//        }
//
//    }
//}
