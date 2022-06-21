package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.Namespace;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-14
 */
public class NamespaceComparator extends CommonYangStatementComparator<Namespace>{
    @Override
    protected CompatibilityInfo defaultCompatibility(Namespace left, Namespace right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.CHANGED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,"namespace MUST NOT be changed.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
