package tlb1.radix.database;

public enum FieldType {
    SMALL_TEXT("VARCHAR(25)"), TEXT("VARCHAR(50)"), BIG_TEXT("VARCHAR(250)"),
    IDENTIFIER("int"),
    LONG("BIGINT"),INTEGER("INT"), SHORT("SMALLINT"), BYTE("TINYINT"),
    FLOAT("FLOAT"), DOUBLE("DOUBLE"),
    BOOLEAN("BOOL"),
    DATE("DATE"),
    UUID("VARCHAR(36)");
    private final String typeName;

    FieldType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
