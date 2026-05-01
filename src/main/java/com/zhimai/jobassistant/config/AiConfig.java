package com.zhimai.jobassistant.config;

import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置
 *
 * Spring AI 的自动配置已经帮我们做了很多：
 * - ChatClient 自动注入
 * - EmbeddingModel 自动配置
 * - VectorStore 自动连接
 *
 * 这里可以放一些自定义配置
 */
@Configuration
public class AiConfig {

    // 如果需要自定义 ChatClient，可以在这里配置
    // 比如设置 system prompt、advisors 等

    // @Bean
    // public ChatClient chatClient(ChatClient.Builder builder) {
    //     return builder
    //             .defaultSystem("你是一个专业的求职顾问...")
    //             .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
    //             .build();
    // }
}
