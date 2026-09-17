package pl.AWTGameEngineEditor.preview.handlers;

import com.intellij.util.ui.FormBuilder;
import pl.AWTGameEngine.components.ParticleEmitter;
import pl.AWTGameEngine.components.base.ObjectComponent;
import pl.AWTGameEngine.objects.GameObject;
import pl.AWTGameEngineEditor.preview.Preview;

public class ParticleHandler implements PreviewHandler {

    @Override
    public void create(FormBuilder builder, Preview preview) {
        preview.createPreview(builder, "editorScenes/particlePreview.xml");
    }

    @Override
    public void update(Preview preview, ObjectComponent sceneComponent) {
        GameObject object = preview.getWindow().getCurrentScene().getGameObjectByName("emitter");
        ParticleEmitter emitter = (ParticleEmitter) object.getComponentByClass(ParticleEmitter.class);

        ParticleEmitter e = (ParticleEmitter) sceneComponent;

        emitter.setFadeOutStart(e.getFadeOutStart());
        emitter.setParticleSize(e.getParticleSize());
        emitter.setLooped(e.isLooped());
        emitter.getMaterial().setSprite(e.getMaterial().getSprite());
        emitter.setTtl(e.getTtl());
        emitter.setIterationsPerSecond(e.getIterationsPerSecond());
        emitter.setIterationStep(e.getIterationStep());
        emitter.setVectors(e.getVectors());
    }

}
