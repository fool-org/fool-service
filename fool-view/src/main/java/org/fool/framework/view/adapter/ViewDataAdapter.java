package org.fool.framework.view.adapter;


import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.PageResult;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListViewResult;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;

@Component
public class ViewDataAdapter {

    public ListViewResult getListViewResult(PageResult<IDynamicData> item){
        ListViewResult result = new ListViewResult();



        result.setPageInfo(item.getPageInfo());

        result.setItems(new LinkedList<>());
        if(!CollectionUtils.isEmpty(item.getItems())){
            item.getItems().forEach(p->{

                ListDataItem dataItem= new ListDataItem();
                dataItem.setValues(p.toMap());
                dataItem.setId(p.getId());
                result.getItems().add(dataItem);

            });
        }

        return  result;

    }
}
