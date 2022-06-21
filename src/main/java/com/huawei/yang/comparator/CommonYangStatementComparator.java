package com.huawei.yang.comparator;

import com.huawei.yang.base.Cardinality;
import com.huawei.yang.base.YangElement;
import com.huawei.yang.base.YangStatementDef;
import com.huawei.yang.model.api.stmt.IdentifierRef;
import com.huawei.yang.model.api.stmt.SchemaNode;
import com.huawei.yang.model.api.stmt.VirtualSchemaNode;
import com.huawei.yang.model.api.stmt.YangBuiltinStatement;
import com.huawei.yang.model.api.stmt.YangStatement;
import com.huawei.yang.model.api.stmt.YangUnknown;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-10
 */
public class CommonYangStatementComparator<T extends YangStatement> extends AbstractYangStatementComparator<T>{

    @Override
    protected List<CompatibilityRule.ChangeInfo> getChangeInfo(T left, T right) {
        List<CompatibilityRule.ChangeInfo> changeInfos = new ArrayList<>();
        if(left == null){
            changeInfos.add(CompatibilityRule.ChangeInfo.ADDED);
            return changeInfos;
        }

        if(right == null){
            changeInfos.add(CompatibilityRule.ChangeInfo.DELETED);
            return changeInfos;
        }
        if(!yangStatementIsEqual(left,right)){
            changeInfos.add(CompatibilityRule.ChangeInfo.CHANGED);
        }

        int leftIndex = getIndex(left);
        int rightIndex = getIndex(right);
        if(leftIndex != rightIndex){
            changeInfos.add(CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED);
        }
        return changeInfos;
    }
    private int getIndex(T t){
        YangStatement parentStmt = t.getParentStatement();
        if(null == parentStmt){
            return -1;
        }
        int index = -1;
        for(int i =0; i< parentStmt.getSubElements().size();i++){
            YangElement subElement = parentStmt.getSubElements().get(i);
            if(subElement instanceof YangStatement){
                index++;
                if(subElement == t){
                    return index;
                }
            }
        }
        return index;
    }
    protected ChangeType getChangeType(CompatibilityRule.ChangeInfo changeInfo){
        switch (changeInfo){
            case ADDED:
            case MANDATORY_ADDED:{
                return ChangeType.ADD;
            }
            case DELETED:{
                return ChangeType.DELETE;
            }
            default:{
                return ChangeType.MODIFY;
            }
        }
    }

    @Override
    protected CompatibilityInfo defaultCompatibility(T left, T right, CompatibilityRule.ChangeInfo changeInfo) {
        return new CompatibilityInfo(CompatibilityRule.Compatibility.BC,null);
    }

    public static String getStatement(YangStatement left, YangStatement right){
        YangStatement effective = left;
        if(effective == null){
            effective = right;
        }
        String statement = null;
        if(effective instanceof YangBuiltinStatement){
            statement = effective.getYangKeyword().getLocalName();
        } else {
            YangUnknown unknown = (YangUnknown) effective;
            String moduleName = unknown.getExtension().getContext().getCurModule().getMainModule().getArgStr();
            String extensionName = unknown.getExtension().getArgStr();
            statement = moduleName + ":" + extensionName;
        }
        return statement;
    }
    protected List<YangCompareResult> compareChildren(T left, T right){
        List<YangCompareResult> compareResults = compareStatements(left==null?new ArrayList<>():left.getEffectiveSubStatements(),
            right==null?new ArrayList<>():right.getEffectiveSubStatements(),true);
        return compareResults;
    }

