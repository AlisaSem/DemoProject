package demo;

/** Тип веб-элемента на странице. */
public enum WebElementType {
  LABEL("Надпись");
  private final String typeName;

  WebElementType(String typeName) {
    this.typeName = typeName;
  }

  public String getTypeName() {
    return typeName;
  }
}
