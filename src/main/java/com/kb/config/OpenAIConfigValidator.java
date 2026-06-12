package com.kb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@Slf4j
public class OpenAIConfigValidator implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            String msg = "环境变量 OPENAI_API_KEY 未配置。将以本地回退模式运行（使用确定性向量），若需调用 OpenAI 请设置该变量。示例（Windows PowerShell）：\n" +
                "setx OPENAI_API_KEY \"sk-...\" ; 重新打开终端并重启应用。";
            log.warn(msg);
            return;
        }
        log.info("OPENAI_API_KEY 已配置，长度 {}，启动校验通过。", apiKey.length());
    }
}
