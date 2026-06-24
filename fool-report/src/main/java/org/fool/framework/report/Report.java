package org.fool.framework.report;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Report {
    private String name;
    private List<Param> params;
    private UUID id;
    private String no;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public List<ReportResultTable> getResult() {
        throw new UnsupportedOperationException();
    }

    public void setResult(List<ReportResultTable> result) {
    }

    public IReportSource getSource() {
        throw new UnsupportedOperationException();
    }

    public void setSource(IReportSource source) {
    }

    public LocalDateTime getCreateTime() {
        throw new UnsupportedOperationException();
    }

    public void setCreateTime(LocalDateTime createTime) {
    }

    public String getCreatePerson() {
        throw new UnsupportedOperationException();
    }

    public void setCreatePerson(String createPerson) {
    }

    public LocalDateTime getModifyTime() {
        throw new UnsupportedOperationException();
    }

    public void setModifyTime(LocalDateTime modifyTime) {
    }

    public String getMoidiyPerson() {
        throw new UnsupportedOperationException();
    }

    public void setMoidiyPerson(String moidiyPerson) {
    }
}
