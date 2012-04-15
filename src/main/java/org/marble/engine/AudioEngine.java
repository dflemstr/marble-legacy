package org.marble.engine;

import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;

import org.marble.entity.audible.Audible;

public class AudioEngine extends Engine<Audible> {

    private final AudioRenderer audioRenderer;

    private final Listener listener;

    protected AudioEngine(final JmeContext context) {
        super(Audible.class);
        audioRenderer = JmeSystem.newAudioRenderer(context.getSettings());
        AudioContext.setAudioRenderer(audioRenderer);

        listener = new Listener();
        audioRenderer.setListener(listener);
    }

    @Override
    public void destroy() {
        audioRenderer.cleanup();
    }

    @Override
    public void initialize() {
        audioRenderer.initialize();
    }

    @Override
    public void update(final float timePerFrame) {
        AudioContext.setAudioRenderer(audioRenderer);
        audioRenderer.update(timePerFrame);
    }
}
