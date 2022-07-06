package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-10
 */
public abstract class AbstractYangStatementComparator<T extends YangStatement> implements YangStatementComparator<T>{
    public AbstractYangStatementComparator() {
    }

    public CompatibilityRules getCompatibilityRules() {
        return CompatibilityRules.getInstance();
    }

    protected abstract List<CompatibilityRule.ChangeInfo> getChangeInfo(T left, T right);

    protected abstract CompatibilityInfo defaultCompatibility(T left, T right,
        CompatibilityRule.ChangeInfo changeInfo);
}
