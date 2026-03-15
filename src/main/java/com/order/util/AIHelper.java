package com.order.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * AI辅助工具 — 调用硅基流动 DeepSeek-V3 API
 */
public class AIHelper {

    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final String API_KEY  = "sk-bkforrkuugpzcnporgvyolphawfhhtgrwaulsdyhdespgpnl";
    private static final String MODEL    = "deepseek-ai/DeepSeek-V3";

    /** 单次对话 */
    public static String chat(String systemPrompt, String userMessage) throws Exception {
        String body = buildJson(systemPrompt, userMessage);
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        if (code != 200) throw new Exception("API错误 " + code + ": " + sb);
        return extractContent(sb.toString());
    }

    /**
     * 解析自然语言订单描述，支持批量（多行/多句）
     * 返回每个订单的 Map: {customer, items: [{goods, qty},...]}
     */
    public static List<Map<String, Object>> parseBatchOrders(String text) throws Exception {
        String sys = "你是一个订单解析助手。用户会输入一段或多段自然语言订单描述，可能包含多个客户的订单。\n" +
            "请解析出每个独立订单，返回JSON数组，每个元素格式：\n" +
            "{\"customer\":\"客户名\",\"items\":[{\"goods\":\"货物名\",\"qty\":数量}]}\n" +
            "规则：\n" +
            "1. 如果一段文字只有一个客户，输出一个对象\n" +
            "2. 如果包含多个客户，每个客户一个对象\n" +
            "3. qty必须是整数，默认1\n" +
            "4. 只输出JSON数组，不要任何其他文字、markdown或代码块标记";

        String raw = chat(sys, text);
        raw = raw.trim();
        if (raw.startsWith("```")) raw = raw.replaceAll("```[a-z]*\\n?", "").replaceAll("```", "").trim();
        return parseOrderArray(raw);
    }

    /** 销售分析 */
    public static String analyzeSales(String dataStr) throws Exception {
        String sys = "你是专业销售分析师，请根据数据生成中文分析报告，结构如下：\n" +
            "1.【销售健康评分】格式：评分：XX/100（整数）评级：优秀/良好/一般/差\n" +
            "2.【客户集中度】客户贡献度分析（2-3句）\n" +
            "3.【热销货物】最畅销的产品\n" +
            "4.【注意货物】需要关注的产品\n" +
            "5.【销售建议】3条具体建议（用-分隔）\n" +
            "6.【潜在增长机会】2-3条（用-分隔）\n" +
            "语言专业直接，总字数350字以内。";
        return chat(sys, dataStr);
    }

    private static List<Map<String, Object>> parseOrderArray(String json) {
        List<Map<String, Object>> result = new ArrayList<>();
        // Find array content
        int arrStart = json.indexOf('[');
        int arrEnd = json.lastIndexOf(']');
        if (arrStart < 0 || arrEnd < 0) {
            // Single object?
            if (json.contains("\"customer\"")) {
                Map<String, Object> m = parseSingleOrder(json);
                if (m != null) result.add(m);
            }
            return result;
        }
        String arr = json.substring(arrStart + 1, arrEnd).trim();
        // Split objects
        List<String> objects = splitJsonObjects(arr);
        for (String obj : objects) {
            Map<String, Object> m = parseSingleOrder(obj.trim());
            if (m != null) result.add(m);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseSingleOrder(String obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        String customer = extractStr(obj, "customer");
        if (customer == null || customer.isEmpty()) return null;
        map.put("customer", customer);

        List<Map<String, Object>> items = new ArrayList<>();
        int itemsStart = obj.indexOf("\"items\"");
        if (itemsStart >= 0) {
            int arrS = obj.indexOf('[', itemsStart);
            int arrE = obj.lastIndexOf(']');
            if (arrS >= 0 && arrE > arrS) {
                String itemsArr = obj.substring(arrS + 1, arrE);
                for (String itemObj : splitJsonObjects(itemsArr)) {
                    String goods = extractStr(itemObj, "goods");
                    int qty = extractInt(itemObj, "qty");
                    if (goods != null && !goods.isEmpty()) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("goods", goods);
                        item.put("qty", qty > 0 ? qty : 1);
                        items.add(item);
                    }
                }
            }
        }
        map.put("items", items);
        return map;
    }

    private static List<String> splitJsonObjects(String s) {
        List<String> result = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0 && start >= 0) { result.add(s.substring(start, i + 1)); start = -1; } }
        }
        return result;
    }

    private static String extractStr(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    private static int extractInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 1;
        int colon = json.indexOf(':', idx + pattern.length());
        if (colon < 0) return 1;
        StringBuilder sb = new StringBuilder();
        for (int i = colon + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c)) sb.append(c);
            else if (sb.length() > 0) break;
        }
        try { return sb.length() > 0 ? Integer.parseInt(sb.toString()) : 1; }
        catch (NumberFormatException e) { return 1; }
    }

    private static String buildJson(String system, String user) {
        return "{\"model\":\"" + MODEL + "\",\"max_tokens\":1000,\"messages\":[" +
            "{\"role\":\"system\",\"content\":\"" + escJson(system) + "\"}," +
            "{\"role\":\"user\",\"content\":\"" + escJson(user) + "\"}]}";
    }

    private static String extractContent(String json) throws Exception {
        String marker = "\"content\":\"";
        int idx = json.lastIndexOf(marker);
        if (idx < 0) throw new Exception("解析AI响应失败");
        int start = idx + marker.length();
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                if (c == 'n') sb.append('\n');
                else if (c == 't') sb.append('\t');
                else sb.append(c);
                escape = false;
            } else if (c == '\\') { escape = true; }
            else if (c == '"') break;
            else sb.append(c);
        }
        return sb.toString();
    }

    private static String escJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
    }
}
