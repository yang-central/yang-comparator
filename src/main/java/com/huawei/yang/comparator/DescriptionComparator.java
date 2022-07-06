package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Description;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class DescriptionComparator extends CommonYangStatementComparator<Description>{
    @Override
    protected CompatibilityInfo defaultCompatibility(Description left, Description right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.DELETED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"delete description statement, it's non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
