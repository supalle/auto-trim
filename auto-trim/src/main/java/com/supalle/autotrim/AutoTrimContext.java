package com.supalle.autotrim;

public class AutoTrimContext {
    private ImportMetadata importMetadata;
    private VarStack varStack;
    private ClassMetadata classMetadata;

    public VarStack getVarStack() {
        return varStack;
    }

    public void setVarStack(VarStack varStack) {
        this.varStack = varStack;
    }

    public ClassMetadata getClassMetadata() {
        return classMetadata;
    }

    public void setClassMetadata(ClassMetadata classMetadata) {
        this.classMetadata = classMetadata;
    }

    public VarStack newVarStack() {
        return this.varStack = new VarStack(this.varStack);
    }

    public VarStack popVarStack() {
        return this.varStack = this.varStack.getParent();
    }

    public ClassMetadata newClassMetadata(ClassMetadata parent, String name) {
        return this.classMetadata = new ClassMetadata(parent, name);
    }

    public ClassMetadata popClassMetadata() {
        return this.classMetadata = this.classMetadata.getParent();
    }

    public void addStackVar(VarStack.StackVar stackVar) {
        this.varStack.addStackVar(stackVar);
    }

    public ImportMetadata getImportMetadata() {
        return importMetadata;
    }

    public void setImportMetadata(ImportMetadata importMetadata) {
        this.importMetadata = importMetadata;
    }

}
