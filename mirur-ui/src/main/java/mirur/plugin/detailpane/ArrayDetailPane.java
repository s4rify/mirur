package mirur.plugin.detailpane;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.logging.Logger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class ArrayDetailPane implements IDetailPane {
    private static final Logger LOGGER = Logger.getLogger(ArrayDetailPane.class.getName());

    public static final String ID = "mirur.detailpanes.simplearray";
    public static final String NAME = "Simple Array Details";
    public static final String DESCRIPTION = "Only print the first several values of the array to avoid very long evaluation times.";

    private Control container;
    private SourceViewer sourceViewer;

    @Override
    public void init(IWorkbenchPartSite partSite) {
        // nop
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void dispose() {
        container.dispose();
    }

    @Override
    public Control createControl(Composite parent) {
        sourceViewer = new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL);
        sourceViewer.setDocument(new Document());
        sourceViewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        sourceViewer.getTextWidget()
                .setWordWrap(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IDebugPreferenceConstants.PREF_DETAIL_PANE_WORD_WRAP));
        sourceViewer.setEditable(false);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(sourceViewer.getTextWidget(), IDebugHelpContextIds.DETAIL_PANE);
        container = sourceViewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        container.setLayoutData(gd);
        return container;
    }

    @Override
    public boolean setFocus() {
        return sourceViewer.getTextWidget().setFocus();
    }

    @Override
    public void display(IStructuredSelection selection) {
        // other conditions are checked by the factory
        if (selection.isEmpty()) {
            sourceViewer.getDocument().set("");
            return;
        }

        int numElements = 10;
        StringBuilder sb = new StringBuilder();

        try {
            Object o = selection.getFirstElement();
            IJavaArray array = (IJavaArray) ((IJavaVariable) o).getValue();

            if (array.getValue(0) instanceof IJavaPrimitiveValue) {
                sb.append("[");
                for (int i = 0; i < array.getSize() && i < numElements; i++) {
                    IJavaValue value = array.getValue(i);
                    if (value instanceof IJavaPrimitiveValue) {
                        String str = ((IJavaPrimitiveValue) value).getValueString();
                        sb.append(str);
                        sb.append(", ");
                    }
                }

                if (array.getSize() <= numElements) {
                    sb.append("]");
                } else {
                    sb.append("... (and ");
                    sb.append(String.format("%,d", array.getSize() - 3));
                    sb.append(" more)");
                }
            } else {
                sb.append("(large array printing suppressed)");
            }
        } catch (DebugException ex) {
            logWarning(LOGGER, "Error fetching details", ex);
        }

        sourceViewer.getDocument().set(sb.toString());
    }
}