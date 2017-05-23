import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.VerticalBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GenerateDialog extends DialogWrapper {

    private final CollectionListModel<PsiField> fieldsCollection;
    private final LabeledComponent<JPanel> fieldsComponent;
    private final boolean showCheckbox;

    protected GenerateDialog(final PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Select Fields for toMap Generation");

        fieldsCollection = new CollectionListModel<>();
        final JBList fieldList = new JBList(fieldsCollection);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList)
                .disableAddAction()
                .disableUpDownActions();
        final JPanel panel = decorator.createPanel();

        fieldsComponent = LabeledComponent.create(panel, "Fields to include in toMap");

        showCheckbox = psiClass.getFields().length != psiClass.getAllFields().length;

        updateFieldsDisplay(psiClass);
        init();
    }



    private void updateFieldsDisplay(PsiClass psiClass) {
        final List<PsiField> fields;
        fields = getClassFields(psiClass.getFields());
        fieldsCollection.removeAll();
        fieldsCollection.add(fields);
    }


    private List<PsiField> getClassFields(PsiField[] allFields) {
        final List<PsiField> fields = new ArrayList<>();
        for (PsiField field : allFields) {
            if (!field.hasModifierProperty(PsiModifier.STATIC) && !field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                fields.add(field);
            }
        }
        return fields;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return fieldsComponent;
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        JComponent southPanel = super.createSouthPanel();

        if(showCheckbox && southPanel != null) {
            final VerticalBox combinedView = new VerticalBox();
            combinedView.add(southPanel);
            return combinedView;
        } else {
            return southPanel;
        }
    }

    public List<PsiField> getSelectedFields() {
        return fieldsCollection.getItems();
    }
}