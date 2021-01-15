package com.github.yfge.fool.common.data.tree;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;

public enum TreeNodeCompareResult {
    Parent(-2),
    Child(1),
    NextNode(2),
    PreNode(-1),
    Equal(0),
    NextLevel(3);

    @Getter
    private int value;
    private TreeNodeCompareResult(int val){
        this.value = val;
    }

}
