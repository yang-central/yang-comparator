package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.Base;
import com.huawei.yang.model.api.stmt.Identity;
import com.huawei.yang.model.api.stmt.Type;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-15
 */
public class BaseComparator extends CommonYangStatementComparator<Base>{


    @Override
    protected CompatibilityInfo defaultCompatibility(Base left, Base right,
        CompatibilityRule.ChangeInfo changeInfo) {
        Base temp = left;
        if(temp == null){
            temp = right;
        }
        if(temp.getParentStatement() instanceof Identity){
            if(changeInfo == CompatibilityRule.ChangeInfo.DELETED){
                return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                    "delete a base for identity,it's non-backward-compatible.");
            }
        } else if(temp.getParentStatement() instanceof Type){
            if(changeInfo == CompatibilityRule.ChangeInfo.ADDED){
                return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                    "add a new base for identity-ref, it's non-backward-compatible.");
            }
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
