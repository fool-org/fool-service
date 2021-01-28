package org.fool.framework.view.adapter;

import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.TableColumnInfo;
import org.fool.framework.view.dto.ViewInputInfo;
import org.fool.framework.view.model.InputType;
import org.fool.framework.view.model.View;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class ViewAdapter {

    public ListViewInfo getViewInfo(View view) {

        ListViewInfo result = new ListViewInfo();


        result.setId(view.getId());
        result.setViewName(view.getViewName());
        result.setViewTitle(view.getViewTitle());
        result.setViewType(view.getViewType());
        result.setBrowserTitle(view.getViewRemark());
        result.setTableColumn(new LinkedList<>());
        view.getListItems().forEach(p -> {
            result.getTableColumn().add(
                    TableColumnInfo.builder().property(p.getModelProperty())
                            .title(p.getItemLabel())
                            .build());
        });
        result.setInputInfo(new LinkedList<>());
        view.getListItems().stream().filter(p -> p.getInputType() != InputType.READ_ONLY).forEach(p -> {
            result.getInputInfo().add(
                    ViewInputInfo.builder()
                            .inputType(p.getInputType())
                            .legend(p.getItemLegend())
                            .option(new LinkedList<>())
                            .property(p.getModelProperty())
                            .text(p.getItemLabel())
                            .build()
            );
        });


        return result;
    }
}
