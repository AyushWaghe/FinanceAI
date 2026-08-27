package org.example.util;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;

public class TokenUtil {

    private static final EncodingRegistry registry = Encodings.newLazyEncodingRegistry();

    private static final Encoding encoding =
            registry.getEncodingForModel(ModelType.GPT_4);

    public static int getTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        return encoding.countTokens(text);
    }
}