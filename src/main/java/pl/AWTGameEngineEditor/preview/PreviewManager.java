package pl.AWTGameEngineEditor.preview;

import com.intellij.util.ui.FormBuilder;
import pl.AWTGameEngine.components.*;
import pl.AWTGameEngine.components.base.Base3DShape;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngineEditor.preview.handlers.ModelHandler;
import pl.AWTGameEngineEditor.preview.handlers.ParticleHandler;
import pl.AWTGameEngineEditor.preview.handlers.PreviewHandler;

import java.util.HashMap;

public class PreviewManager {

    private static final HashMap<Class<? extends ObjectComponent>, PreviewHandler> handlers = new HashMap<>();

    static {
        addTypeHandler(ParticleEmitter.class, new ParticleHandler());
        //
        ModelHandler modelHandler = new ModelHandler();
        addTypeHandler(Model3D.class, modelHandler);
        addTypeHandler(Box3D.class, modelHandler);
        addTypeHandler(Plane3D.class, modelHandler);
        addTypeHandler(TextRenderer3D.class, modelHandler);
    }

    public static void create(FormBuilder builder, Preview preview, ObjectComponent component) {
        if(component == null) {
            return;
        }
        PreviewHandler handler = handlers.getOrDefault(component.getClass(), null);
        if(handler == null) {
            return;
        }
        handler.create(builder, preview);
        handler.update(preview, component);
    }

    public static void update(Preview preview, ObjectComponent component) {
        if(component == null) {
            return;
        }
        PreviewHandler handler = handlers.getOrDefault(component.getClass(), null);
        if(handler == null) {
            return;
        }
        handler.update(preview, component);
    }

    public static void addTypeHandler(Class<? extends ObjectComponent> type, PreviewHandler handler) {
        handlers.put(type, handler);
    }

    public static ObjectComponent standardObjectPreview(GameObject object) {
        for(ObjectComponent component : object.getComponents()) {
            if(component instanceof Base3DShape) {
                return component;
            }
        }
        return null;
    }

}
