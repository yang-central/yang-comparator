package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.Unique;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-20
 */
public class UniqueComparator extends CommonYangStatementComparator<Unique> {
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(Unique left, Unique right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if (left == null && right != null) {
            changeInfos.add(CompatibilityRule.ChangeInfo.ADDED);
        } else if (left != null && right == null) {
            changeInfos.add(CompatibilityRule.ChangeInfo.DELETED);
        } else {
            if (!left.equals(right)) {
                int leftSize = left.getUniqueNodes().size();
                int rightSize = right.getUniqueNodes().size();
                if (leftSize == rightSize) {
                    changeInfos.add(CompatibilityRule.ChangeInfo.CHANGED);
                } else if (leftSize < rightSize) {
                    boolean notfind = false;
                    for (int i = 0; i < leftSize; i++) {
                        Leaf leftUnique = left.getUniqueNodes().get(i);
                        List<YangStatement> rightUniques = new ArrayList<>();
                        for (Leaf rightUnique : right.getUniqueNodes()) {
                            rightUniques.add(rightUnique);
                        }
                        if (CommonYangStatementComparator.searchStatement(leftUnique, rightUniques, new ArrayList<>()) == null) {
                            notfind = true;
                        }
                    }
                    if (notfind) {
                        changeInfos.add(CompatibilityRule.ChangeInfo.CHANGED);
                    } else {
                        changeInfos.add(CompatibilityRule.ChangeInfo.REDUCE);
                    }
                } else {
                    boolean notfind = false;
                    for (int i = 0; i < rightSize; i++) {
                        Leaf rightUnique = right.getUniqueNodes().get(i);
                        List<YangStatement> leftUniques = new ArrayList<>();
                        for (Leaf leftUnique : left.getUniqueNodes()) {
                            leftUniques.add(leftUnique);
                        }
                        if (CommonYangStatementComparator.searchStatement(rightUnique, leftUniques, new ArrayList<>()) == null) {
                            notfind = true;
                        }
                    }
                    if (notfind) {
                        changeInfos.add(CompatibilityRule.ChangeInfo.CHANGED);
                    } else {
                        changeInfos.add(CompatibilityRule.ChangeInfo.EXPAND);
                    }
                }
            }
        }

        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(Unique left, Unique right,
                                                     CompatibilityRule.ChangeInfo changeInfo) {
        if (changeInfo == CompatibilityRule.ChangeInfo.ADDED
                || changeInfo == CompatibilityRule.ChangeInfo.REDUCE) {
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,
                    "add new unique or add a new node on unique is non-backward-compatible.");
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }
}
