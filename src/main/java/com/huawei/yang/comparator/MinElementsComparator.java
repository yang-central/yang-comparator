package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.MinElements;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-15
 */
public class MinElementsComparator extends CommonYangStatementComparator<MinElements> {
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(MinElements left, MinElements right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if(left.getValue() >right.getValue()){
            changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
        } else if(left.getValue() < right.getValue()){
            changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
        }
        if(super.getChangeInfo(left, right).contains(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED)){
            changeInfos.add(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED);
        }
        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(MinElements left, MinElements right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.REDUCE){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"min-elements is increased,"
                + "it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
