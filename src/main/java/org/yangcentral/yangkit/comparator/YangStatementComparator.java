package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-10
 */
public interface YangStatementComparator<T extends YangStatement> {
    List<YangCompareResult> compare(T left, T right);
}
