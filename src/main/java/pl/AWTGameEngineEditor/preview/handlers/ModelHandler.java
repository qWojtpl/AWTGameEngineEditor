package pl.AWTGameEngineEditor.preview.handlers;

import com.intellij.util.ui.FormBuilder;
import pl.AWTGameEngine.components.Model3D;
import pl.AWTGameEngine.components.base.Base3DShape;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.engine.Shaders;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngineEditor.preview.Preview;

public class ModelHandler implements PreviewHandler {

    @Override
    public void create(FormBuilder builder, Preview preview) {
        preview.createPreview(builder, "editorScenes/modelPreview.xml");
    }

    @Override
    public void update(Preview preview, ObjectComponent sceneComponent) {
        GameObject previewObject = preview.getWindow().getCurrentScene().getGameObjectByName("model");

        Base3DShape sceneModel = (Base3DShape) sceneComponent;

        previewObject.setQuaternionRotation(sceneModel.getObject().getQuaternionRotation());
        previewObject.setSize(sceneModel.getObject().getSize());

        Base3DShape previewModel = (Base3DShape) previewObject.getComponentByClass(Model3D.class);

        previewModel.setSprite(sceneModel.getSprite());
        previewModel.setColor(sceneModel.getColor());
        previewModel.setShader(Shaders.of(previewObject.getScene().getWindow(), sceneModel.getShader().getClass()));
        previewModel.setRepeatTexture(sceneModel.getRepeatTexture());
        previewModel.setShapePath(sceneModel.getShapePath());
    }

}
