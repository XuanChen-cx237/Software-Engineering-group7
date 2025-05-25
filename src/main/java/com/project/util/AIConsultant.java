package com.project.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * AI Consultant utility to communicate with DeepSeek API
 * 支持流式响应和常规响应
 */
public class AIConsultant {
    // DeepSeek API 配置
    private static final String API_ENDPOINT = "https://api.deepseek.com/v1/chat/completions";
    private static final String MODEL = "deepseek-coder";
    private static final String API_KEY = "sk-0139a7ea12f94732bb4e6227adc64a70";

    private final HttpClient client;

    /**
     * 构造函数
     */
    public AIConsultant() {
        // 创建HTTP客户端，设置120秒超时
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * 从DeepSeek API获取财务建议（静态方法，兼容原有代码）
     *
     * @param prompt 包含预算和支出数据的提示
     * @return AI 的响应
     * @throws Exception 如果与 API 通信出错
     */
    public static String getAdvice(String prompt) throws Exception {
        // 创建AIConsultant实例
        AIConsultant aiConsultant = new AIConsultant();
        return aiConsultant.processQuery(prompt);
    }

    /**
     * 处理查询并获取DeepSeek API响应（非流式）
     *
     * @param userMessage 用户消息
     * @return API的响应
     * @throws Exception 如果与API通信出错
     */
    public String processQuery(String userMessage) throws Exception {
        try {
            // 创建非流式请求体
            JSONObject requestBodyObj = buildRequestBodyObject(userMessage);
            requestBodyObj.put("stream", false); // 确保非流式
            String requestBody = requestBodyObj.toString();

            System.out.println("发送请求到: " + API_ENDPOINT);
            System.out.println("请求体: " + requestBody);

            // 创建HTTP请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("响应状态码: " + response.statusCode());

            if (response.statusCode() == 200) {
                // 解析JSON响应
                JSONObject jsonResponse = new JSONObject(response.body());
                if (jsonResponse.has("choices")) {
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        if (choice.has("message")) {
                            JSONObject message = choice.getJSONObject("message");
                            if (message.has("content")) {
                                return message.getString("content");
                            }
                        }
                    }
                }
                return "Failed to extract content from response: " + response.body();
            } else {
                return "API Error (Code " + response.statusCode() + "): " + response.body();
            }
        } catch (Exception e) {
            throw new Exception("Error communicating with AI service: " + e.getMessage(), e);
        }
    }

