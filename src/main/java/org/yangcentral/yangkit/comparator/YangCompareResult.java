package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.stmt.Module;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-05-24
 */
public interface YangCompareResult<T> {
    ChangeType getChangeType();
    T getLeft();
    T getRight();
    String getChangeDescription(String indent);
    Module getModule();
    CompatibilityInfo getCompatibilityInfo();
    void setCompatibilityInfo(CompatibilityInfo compatibilityInfo);
}
