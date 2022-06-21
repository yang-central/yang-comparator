package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.Status;
import com.huawei.yang.model.api.stmt.StatusStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-15
 */
public class StatusComparator extends CommonYangStatementComparator<StatusStmt>{
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(StatusStmt left, StatusStmt right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if(!left.equals(right)){
            if(left.getStatus() == Status.CURRENT){
                changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
            } else if(left.getStatus() == Status.DEPRECATED && right.getStatus() == Status.OBSOLETE){
                changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
            } else {
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
    protected CompatibilityInfo defaultCompatibility(StatusStmt left, StatusStmt right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.REDUCE){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"un-acceptable change of status,"
                + "changed from current to deprecated or obsolete and changed from deprecated to obsolete ard acceptable.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
