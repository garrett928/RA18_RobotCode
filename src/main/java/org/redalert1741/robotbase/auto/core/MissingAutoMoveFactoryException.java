package org.redalert1741.robotbase.auto.core;

public class MissingAutoMoveFactoryException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public enum FactoryType {
        MOVE, END
    }
    
    public FactoryType factory;
    public String type;
    
    public MissingAutoMoveFactoryException(FactoryType factory, String type) {
        super("Missing " + factory.toString() + " factory for \"" + type + "\"");
        this.factory = factory;
        this.type = type;
    }
}
