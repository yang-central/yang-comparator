package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.type.SectionExpression;


import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class RangeLengthComparator extends CommonYangStatementComparator<SectionExpression>{
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(SectionExpression left, SectionExpression right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if(!left.equals(right)){
            if(left.isSubSet(right)){
                changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
            } else if(right.isSubSet(left)){
                changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
            }
        }

        List<CompatibilityRule.ChangeInfo> superChangeInfos = super.getChangeInfo(left, right);
        if(superChangeInfos.contains(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED)){
            changeInfos.add(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED);
        }
        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(SectionExpression left, SectionExpression right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.REDUCE){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"the range or length is reduced, "
                + "it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
