package com.huawei.yang.comparator;

import com.huawei.yang.model.api.stmt.SchemaNode;
import com.huawei.yang.model.api.stmt.SchemaNodeContainer;
import com.huawei.yang.model.api.stmt.VirtualSchemaNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-13
 */
public class SchemaNodeComparator extends CommonYangStatementComparator<SchemaNode> {
    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(SchemaNode left, SchemaNode right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if(left == null && right != null){
            if(right.isMandatory()){
                changeInfos.add(CompatibilityRule.ChangeInfo.MANDATORY_ADDED);
            }
            else {
                changeInfos.add(CompatibilityRule.ChangeInfo.ADDED);
            }
        } else if(left != null && right == null){
            changeInfos.add(CompatibilityRule.ChangeInfo.DELETED);
        } else if(left == null && right == null){

        } else {
            changeInfos.addAll(super.getChangeInfo(left, right));
        }
        return changeInfos;
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(SchemaNode left, SchemaNode right,
        CompatibilityRule.ChangeInfo changeInfo) {
        if(changeInfo == CompatibilityRule.ChangeInfo.MANDATORY_ADDED
        || changeInfo == CompatibilityRule.ChangeInfo.DELETED){
            return new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,null);
        }
        return super.defaultCompatibility(left, right, changeInfo);
    }

    @Override
    protected List<YangCompareResult> compareChildren(SchemaNode left, SchemaNode right) {
        List<YangCompareResult> results = new ArrayList<>();
        results.addAll(super.compareChildren(left, right));

        SchemaNode temp = left;
        if(temp == null){
            temp = right;
        }
        if((temp instanceof SchemaNodeContainer) && !(temp instanceof VirtualSchemaNode)){
            SchemaNodeContainer leftContainer = (SchemaNodeContainer) left;
            SchemaNodeContainer rightContainer = (SchemaNodeContainer) right;
            results.addAll(compareStatements(leftContainer==null?new ArrayList<>():YangComparator.getEffectiveSchemaNodeChildren(leftContainer),
                rightContainer==null?new ArrayList<>():YangComparator.getEffectiveSchemaNodeChildren(rightContainer),false));
        }
        return results;
    }

    @Override
    public List<YangCompareResult> compare(SchemaNode left, SchemaNode right) {
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<CompatibilityRule.ChangeInfo> changeInfos = getChangeInfo(left,right);
        List<YangTreeCompareResult> treeCompareResults = new ArrayList<>();
        SchemaNode temp = right;
        if(temp == null){
            temp = left;
        }
        String statement = getStatement(left,right);
        if(!changeInfos.isEmpty()){
            for(CompatibilityRule.ChangeInfo changeInfo:changeInfos){

                CompatibilityRule compatibilityRule = null;
                if(getCompatibilityRules() != null){
                    compatibilityRule = getCompatibilityRules().searchRule(statement, CompatibilityRule.RuleType.TREE,changeInfo);
                }
                if(compatibilityRule == null){
                    if(changeInfo == CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED){
                        continue;
                    }
                    YangTreeCompareResult treeCompareResult = new YangTreeCompareResult(temp.getSchemaPath(),getChangeType(changeInfo));
                    treeCompareResult.setLeft(left);
                    treeCompareResult.setRight(right);
                    treeCompareResult.setCompatibilityInfo(defaultCompatibility(left,right,changeInfo));
                    treeCompareResults.add(treeCompareResult);
                }
                else {
                    YangTreeCompareResult treeCompareResult = new YangTreeCompareResult(temp.getSchemaPath(),getChangeType(changeInfo));
                    treeCompareResult.setLeft(left);
                    treeCompareResult.setRight(right);
                    treeCompareResult.setCompatibilityInfo(new CompatibilityInfo(compatibilityRule.getCompatibility(),compatibilityRule.getDescription()));
                    treeCompareResults.add(treeCompareResult);
                }
            }
            compareResults.addAll(treeCompareResults);
        }

        //sub statements
        if(left != null && right != null){
            List<YangCompareResult> childrenResults = compareChildren(left,right);
            for(YangCompareResult childResult:childrenResults){
                if(childResult instanceof YangStatementCompareResult){
                    if(treeCompareResults.isEmpty()){
                        YangTreeCompareResult treeCompareResult = new YangTreeCompareResult(temp.getSchemaPath(),ChangeType.MODIFY);
                        treeCompareResult.setLeft(left);
                        treeCompareResult.setRight(right);
                        treeCompareResult.setCompatibilityInfo(new CompatibilityInfo(CompatibilityRule.Compatibility.BC,null));
                        treeCompareResults.add(treeCompareResult);
                        compareResults.add(treeCompareResult);
                    }
                    for(YangTreeCompareResult treeCompareResult:treeCompareResults){
                        treeCompareResult.addMetaCompareResult(childResult);
                        if(childResult.getCompatibilityInfo().getCompatibility()== CompatibilityRule.Compatibility.NBC){
                            treeCompareResult.setCompatibilityInfo(new CompatibilityInfo(CompatibilityRule.Compatibility.NBC,null));
                        }else if(childResult.getCompatibilityInfo().getCompatibility() == CompatibilityRule.Compatibility.UNKNOWN){
                            if(treeCompareResult.getCompatibilityInfo().getCompatibility() == CompatibilityRule.Compatibility.BC){
                                treeCompareResult.setCompatibilityInfo(new CompatibilityInfo(CompatibilityRule.Compatibility.UNKNOWN,null));
                            }
                        }
                    }
                } else {
                    compareResults.add(childResult);
                }
            }
        }
        return compareResults;
    }
}
