package org.fool.framework.common.data;

import org.fool.framework.common.annotation.Column;
import org.fool.framework.common.annotation.GenerationType;
import org.fool.framework.common.annotation.Id;

public abstract class BusinessObject implements IBusinessObject {
    @Id("BO_Id")
    @Column(value = "BO_Id", key = true, generationType = GenerationType.ON_INSERT)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
