package org.yangcentral.yangkit.comparator;


/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-05-19
 */
public enum ChangeType {
    ADD("add"),
    MODIFY("modify"),
    DELETE("delete");
    private String description;
    private ChangeType(String description){
        this.description = description;
    }
    public String getName(){
        return this.description;
    }
}
