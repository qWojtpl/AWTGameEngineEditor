package pl.AWTGameEngineEditor.preview.handlers;

import com.intellij.util.ui.FormBuilder;
import pl.AWTGameEngine.components.Model3D;
import pl.AWTGameEngine.components.base.Base3DShape;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngineEditor.preview.Preview;

public class ModelHandler implements PreviewHandler {

    @Override
    public void create(FormBuilder builder, Preview preview) {
        preview.createPreview(builder, "editorScenes/modelPreview.xml");
    }

    @Override
    public void update(Preview preview, ObjectComponent sceneComponent) {
        GameObject object = preview.getWindow().getCurrentScene().getGameObjectByName("model");

        Base3DShape m = (Base3DShape) sceneComponent;

        object.setQuaternionRotation(m.getObject().getQuaternionRotation());
        object.setSize(m.getObject().getSize());

        Base3DShape model = (Base3DShape) object.getComponentByClass(Model3D.class);

        model.setSprite(m.getSprite());
        model.setColor(m.getColor());
        model.setShader(m.getShader());
        model.setRepeatTexture(m.getRepeatTexture());
    }

}
