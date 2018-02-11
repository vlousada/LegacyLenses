package com.vlousada.legacylenses;

public class ParsedDataSet {

    private String parentTag = null;
    private String name = null;
    private String mount = null;
    private String focal = null;
    private String apertures = null;
    private String math = null;
    private String description = null;


    public ParsedDataSet() {
    }

    // parent tag
    public String getParentTag() {
        return parentTag;
    }

    public void setParentTag(String parentTag) {
        this.parentTag = parentTag;
    }

    // name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // mount
    public String getMount() {
        return mount;
    }

    public void setMount(String mount) {
        this.mount = mount;
    }

    // focals
    public String getFocal() {
        return focal;
    }

    public void setFocal(String focal) {
        this.focal = focal;
    }

    // apertures
    public String getApertures() {
        return apertures;
    }

    public void setApertures(String apertures) {
        this.apertures = apertures;
    }

    // math
    public String getMath() {
        return math;
    }

    public void setMath(String math) {
        this.math = math;
    }

    // description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}