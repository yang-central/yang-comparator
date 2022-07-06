package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-13
 */
public class ModuleComparator extends CommonYangStatementComparator<Module> {
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(Module left, Module right) {
        return super.getChangeInfo(left, right);
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(Module left, Module right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED
        || changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"delete a module or change module name,"
                + "it's non-backward-compatible.") ;
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }

    @Override
    protected List<YangCompareResult> compareChildren(Module left, Module right) {
        List<YangCompareResult> results = new ArrayList<>();
        Module temp = left;
        if(temp == null){
            temp = right;
        }
        results.addAll(compareStatements(left==null?new ArrayList<>():left.getEffectiveMetaStatements(),
            right==null?new ArrayList<>():right.getEffectiveMetaStatements(),true));
        results.addAll(compareStatements(left==null?new ArrayList<>():left.getEffectiveLinkageStatement(),
            right==null?new ArrayList<>():right.getEffectiveLinkageStatement(),true));
        if(temp instanceof MainModule){
            results.addAll(compareStatements(left==null?new ArrayList<>():left.getEffectiveDefinitionStatement(),
                right==null?new ArrayList<>():right.getEffectiveDefinitionStatement(),true));
            results.addAll(compareStatements(left==null?new ArrayList<>():YangComparator.getEffectiveSchemaNodeChildren(left),
                right==null?new ArrayList<>():YangComparator.getEffectiveSchemaNodeChildren(right),false));
        }
        return results;
    }


}
