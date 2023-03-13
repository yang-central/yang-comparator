package com.huawei.yang.comparator;

import org.yangcentral.yangkit.base.Cardinality;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.base.YangStatementDef;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-10
 */
public class CommonYangStatementComparator<T extends YangStatement> extends AbstractYangStatementComparator<T>{
    public static final int OPTION_ONLY_META = 1;
    public static final int OPTION_ONLY_SCHEMA = 2;
    public static final int OPTION_ALL = 3;
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

    public static String getStatement(YangStatement stmt){
        if(stmt == null){
            return null;
        }
        String statement = null;
        if(stmt instanceof YangBuiltinStatement){
            statement = stmt.getYangKeyword().getLocalName();
        } else {
            YangUnknown unknown = (YangUnknown) stmt;
            if(unknown.getExtension() == null){
                return unknown.getKeyword();
            }
            String moduleName = unknown.getExtension().getContext().getCurModule().getMainModule().getArgStr();
            String extensionName = unknown.getExtension().getArgStr();
            statement = moduleName + ":" + extensionName;
        }
        return statement;
    }
    protected List<YangCompareResult> compareChildren(T left, T right){
        List<YangCompareResult> compareResults = compareStatements(left==null?new ArrayList<>():left.getEffectiveSubStatements(),
            right==null?new ArrayList<>():right.getEffectiveSubStatements(), OPTION_ONLY_META);
        return compareResults;
    }

    @Override
    public List<YangCompareResult> compare(T left, T right) {
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<CompatibilityRule.ChangeInfo> changeInfos = getChangeInfo(left,right);
        if(!changeInfos.isEmpty()){
            YangStatement effectiveStmt = left==null?right:left;
            String statement = getStatement(effectiveStmt);
            String parentStmt = getStatement(effectiveStmt.getParentStatement());
            for(CompatibilityRule.ChangeInfo changeInfo:changeInfos){
                if(changeInfo == CompatibilityRule.ChangeInfo.IGNORE){
                    continue;
                }
                CompatibilityRule compatibilityRule = null;
                if(null != getCompatibilityRules()){
                    compatibilityRule = getCompatibilityRules().searchRule(statement,parentStmt, changeInfo);
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

    public static int calSimilarity(YangStatement src,YangStatement candidate){
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

    public static boolean contains(List list,Object o){
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

    public static YangStatement searchStatement(YangStatement statement,List<YangStatement> target,List<YangStatement> matched){
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
                if(similarity <=1){
                    continue;
                }
                if(similarity > maxSimilarity){
                    maxSimilarity = similarity;
                    maxSimilarStatement = matchedTargetStmt;
                }
            }
            if(maxSimilarStatement != null){
                return maxSimilarStatement;
            }
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

    public static boolean yangStatementIsEqual(YangStatement left,YangStatement right){
        if((left instanceof SchemaNode) && (right instanceof SchemaNode)){
            SchemaNode leftSchemaNode = (SchemaNode) left;
            SchemaNode rightSchemaNode = (SchemaNode) right;
            if(!left.getContext().getNamespace().equals(right.getContext().getNamespace())){
                return false;
            }
            if(!yangStatementIsEqual((YangStatement) leftSchemaNode.getClosestAncestorNode(),
                    (YangStatement) rightSchemaNode.getClosestAncestorNode())){
                return false;
            }
        }
        if((left instanceof IdentifierRef) && (right instanceof IdentifierRef)){
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
        List<? extends YangStatement> rightElements,int option){
        List<YangCompareResult> compareResults = new ArrayList<>();
        List<YangStatement> foundStatements = new ArrayList<>();

        if(leftElements.size() > 0){
            for(YangStatement subElement:leftElements){
                YangStatement leftSubStatement = subElement;
                if(option == OPTION_ONLY_META){
                    if((leftSubStatement instanceof SchemaNode)
                            ||(leftSubStatement instanceof Referencable)){
                        continue;
                    }
                } else if (option == OPTION_ONLY_SCHEMA){
                    if(!((leftSubStatement instanceof SchemaNode)
                    )){
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
                for(YangStatement rightElement:rightElements){
                    if(leftSubStatement.getYangKeyword().equals(rightElement.getYangKeyword())){
                        rightSubStatements.add(rightElement);
                    }
                }
                if(rightSubStatements.size()==0){
                    //no right statement, so change type is delete
                    YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(leftSubStatement));
                    compareResults.addAll(comparator.compare(leftSubStatement,null));
                    continue;
                }
                YangStatement matchedRightSubStatement = searchStatement(leftSubStatement,rightSubStatements,foundStatements);
                if(null == matchedRightSubStatement){
                    YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(leftSubStatement));
                    compareResults.addAll(comparator.compare(leftSubStatement,null));
                } else {
                    foundStatements.add(matchedRightSubStatement);
                    YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(leftSubStatement==null?matchedRightSubStatement:leftSubStatement));
                    compareResults.addAll(comparator.compare(leftSubStatement,matchedRightSubStatement));
                }
            }
        }
        if(rightElements.size() > 0){
            for(YangStatement subElement:rightElements){
                if(option == OPTION_ONLY_META){
                    if((subElement instanceof SchemaNode)
                            ||(subElement instanceof Referencable)){
                        continue;
                    }
                } else if (option == OPTION_ONLY_SCHEMA){
                    if(!((subElement instanceof SchemaNode)
                    )){
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
                YangStatementComparator comparator = YangComparatorRegister.getInstance().getComparator(getStatement(rightSubElement));
                compareResults.addAll(comparator.compare(null,rightSubElement));
            }
        }
        return compareResults;
    }

}
