package org.fool.framework.view.dto;


import io.swagger.annotations.ApiModel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fool.framework.view.model.InputType;

import java.util.List;


@Data
@ApiModel("表单输入项")
@NoArgsConstructor
@Builder
public class ViewInputInfo {
    private String property;
    private String text;
    private String legend;
    private InputType inputType;
    private List<OptionInfo> option;
}
