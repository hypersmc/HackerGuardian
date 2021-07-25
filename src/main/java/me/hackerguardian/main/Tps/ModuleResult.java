package me.hackerguardian.main.Tps;

public class ModuleResult {
    private String name;
    private Boolean pf;
    private String desc;

    public ModuleResult(String ModuleName, Boolean passed, String description) {
        name = ModuleName;
        pf = passed;
        this.desc = description;
    }

    public String getDesc() {
        return this.desc;
    }

    public boolean passed() {
        return pf;
    }

    public String getModuleName() {
        return name;
    }
}
