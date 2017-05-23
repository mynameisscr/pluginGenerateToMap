import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiClassReferenceType;

import java.util.List;

public class CodeGenerator {

    private static final String[] BOOLEAN = new String[] {"boolean", "Boolean"};
    private static final String[] NUMERIC = new String[] {"int", "byte", "short", "long", "double", "float", "char",
            "Integer", "Byte", "Short", "Long", "Double", "Float", "Character"};
    private static final String[] STRING = new String[] {"java.lang.String"};

    private final PsiClass mClass;
    private final List<PsiField> mFields;

    public CodeGenerator(PsiClass psiClass, List<PsiField> fields) {
        mClass = psiClass;
        mFields = fields;
    }

    public void generate() {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(mClass.getProject());

        PsiMethod toMap = elementFactory.createMethodFromText(generateToMap(mFields), mClass);

        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(mClass.getProject());

        PsiElement after;
        if (mClass.getMethods().length > 0) {
            after = mClass.getMethods()[mClass.getMethods().length - 1];
        } else {
            after = mClass.getFields()[mClass.getFields().length - 1];
        }
        styleManager.shortenClassReferences(mClass.addAfter(toMap, after));
    }

    private String generateToMap(List<PsiField> fields) {
        StringBuilder sb = new StringBuilder("public java.util.Map<String, String> toMap() {");
        sb.append("java.util.HashMap<String, String> map = new java.util.HashMap<>();");

        for (PsiField field : fields) {
            sb.append(getLineToAdd(field));
        }

        sb.append("return map;");
        sb.append("}");

        return sb.toString();
    }

    private String getLineToAdd(PsiField field) {
        switch(getType(field)) {
            case BOOLEAN:
                return "map.put(\"" + field.getName() + "\", String.valueOf(" + field.getName() + "));\n";
            case NUMERIC:
                return "if (" + field.getName() + " != 0) {\n" +
                        "map.put(\"" + field.getName() + "\", String.valueOf(" + field.getName() + "));\n" +
                        "}\n";
            case STRING:
                return "if (" + field.getName() + " != null) {\n" +
                        "map.put(\"" + field.getName() + "\", " + field.getName() + ");\n" +
                        "}\n";
            case OBJECT:
                return "if (" + field.getName() + " != null) {\n" +
                        "map.put(\"" + field.getName() + "\", String.valueOf(" + field.getName() + "));\n" +
                        "}\n";
            case ARRAY:
                return "if (" + field.getName() + " != null) {\n" +
                        "map.put(\"" + field.getName() + "\", java.util.Arrays.toString(" + field.getName() + "));\n" +
                        "}\n";
            case ENUM:
                return "if (" + field.getName() + " != null) {\n" +
                        "map.put(\"" + field.getName() + "\", " + field.getName() + ".name());\n" +
                        "}\n";
        }

        return "";
    }

    private Type getType(PsiField field) {
        if (field.getType().getClass() == PsiPrimitiveType.class) {
            for(String str: BOOLEAN) {
                if (field.getType().equalsToText(str))
                    return Type.BOOLEAN;
            }
            for(String str: NUMERIC) {
                if (field.getType().equalsToText(str))
                    return Type.NUMERIC;
            }
        } else if (field.getType().getClass() == PsiArrayType.class) {
            return Type.ARRAY;
        } else {
            for(String str: STRING) {
                if (field.getType().equalsToText(str))
                    return Type.STRING;
            }
            if (((PsiClassReferenceType) field.getType()).resolve().isEnum())
                return Type.ENUM;
        }

        return Type.OBJECT;
    }

    enum Type {
        BOOLEAN, NUMERIC, STRING, OBJECT, ENUM, ARRAY
    }
}
