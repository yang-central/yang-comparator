package com.huawei.yang.comparator;

import com.huawei.yang.model.api.schema.SchemaTreeType;
import com.huawei.yang.model.api.stmt.Config;
import com.huawei.yang.model.api.stmt.SchemaNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-13
 */
public class ConfigComparator extends CommonYangStatementComparator<Config>{
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(Config left, Config right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        changeInfos.addAll(super.getChangeInfo(left, right));
        if(left.getContext().getSelf() instanceof SchemaNode){
            SchemaNode schemaNode = (SchemaNode) left.getContext().getSelf();
            if(schemaNode.getSchemaTreeType()!= SchemaTreeType.DATATREE){
                changeInfos.add(CompatibilityRule.ChangeInfo.IGNORE);
                return changeInfos;
            }
        } else if(left.getParentStatement() != null){
            if(left.getParentStatement() instanceof SchemaNode){
                SchemaNode schemaNode = (SchemaNode) left.getParentStatement();
                if(schemaNode.getSchemaTreeType()!= SchemaTreeType.DATATREE){
                    changeInfos.add(CompatibilityRule.ChangeInfo.IGNORE);
                    return changeInfos;
                }
            }
        }
        if(left.isConfig() && !right.isConfig()){
            changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
        } else if(!left.isConfig()&& right.isConfig()){
            changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
        }

        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(Config left, Config right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.REDUCE){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                "config is changed from false to true, it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
