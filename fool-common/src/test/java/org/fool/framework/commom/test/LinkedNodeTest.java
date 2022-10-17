package org.fool.framework.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class LinkedNodeTest {

    @Test
    public void revert() {
        LinkedNode<Integer> current = new LinkedNode<Integer>();
        LinkedNode<Integer> head = current;
        for (Integer i = 0; i < 10; i++) {
            current.setData(i);
            current.setNext(new LinkedNode<Integer>());
            current = current.getNext();
        }
        current.setNext(null);
        var node = head;
        while (node != null) {
            System.out.println("current value :" + String.valueOf(node.getData()));
            node = node.getNext();
        }
        node = LinkedNode.revert(head);
        System.out.println("the revert result");
        while (node != null) {
            System.out.println("current value :" + String.valueOf(node.getData()));
            node = node.getNext();
        }

    }

}
