package com.bookshell.server.xfxh;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BookDigest {
    @Autowired
    private RestTemplate restTemplate;

    public String sendPostRequest(String bookName, String APIPassword) {
        String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";

        // 创建请求体对象
        RequestBody requestBody = new RequestBody();
        requestBody.setModel("4.0Ultra");
        requestBody.setUser("user_id");

        // 创建messages
        RequestBody.Message message = new RequestBody.Message();
        message.setRole("user");
        message.setContent("请为未读过《"+bookName+"》这本书的顾客提供一段摘要，仅提供这本书的摘要，不要剧透");
        requestBody.setMessages(List.of(message));

        // 创建tools
        RequestBody.Tool tool = new RequestBody.Tool();
        tool.setType("web_search");
        RequestBody.WebSearch webSearch = new RequestBody.WebSearch();
        webSearch.setEnable(true);
        webSearch.setSearchMode("normal");
        tool.setWebSearch(webSearch);
        requestBody.setTools(List.of(tool));

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(APIPassword);

        // 创建请求实体
        HttpEntity<RequestBody> requestEntity = new HttpEntity<>(requestBody, headers);
        try {
            ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                    url, requestEntity, ApiResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ApiResponse apiResponse = response.getBody();
                if (apiResponse.getCode() == 0 && !apiResponse.getChoices().isEmpty()) {
                    return apiResponse.getChoices().get(0).getMessage().getContent();
                }
            }
            throw new RuntimeException("API响应异常：" + response.getBody().getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return "API响应异常：" + e.getMessage();
        }
    }

    // 请求体内嵌类
    static class RequestBody {
        private String model;
        private String user;
        private List<Message> messages;
        private List<Tool> tools;

        // getters and setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        public List<Message> getMessages() { return messages; }
        public void setMessages(List<Message> messages) { this.messages = messages; }
        public List<Tool> getTools() { return tools; }
        public void setTools(List<Tool> tools) { this.tools = tools; }

        static class Message {
            private String role;
            private String content;

            // getters and setters
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
            public String getContent() { return content; }
            public void setContent(String content) { this.content = content; }
        }

        static class Tool {
            private String type;
            @JsonProperty("web_search")
            private WebSearch webSearch;

            // getters and setters
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
            public WebSearch getWebSearch() { return webSearch; }
            public void setWebSearch(WebSearch webSearch) { this.webSearch = webSearch; }
        }

        static class WebSearch {
            private boolean enable;
            @JsonProperty("search_mode")
            private String searchMode;

            // getters and setters
            public boolean isEnable() { return enable; }
            public void setEnable(boolean enable) { this.enable = enable; }
            public String getSearchMode() { return searchMode; }
            public void setSearchMode(String searchMode) { this.searchMode = searchMode; }
        }
    }

    // 请求返回体
    static class ApiResponse {
        private int code;
        private String message;
        private String sid;
        private List<Choice> choices;

        // getters/setters
        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSid() { return sid; }
        public void setSid(String sid) { this.sid = sid; }
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }

        public static class Choice {
            private Message message;
            private int index;

            // getters/setters
            public Message getMessage() { return message; }
            public void setMessage(Message message) { this.message = message; }
            public int getIndex() { return index; }
            public void setIndex(int index) { this.index = index; }
        }

        public static class Message {
            private String role;
            private String content;

            // getters/setters
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
            public String getContent() { return content; }
            public void setContent(String content) { this.content = content; }
        }
    }
}
