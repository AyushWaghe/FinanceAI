package org.example.prompts;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Service
public class PromptLoaderImpl {
    private final HashMap<String,String> systemPrompts=new HashMap<>();

    @PostConstruct
    public void loadPrompts(){
        PathMatchingResourcePatternResolver resolver=new PathMatchingResourcePatternResolver();

        try {
            Resource[] resources=resolver.getResources("classpath:prompts/*.txt");

            for(Resource r:resources){
                String fileName=r.getFilename();

                if(fileName==null) continue;

                String promptName=fileName.replace(".txt","");

                String promptContent=new String(r.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8);

                systemPrompts.put(promptName,promptContent);
            }
        }catch (IOException e){
            throw new RuntimeException("Failed to load prompt files.", e);
        }
    }

    public String load(String promptName) {
        String prompt=systemPrompts.get(promptName);

        if(prompt==null){
            throw new IllegalArgumentException(
                    "Prompt not found : " + promptName);
        }

        return prompt;
    }
}
