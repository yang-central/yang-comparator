package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-05-20
 */
public class YangStatementCompareResult implements YangCompareResult<YangStatement>{
    private ChangeType changeType;
    private YangStatement left;
    private YangStatement right;
    private CompatibilityInfo compatibilityInfo;

    public YangStatementCompareResult(ChangeType changeType, YangStatement left, YangStatement right) {
        this.changeType = changeType;
        this.left = left;
        this.right = right;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public YangStatement getLeft() {
        return left;
    }

    public YangStatement getRight() {
        return right;
    }


    @Override
    public String getChangeDescription(String indent) {
        StringBuffer sb = new StringBuffer();
        if(null != indent){
            sb.append(indent);
        }

        switch (changeType){
            case ADD:{
                sb.append(YangComparator.outputStatement(right));
                sb.append("\t@").append("module:").append(right.getContext().getCurModule().getArgStr()).append(" ")
                    .append(right.getElementPosition().getLocation().getLocation());
                break;
            }
            case MODIFY:{
                sb.append("FROM ");
                sb.append(YangComparator.outputStatement(left));
                sb.append(" TO ");
                sb.append(YangComparator.outputStatement(right));
                sb.append("\t@").append("module:").append(right.getContext().getCurModule().getArgStr()).append(" ")
                    .append(right.getElementPosition().getLocation().getLocation());
                break;
            }
            case DELETE:{
                sb.append(YangComparator.outputStatement(left));
                sb.append("\t@").append("module:").append(left.getContext().getCurModule().getArgStr()).append(" ")
                    .append(left.getElementPosition().getLocation().getLocation());
                break;
            }
        }
        return sb.toString();
    }
    @Override
    public Module getModule() {
        switch (changeType){
            case ADD:
            case MODIFY:{
                return right.getContext().getCurModule();
            }
            case DELETE:{
                return left.getContext().getCurModule();
            }
        }
        return null;
    }

    @Override
    public CompatibilityInfo getCompatibilityInfo() {
        return compatibilityInfo;
    }

    @Override
    public void setCompatibilityInfo(CompatibilityInfo compatibility) {
        this.compatibilityInfo = compatibility;
    }
}
