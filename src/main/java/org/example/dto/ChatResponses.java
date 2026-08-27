package org.example.dto;

import lombok.Data;
import org.example.enums.Role;

@Data
//@AllArgsConstructor
public class ChatResponses {
    private Long messageId;
    private Role role;
    private String message;

    public ChatResponses(Long messageId, Role role, String message) {
        this.messageId = messageId;
        this.role = role;
        this.message = message;
    }
}