    @Override
    public List<YangCompareResult> compare(T left, T right) {
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<CompatibilityRule.ChangeInfo> changeInfos = getChangeInfo(left,right);
        if(!changeInfos.isEmpty()){
            String statement = getStatement(left,right);
            for(CompatibilityRule.ChangeInfo changeInfo:changeInfos){
                if(changeInfo == CompatibilityRule.ChangeInfo.IGNORE){
                    continue;
                }
                CompatibilityRule compatibilityRule = null;
                if(null != getCompatibilityRules()){
                    compatibilityRule = getCompatibilityRules().searchRule(statement, CompatibilityRule.RuleType.STMT,changeInfo);
                }
                if(compatibilityRule == null){
                    //ignore sequence change
                    if(changeInfo != CompatibilityRule.ChangeInfo.SEQUENCE_CHANGED){
                        YangStatementCompareResult statementCompareResult = new YangStatementCompareResult(getChangeType(changeInfo),left,right);
                        statementCompareResult.setCompatibilityInfo(defaultCompatibility(left,right,changeInfo));
                        compareResults.add(statementCompareResult);
                    }

                }
                else {
                    YangStatementCompareResult statementCompareResult = new YangStatementCompareResult(getChangeType(changeInfo),left,right);
                    statementCompareResult.setCompatibilityInfo(new CompatibilityInfo(compatibilityRule.getCompatibility(),
                        compatibilityRule.getDescription()));
                    compareResults.add(statementCompareResult);
                }

            }
        }

        //sub statements
        if(left != null && right != null){
            compareResults.addAll(compareChildren(left,right));
        }
        return compareResults;
    }

    private static int calSimilarity(YangStatement src,YangStatement candidate){
        if(!src.getYangKeyword().equals(candidate.getYangKeyword())){
            return 0;
        }
        int similarity = 1;
        if(!src.equals(candidate)){
            return similarity;
        }
        similarity++;
        List<YangStatement> srcSubStatements = src.getEffectiveSubStatements();
        List<YangStatement> candidateSubStatements = candidate.getEffectiveSubStatements();
        if(srcSubStatements.size() != candidateSubStatements.size()){
            return similarity;
        }
        similarity++;
        List<YangStatement> matched = new ArrayList<>();
        for(YangStatement srcSubStatement:srcSubStatements){
            int maxSubSimilarity = 0;
            YangStatement maxsimliaritySubStatement = null;
            for(YangStatement candidateSubStatment:candidateSubStatements){
                if(contains(matched,candidateSubStatment)){
                    continue;
                }
                int subSimilarity = calSimilarity(srcSubStatement,candidateSubStatment);
                if(subSimilarity > maxSubSimilarity){
                    maxSubSimilarity = subSimilarity;
                    maxsimliaritySubStatement = candidateSubStatment;
                }
            }
            similarity+= maxSubSimilarity;
            matched.add(maxsimliaritySubStatement);
        }
        return similarity;

    }

    private static boolean contains(List list,Object o){
        if(list == null ){
            return false;
        }
        for(Object src:list){
            if(src == o){
                return true;
            }
        }
        return false;
    }

    private static YangStatement searchStatement(YangStatement statement,List<YangStatement> target,List<YangStatement> matched){
        if(null == target || target.size() == 0){
            return null;
        }
        List<YangStatement> matchedTargetStmts = new ArrayList<>();
        for(YangStatement rightSubStatement:target){
            if(contains(matched,rightSubStatement)){
                continue;
            }
            if(yangStatementIsEqual(statement,rightSubStatement)){
                matchedTargetStmts.add(rightSubStatement) ;
                continue;
            }
        }
        if(matchedTargetStmts.size() == 1){
            return matchedTargetStmts.get(0);
        } else if(matchedTargetStmts.size() >1){
            int maxSimilarity = 0;
            YangStatement maxSimilarStatement = null;
            for(YangStatement matchedTargetStmt:matchedTargetStmts){
                int similarity = calSimilarity(statement,matchedTargetStmt);
                if(similarity > maxSimilarity){
                    maxSimilarity = similarity;
                    maxSimilarStatement = matchedTargetStmt;
                }
            }
            return maxSimilarStatement;
        }

        if(statement.getParentStatement() == null){
            return null;
        }

        YangStatementDef statementDef = statement.getContext().getYangSpecification()
            .getStatementDef(statement.getParentStatement().getYangKeyword());
        if(statementDef == null){
            if(statement.getParentStatement() instanceof YangUnknown){
                //unknown will be treated as cardinality with non-unbounded
                for(YangStatement rightSubStatement:target){
                    if(contains(matched,rightSubStatement)){
                        continue;
                    }
                    return rightSubStatement;
                }
                return null;
            }
            else {
                return null;
            }
        }

        Cardinality cardinality = statementDef.getSubStatementCardinality(statement.getYangKeyword());
        if(cardinality == null){
            if(statement instanceof YangUnknown){
                //unknown will be treated as cardinality with non-unbounded
                for(YangStatement rightSubStatement:target){
                    if(contains(matched,rightSubStatement)){
                        continue;
                    }
                    return rightSubStatement;
                }
            }
            return null;
        }
        if(cardinality.isUnbounded()){
            return null;
        }

        for(YangStatement rightSubStatement:target){
            if(contains(matched,rightSubStatement)){
                continue;
            }
            return rightSubStatement;
        }
        return null;
    }

