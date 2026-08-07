package pl.AWTGameEngineEditor.preview.handlers;

import com.intellij.util.ui.FormBuilder;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngineEditor.preview.Preview;

public interface PreviewHandler {

    void create(FormBuilder builder, Preview preview);
    void update(Preview preview, ObjectComponent sceneComponent);

}
