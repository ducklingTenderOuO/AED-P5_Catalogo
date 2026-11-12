import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class ReproductorAudio {
    private Clip clip;
    private boolean reproduciendo;

    public ReproductorAudio(String rutaArchivo) {
        this.reproduciendo = false;
        inicializar(rutaArchivo);
    }

    private void inicializar(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists() && rutaArchivo.toLowerCase().endsWith(".mp3")) {
                archivo = new File(rutaArchivo.substring(0, rutaArchivo.length() - 4) + ".wav");
            } else if (archivo.exists() && rutaArchivo.toLowerCase().endsWith(".mp3")) {
                File wavAlternativo = new File(rutaArchivo.substring(0, rutaArchivo.length() - 4) + ".wav");
                if (wavAlternativo.exists()) {
                    archivo = wavAlternativo;
                }
            }
            if (!archivo.exists()) {
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(archivo);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(-4.0f);
            }
        } catch (Exception ignored) {
        }
    }

    public void reproducir() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
            reproduciendo = true;
        }
    }

    public void pausar() {
        if (clip != null) {
            clip.stop();
            reproduciendo = false;
        }
    }

    public boolean alternarAudio() {
        if (reproduciendo) {
            pausar();
        } else {
            reproducir();
        }
        return reproduciendo;
    }

    public boolean isReproduciendo() {
        return reproduciendo;
    }

    public void detener() {
        if (clip != null) {
            clip.stop();
            clip.close();
            reproduciendo = false;
        }
    }
}