    private static boolean yangStatementIsEqual(YangStatement left,YangStatement right){
        if(left instanceof IdentifierRef && right instanceof IdentifierRef){
            if(!left.getYangKeyword().equals(right.getYangKeyword())){
                return false;
            }
            YangStatement leftRefStatement = ((IdentifierRef)left).getReferenceStatement();
            YangStatement rightRefStatement = ((IdentifierRef)right).getReferenceStatement();
            if(leftRefStatement != null && rightRefStatement != null && leftRefStatement.equals(rightRefStatement)){
                return true;
            } else if(leftRefStatement == null && rightRefStatement == null){
                if(left.getArgStr().equals(right.getArgStr())){
                    return true;
                }
                else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return left.equals(right);
    }

    public static List<YangCompareResult> compareStatements(List<? extends YangStatement> leftElements,
        List<? extends YangStatement> rightElements,boolean exceptSchemaNode){
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<YangStatement> foundStatements = new ArrayList<>();

        if(leftElements.size() > 0){
            for(YangStatement subElement:leftElements){
                YangStatement leftSubStatement = subElement;
                if(exceptSchemaNode){
                    if((leftSubStatement instanceof SchemaNode) && !(leftSubStatement instanceof VirtualSchemaNode)){
                        continue;
                    }
                }
                if(leftSubStatement instanceof SchemaNode){
                    SchemaNode leftSchemaNode = (SchemaNode) leftSubStatement;
                    if(!leftSchemaNode.isActive()){
                        continue;
                    }
                }

                List<YangStatement> rightSubStatements = new ArrayList<>();
                for(YangElement rightElement:rightElements){
                    if(!(rightElement instanceof YangStatement)){
                        continue;
                    }
                    if(leftSubStatement.getYangKeyword().equals(((YangStatement) rightElement).getYangKeyword())){
                        rightSubStatements.add((YangStatement) rightElement);
                    }
                }
                if(rightSubStatements.size()==0){
                    //no right statement, so change type is delete
                    YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(leftSubStatement,null));
                    compareResults.addAll(comparator.compare(leftSubStatement,null));
                    continue;
                }
                YangStatement matchedRightSubStatement = searchStatement(leftSubStatement,rightSubStatements,foundStatements);
                if(null == matchedRightSubStatement){
                    YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(leftSubStatement,null));
                    compareResults.addAll(comparator.compare(leftSubStatement,null));
                } else {
                    foundStatements.add(matchedRightSubStatement);
                    YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(leftSubStatement,matchedRightSubStatement));
                    compareResults.addAll(comparator.compare(leftSubStatement,matchedRightSubStatement));
                }
            }
        }
        if(rightElements.size() > 0){
            for(YangStatement subElement:rightElements){
                if(exceptSchemaNode){
                    if((subElement instanceof SchemaNode) && !(subElement instanceof VirtualSchemaNode)){
                        continue;
                    }
                }
                if(subElement instanceof SchemaNode){
                    SchemaNode rightSchemaNode = (SchemaNode) subElement;
                    if(!rightSchemaNode.isActive()){
                        continue;
                    }
                }
                YangStatement rightSubElement = subElement;
                if(contains(foundStatements,rightSubElement)){
                    continue;
                }
                YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(null,rightSubElement));
                compareResults.addAll(comparator.compare(null,rightSubElement));
            }
        }
        return compareResults;
    }

}
