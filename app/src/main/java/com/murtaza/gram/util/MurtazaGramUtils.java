package com.murtaza.gram.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

/**
 * Utility helpers for MurtazaGram.
 */
public class MurtazaGramUtils {

    public static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    public static String formatNumber(long count) {
        if (count < 1000) return String.valueOf(count);
        if (count < 1_000_000) return String.format("%.1fK", count / 1000.0);
        if (count < 1_000_000_000) return String.format("%.1fM", count / 1_000_000.0);
        return String.format("%.1fB", count / 1_000_000_000.0);
    }

    public static String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, 1).toUpperCase();
    }

    public static int getAvatarColor(long id) {
        int[] colors = {
            0xFF7B2FBE, 0xFF9D5DE0, 0xFF5A1E8C, 0xFFB47CE0,
            0xFF6B3FA0, 0xFF8B5CB8, 0xFFA04DD8, 0xFF4A1B7A
        };
        return colors[(int) (Math.abs(id) % colors.length)];
    }

    public static boolean isValidBotToken(String token) {
        if (token == null || token.isEmpty()) return false;
        return token.matches("\\d{6,}:.{30,}");
    }
}
