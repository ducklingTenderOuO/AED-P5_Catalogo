import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CargadorImagenes {
    private static final String CARPETA_IMAGENES = "imagenes";
    private static final String URL_BASE = "https://images.ygoprodeck.com/images/cards/";
    private static final Map<String, BufferedImage> cacheMemoria = new ConcurrentHashMap<>();
    private static final Set<String> enDescarga = ConcurrentHashMap.newKeySet();
    private static final ExecutorService poolDescarga = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    private static final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static BufferedImage imagenPlaceholder;

    public static void obtenerImagen(Carta carta, Consumer<BufferedImage> callback) {
        if (carta == null || carta.getId() == null || carta.getId().isEmpty()) {
            callback.accept(obtenerPlaceholder());
            return;
        }
        String id = carta.getId().trim();
        if (cacheMemoria.containsKey(id)) {
            callback.accept(cacheMemoria.get(id));
            return;
        }
        File directorio = new File(CARPETA_IMAGENES);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
        File archivoLocal = new File(directorio, id + ".jpg");
        if (archivoLocal.exists() && archivoLocal.length() > 0) {
            try {
                BufferedImage img = ImageIO.read(archivoLocal);
                if (img != null) {
                    cacheMemoria.put(id, img);
                    callback.accept(img);
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        callback.accept(obtenerPlaceholder());
        if (enDescarga.add(id)) {
            poolDescarga.submit(() -> descargarYGuardar(id, archivoLocal.toPath(), callback));
        }
    }

    private static void descargarYGuardar(String id, Path destino, Consumer<BufferedImage> callback) {
        try {
            String url = URL_BASE + id + ".jpg";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<InputStream> resp = clienteHttp.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() == 200) {
                Files.copy(resp.body(), destino, StandardCopyOption.REPLACE_EXISTING);
                BufferedImage img = ImageIO.read(destino.toFile());
                if (img != null) {
                    cacheMemoria.put(id, img);
                    SwingUtilities.invokeLater(() -> callback.accept(img));
                }
            }
        } catch (Exception ignored) {
        } finally {
            enDescarga.remove(id);
        }
    }

    public static BufferedImage obtenerPlaceholder() {
        if (imagenPlaceholder != null) {
            return imagenPlaceholder;
        }
        int w = 421;
        int h = 614;
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(43, 29, 18), w, h, new Color(78, 52, 34));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w, h, 24, 24);
        g2.setColor(new Color(212, 175, 55));
        g2.setStroke(new BasicStroke(8f));
        g2.drawRoundRect(10, 10, w - 20, h - 20, 18, 18);
        g2.setColor(new Color(139, 101, 8));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(85, 130, 251, 354);
        g2.setColor(new Color(212, 175, 55));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        String texto1 = "YU-GI-OH!";
        g2.drawString(texto1, (w - fm.stringWidth(texto1)) / 2, 295);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        fm = g2.getFontMetrics();
        String texto2 = "Loading card...";
        g2.drawString(texto2, (w - fm.stringWidth(texto2)) / 2, 330);
        g2.dispose();
        imagenPlaceholder = bi;
        return imagenPlaceholder;
    }

    public static ImageIcon escalarImagen(BufferedImage origen, int anchoObjetivo, int altoObjetivo) {
        if (origen == null) {
            origen = obtenerPlaceholder();
        }
        BufferedImage actual = origen;
        int w = origen.getWidth();
        int h = origen.getHeight();

        if (w <= 0 || h <= 0) {
            w = anchoObjetivo;
            h = altoObjetivo;
        }

        while (w / 2 >= anchoObjetivo && h / 2 >= altoObjetivo) {
            w = Math.max(anchoObjetivo, w / 2);
            h = Math.max(altoObjetivo, h / 2);
            BufferedImage paso = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = paso.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(actual, 0, 0, w, h, null);
            g2.dispose();
            actual = paso;
        }

        if (w != anchoObjetivo || h != altoObjetivo) {
            BufferedImage finalImg = new BufferedImage(anchoObjetivo, altoObjetivo, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = finalImg.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(actual, 0, 0, anchoObjetivo, altoObjetivo, null);
            g2.dispose();
            actual = finalImg;
        }

        return new ImageIcon(actual);
    }
}
