package org.yangcentral.yangkit.comparator;

import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-05-19
 */
public class YangTreeCompareResult implements YangCompareResult<SchemaNode>{
    private SchemaPath.Absolute schemaPath;
    private ChangeType changeType;
    private SchemaNode left;
    private SchemaNode right;
    private String changeDescription;
    private List<YangCompareResult> metaCompareResults = new ArrayList<>();
    private CompatibilityInfo compatibilityInfo;

    public YangTreeCompareResult(SchemaPath.Absolute schemaPath, ChangeType changeType) {
        this.schemaPath = schemaPath;
        this.changeType = changeType;
    }

    public List<YangCompareResult> getMetaCompareResults() {
        return metaCompareResults;
    }

    public void setMetaCompareResults(List<YangCompareResult> metaCompareResults) {
        this.metaCompareResults = metaCompareResults;
    }
    public void addMetaCompareResult(YangCompareResult metaResult){
        metaCompareResults.add(metaResult);
        return;
    }

    public SchemaNode getLeft() {
        return left;
    }

    public void setLeft(SchemaNode left) {
        this.left = left;
    }

    public SchemaNode getRight() {
        return right;
    }

    public void setRight(SchemaNode right) {
        this.right = right;
    }

    public SchemaPath.Absolute getSchemaPath() {
        return schemaPath;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeDescription(String changeDescription) {
        this.changeDescription = changeDescription;
    }

    @Override
    public String getChangeDescription(String indent) {
        StringBuffer sb = new StringBuffer();
        if(indent != null){
            sb.append(indent);
        }
        if(changeDescription != null){
            sb.append("\t").append(changeDescription);
            return sb.toString();
        }
        if(null != schemaPath){
            sb.append(schemaPath.toString());
        }
        sb.append("\t@").append("module:");
        switch (changeType){
            case ADD:
            case MODIFY:{
                sb.append(right.getContext().getCurModule().getArgStr()).append(" ")
                    .append(right.getElementPosition().getLocation().getLocation());
                break;
            }
            case DELETE:{
                sb.append(left.getContext().getCurModule().getArgStr()).append(" ")
                    .append(left.getElementPosition().getLocation().getLocation());
                break;
            }
        }

        if(metaCompareResults.size() > 0){
            for(YangCompareResult compareResult:metaCompareResults){
                sb.append("\n").append(indent).append("\t");
                switch (compareResult.getChangeType()){
                    case ADD:{
                        sb.append("added:");
                        break;
                    }
                    case DELETE:{
                        sb.append("deleted:");
                        break;
                    }
                    case MODIFY:{
                        sb.append("changed:");
                        break;
                    }
                }
                sb.append(" ");
                sb.append(compareResult.getChangeDescription(null));
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
