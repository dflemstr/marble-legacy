package org.marble.engine;

import com.jme3.audio.AudioContext;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.JmeSystem;

import org.marble.entity.audible.Audible;

public class AudioEngine extends Engine<Audible> {

    private final AppSettings appSettings;
    private AudioRenderer audioRenderer;

    private Listener listener;

    public AudioEngine(final JmeContext context) {
        super(Audible.class);
        appSettings = context.getSettings();
    }

    @Override
    public void destroy() {
        audioRenderer.cleanup();
    }

    /**
     * @return the audioRenderer
     */
    public AudioRenderer getAudioRenderer() {
        return audioRenderer;
    }

    @Override
    public void initialize() {
        audioRenderer = JmeSystem.newAudioRenderer(appSettings);
        audioRenderer.initialize();

        listener = new Listener();
        audioRenderer.setListener(listener);

        AudioContext.setAudioRenderer(audioRenderer);
    }

    @Override
    public void update(final float timePerFrame) {
        AudioContext.setAudioRenderer(audioRenderer);
        audioRenderer.update(timePerFrame);
    }
}