    /**
     * 处理带有流式响应回调的查询
     * @param userMessage 用户消息
     * @param callback 流式响应回调接口
     * @return 是否成功处理请求
     */
    public boolean processStreamingQuery(String userMessage, StreamCallback callback) {
        try {
            // 创建流式请求体
            JSONObject requestBodyObj = buildRequestBodyObject(userMessage);
            requestBodyObj.put("stream", true); // 确保流式
            String requestBody = requestBodyObj.toString();

            System.out.println("发送流式请求到: " + API_ENDPOINT);
            System.out.println("请求体: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 使用InputStream处理流式响应
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            System.out.println("响应状态码: " + response.statusCode());

            if (response.statusCode() == 200) {
                try (InputStream inputStream = response.body();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                            String jsonData = line.substring(6).trim(); // 移除 "data: " 前缀
                            try {
                                JSONObject chunk = new JSONObject(jsonData);
                                if (chunk.has("choices")) {
                                    JSONArray choices = chunk.getJSONArray("choices");
                                    if (choices.length() > 0) {
                                        JSONObject choice = choices.getJSONObject(0);
                                        if (choice.has("delta")) {
                                            JSONObject delta = choice.getJSONObject("delta");
                                            if (delta.has("content")) {
                                                String content = delta.getString("content");
                                                System.out.print(content); // 实时输出到控制台
                                                callback.onToken(content); // 调用回调发送token
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("解析流式响应片段时出错: " + e.getMessage());
                                System.err.println("原始数据: " + jsonData);
                                e.printStackTrace();
                            }
                        } else if (line.contains("[DONE]")) {
                            callback.onComplete(); // 完成时调用回调
                        }
                    }
                    System.out.println(); // 换行
                    return true; // 成功处理请求
                }
            } else {
                System.err.println("API错误响应: " + response.statusCode());
                try (InputStream errorStream = response.body();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {

                    StringBuilder errorMsg = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorMsg.append(line);
                    }
                    String errorDetails = errorMsg.toString();
                    System.err.println("错误详情: " + errorDetails);
                    callback.onError("API错误 (" + response.statusCode() + "): " + errorDetails);
                }
                return false; // 请求失败
            }
        } catch (Exception e) {
            System.err.println("API请求异常: " + e);
            e.printStackTrace();
            callback.onError("API请求异常: " + e.getMessage());
            return false; // 请求失败
        }
    }

    /**
     * 构建DeepSeek API请求体对象
     * @param userMessage 用户消息
     * @return 请求体JSON对象
     */
    private JSONObject buildRequestBodyObject(String userMessage) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);

        JSONArray messages = new JSONArray();

        // 添加系统提示，设定AI的角色和背景
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个理财软件的AI助手，擅长个人理财、预算管理、储蓄规划和投资建议。请基于用户提供的财务数据，提供清晰、实用的财务建议。");
        messages.put(systemMessage);

        // 添加用户消息
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.put(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        return requestBody;
    }

    /**
     * 用于兼容原来的代码，保持接口一致
     */
    public static String getFinancialAdvice(String prompt) throws Exception {
        return getAdvice(prompt);
    }

    /**
     * 提供本地响应的备选方案，以防API不可用
     * @param query 用户查询
     * @return 预设的本地响应
     */
    public String getLocalResponse(String query) {
        // 预设回答
        if (query.contains("财政分析") || query.contains("financial analysis")) {
            return "基于您的消费记录，我注意到您在餐饮方面的支出占比较高，达到了总支出的35%。建议您可以考虑更多自己烹饪，这样可以节省一部分开支。同时，您的储蓄率大约为15%，稍低于推荐的20%水平，可以考虑逐步增加每月的储蓄金额。";
        } else if (query.contains("预算建议") || query.contains("budget")) {
            return "制定月度预算时，可以考虑50/30/20法则：50%用于必需品（房租、食物、交通），30%用于个人支出（娱乐、购物），20%用于储蓄和投资。根据您的收入水平，建议每月至少存入收入的20%作为应急基金和长期储蓄。";
        } else if (query.contains("储蓄建议") || query.contains("saving")) {
            return "对于储蓄，建议您首先建立3-6个月生活费用的应急基金，可以存入活期或短期理财产品以保持流动性。之后，可以考虑根据您的风险承受能力和投资期限，配置一定比例的指数基金、定期存款和其他投资产品，实现资产的稳健增长。";
        } else {
            return "我可以为您提供财务管理、预算规划、储蓄投资等方面的建议。基于您提供的数据，我建议您关注支出较高的类别，并寻找节约的机会。同时，确保您有足够的应急基金，并开始制定长期的财务目标。";
        }
    }

    /**
     * 测试API连接
     * @return 测试结果
     */
    public String testConnection() {
        StringBuilder result = new StringBuilder();
        result.append("测试连接到: ").append(API_ENDPOINT).append("\n");
        result.append("使用模型: ").append(MODEL).append("\n");

        try {
            // 尝试一个简单的查询
            String simpleQuery = processQuery("Hello, just testing the API connection.");
            result.append("API测试结果: ").append(simpleQuery);

            return result.toString();
        } catch (Exception e) {
            result.append("连接测试失败: ").append(e.getMessage()).append("\n");
            if (e.getCause() != null) {
                result.append("原因: ").append(e.getCause().getMessage());
            }
            return result.toString();
        }
    }
}