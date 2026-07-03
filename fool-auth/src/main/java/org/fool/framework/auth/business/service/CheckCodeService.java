package org.fool.framework.auth.business.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.fool.framework.auth.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class CheckCodeService {
    private static final String PREFIX = "CHECK_CODE:";
    private static final char[] CHARACTERS = "2345689ABCDEFGHJKLMNPRSTWXY".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private RedisUtils redisUtils;

    public CheckCodeResult create() {
        String code = code();
        String key = UUID.randomUUID().toString();
        redisUtils.set(PREFIX + key, code, 60_000L);
        return new CheckCodeResult(key, code, image(code));
    }

    public boolean validate(CheckCodeRequest request) {
        if (request == null || blank(request.getKey()) || blank(request.getCode())) {
            return false;
        }
        String stored = redisUtils.get(PREFIX + request.getKey());
        return stored != null && stored.trim().equalsIgnoreCase(request.getCode().trim());
    }

    private static String code() {
        StringBuilder result = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            result.append(CHARACTERS[RANDOM.nextInt(CHARACTERS.length)]);
        }
        return result.toString();
    }

    private static String image(String code) {
        BufferedImage image = new BufferedImage(100, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(Color.BLACK);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            graphics.drawString(code, 18, 27);
        } finally {
            graphics.dispose();
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "jpg", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to render check code image", e);
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckCodeResult {
        private String key;
        private String code;
        private String chkCodeImg;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckCodeRequest {
        @JsonAlias("Key")
        private String key;
        @JsonAlias("Code")
        private String code;
        @JsonAlias("ChkCodeImg")
        private String chkCodeImg;
    }
}
