package top.scraft.rotationcaptcha;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class Rest {

    private static final File IMG_DIR = new File("testimg");
    private static final String SESSION_KEY = "ROTATION_CAPTCHA_DEGREE";
    private final Random random = new Random(System.currentTimeMillis());

    @GetMapping("/captcha")
    public ResponseEntity<byte[]> captcha(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        File[] imgs = IMG_DIR.listFiles();
        if (imgs == null) {
            return ResponseEntity.internalServerError().build();
        }

        float rotateDeg = random.nextFloat() * 300.0F + 30.0F;
        double rotateRad = rotateDeg * Math.PI / 180.0;
        session.setAttribute(SESSION_KEY, rotateDeg);

        try {
            BufferedImage original = ImageIO.read(imgs[random.nextInt(imgs.length)]);
            int size = Math.min(original.getHeight(), original.getWidth());

            BufferedImage processed = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = processed.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setClip(new Ellipse2D.Double(0, 0, size, size));
            g.rotate(-rotateRad, size / 2.0, size / 2.0);
            g.drawImage(original, 0, 0, size, size, null);
            g.dispose();

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            ImageIO.write(processed, "JPEG", result);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(result.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/captcha")
    public ResponseEntity<String> verify(HttpServletRequest request,
                                         @RequestParam float value) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.badRequest().body("no session");
        }
        float rotateDeg = (float) session.getAttribute(SESSION_KEY);
        float offset = Math.abs(rotateDeg - value);
        if (offset >= 360.0F) {
            offset -= 360.0F;
        }
        if (offset < 25.0F) {
            return ResponseEntity.ok("验证成功");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("验证失败");
        }
    }

}
