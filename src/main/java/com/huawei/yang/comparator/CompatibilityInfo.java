package com.huawei.yang.comparator;

/**
 * 功能描述
 *
 * @author f00360218
 * @since 2022-06-17
 */
public class CompatibilityInfo {
    private CompatibilityRule.Compatibility compatibility;
    private String description;

    public CompatibilityInfo(CompatibilityRule.Compatibility compatibility, String description) {
        this.compatibility = compatibility;
        this.description = description;
    }

    public CompatibilityRule.Compatibility getCompatibility() {
        return compatibility;
    }

    public String getDescription() {
        return description;
    }
}
