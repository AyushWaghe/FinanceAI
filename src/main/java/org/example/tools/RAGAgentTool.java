package org.example.tools;

import lombok.RequiredArgsConstructor;
import org.example.service.DocumentRetrievalPipeline;
import org.example.util.AuthenticationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RAGAgentTool {
    private final DocumentRetrievalPipeline documentRetrievalPipeline;
    private static final Logger log = LoggerFactory.getLogger(HelperTools.class);

    @Tool(
            name = "search_knowledge",
            description = """
        Search for the user's LATEST uploaded financial documents for relevant information.

        Use this tool when the user's question requires information from
        uploaded documents such as salary slips, bank statements, financial
        statements, or other personal financial documents.

        Pass the user's query as the search query. The knowledge agent will
        perform document retrieval and return the most relevant information
        from the user's documents.

        Do not use this tool for information that can be obtained directly
        from structured financial data such as transactions, bills, or
        analytics APIs.
        """
    )
    public String callRAGAgent(@ToolParam(
            description = "The user's question or information to search for in their uploaded documents."
    ) String query){
        log.info("RAG agent called");
        Integer userId=getAuthenticatedUserId();
        return documentRetrievalPipeline.retrieveDocuments(query,userId);
    }

    private Integer getAuthenticatedUserId() {
        return AuthenticationUtil.getCurrentUserId();
    }
}
