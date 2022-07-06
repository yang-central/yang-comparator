package com.huawei.yang.comparator;

import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-01
 */
public class YangTreeMetaCompareResult implements YangCompareResult<String> {
    private SchemaNode target;
    private String meta;
    private String left;
    private String right;
    private ChangeType changeType;
    private CompatibilityInfo compatibilityInfo;

    public YangTreeMetaCompareResult(SchemaNode target, String meta,ChangeType changeType,String left,
        String right) {
        this.target = target;
        this.left = left;
        this.right = right;
        this.changeType = changeType;
        this.meta = meta;
    }

    @Override
    public ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String getLeft() {
        return left;
    }

    @Override
    public String getRight() {
        return right;
    }

    public String getMeta() {
        return meta;
    }

    public SchemaNode getTarget() {
        return target;
    }

    @Override
    public String getChangeDescription(String indent) {
        StringBuffer sb = new StringBuffer();
        if(indent != null){
            sb.append(indent);
        }
        sb.append(changeType.getName()).append(":").append(meta).append(" ");
        switch (changeType){
            case ADD:{
                sb.append(right);
                break;
            }
            case DELETE:{
                sb.append(left);
                break;
            }
            case MODIFY:{
                sb.append("FROM:").append(left).append(" TO:")
                    .append(right);
                break;
            }
        }
        return sb.toString();
    }

    @Override
    public Module getModule() {
        return getTarget().getContext().getCurModule();
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
