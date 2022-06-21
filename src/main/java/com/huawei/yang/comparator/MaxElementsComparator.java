package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.MaxElements;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-15
 */
public class MaxElementsComparator extends CommonYangStatementComparator<MaxElements>{
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(MaxElements left, MaxElements right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if(left.isUnbounded()&&!right.isUnbounded()){
            changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
        } else if(!left.isUnbounded() && right.isUnbounded()){
            changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
        } else if(!left.isUnbounded() && !right.isUnbounded()){
            if(left.getValue() < right.getValue()){
                changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
            } else if(left.getValue() > right.getValue()){
                changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
            }
        }
        if(super.getChangeInfo(left, right).contains(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED)){
            changeInfos.add(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED);
        }
        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(MaxElements left, MaxElements right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.REDUCE){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"the max-elements is reduced,"
                + "it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
