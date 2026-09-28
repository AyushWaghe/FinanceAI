from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.table import WD_CELL_VERTICAL_ALIGNMENT, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor


OUTPUT = r"D:\NCDC\FinanceAI\FinanceAI_Project_Documentation.docx"


def set_cell_shading(cell, fill):
    props = cell._tc.get_or_add_tcPr()
    shading = props.find(qn("w:shd"))
    if shading is None:
        shading = OxmlElement("w:shd")
        props.append(shading)
    shading.set(qn("w:fill"), fill)


def set_cell_borders(cell, color="D9D9D9", size="6"):
    props = cell._tc.get_or_add_tcPr()
    borders = props.first_child_found_in("w:tcBorders")
    if borders is None:
        borders = OxmlElement("w:tcBorders")
        props.append(borders)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = "w:" + edge
        element = borders.find(qn(tag))
        if element is None:
            element = OxmlElement(tag)
            borders.append(element)
        element.set(qn("w:val"), "single")
        element.set(qn("w:sz"), size)
        element.set(qn("w:space"), "0")
        element.set(qn("w:color"), color)


def set_cell_margins(cell, top=90, start=110, bottom=90, end=110):
    props = cell._tc.get_or_add_tcPr()
    margins = props.first_child_found_in("w:tcMar")
    if margins is None:
        margins = OxmlElement("w:tcMar")
        props.append(margins)
    for side, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = margins.find(qn("w:" + side))
        if node is None:
            node = OxmlElement("w:" + side)
            margins.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_run_font(run, name="Times New Roman", size=11, bold=False, italic=False, color="000000"):
    run.font.name = name
    run._element.get_or_add_rPr().rFonts.set(qn("w:ascii"), name)
    run._element.get_or_add_rPr().rFonts.set(qn("w:hAnsi"), name)
    run.font.size = Pt(size)
    run.bold = bold
    run.italic = italic
    run.font.color.rgb = RGBColor.from_string(color)


def style_paragraph(paragraph, before=0, after=7, line=1.08, align=None):
    fmt = paragraph.paragraph_format
    fmt.space_before = Pt(before)
    fmt.space_after = Pt(after)
    fmt.line_spacing = line
    if align is not None:
        paragraph.alignment = align


def add_para(doc, text="", bold_prefix=None, italic=False, after=7):
    p = doc.add_paragraph()
    style_paragraph(p, after=after)
    if bold_prefix and text.startswith(bold_prefix):
        r = p.add_run(bold_prefix)
        set_run_font(r, bold=True)
        r = p.add_run(text[len(bold_prefix):])
        set_run_font(r, italic=italic)
    else:
        r = p.add_run(text)
        set_run_font(r, italic=italic)
    return p


def add_heading(doc, text, level=1):
    p = doc.add_paragraph(style="Heading 1" if level == 1 else "Heading 2")
    style_paragraph(p, before=12 if level == 1 else 8, after=5, line=1.0)
    r = p.add_run(text)
    set_run_font(r, size=15 if level == 1 else 12, bold=True)
    return p


def add_bullet(doc, text):
    p = doc.add_paragraph(style="List Bullet")
    style_paragraph(p, after=3, line=1.0)
    for run in p.runs:
        set_run_font(run)
    if not p.runs:
        set_run_font(p.add_run(text))
    else:
        p.runs[0].text = text
    return p


def add_code(doc, text):
    p = doc.add_paragraph()
    style_paragraph(p, before=2, after=8, line=1.0)
    p.paragraph_format.left_indent = Inches(0.25)
    r = p.add_run(text)
    set_run_font(r, name="Consolas", size=9, color="303030")
    return p


def add_table(doc, headers, rows, widths=None):
    table = doc.add_table(rows=1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.style = "Table Grid"
    header_properties = table.rows[0]._tr.get_or_add_trPr()
    header_marker = OxmlElement("w:tblHeader")
    header_marker.set(qn("w:val"), "true")
    header_properties.append(header_marker)
    hdr = table.rows[0].cells
    for i, value in enumerate(headers):
        hdr[i].text = ""
        set_cell_shading(hdr[i], "D9EAF0")
        set_cell_borders(hdr[i])
        set_cell_margins(hdr[i])
        hdr[i].vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
        p = hdr[i].paragraphs[0]
        style_paragraph(p, after=0, line=1.0)
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        r = p.add_run(value)
        set_run_font(r, size=10, bold=True)
    for row_index, row in enumerate(rows):
        cells = table.add_row().cells
        for i, value in enumerate(row):
            cells[i].text = ""
            set_cell_borders(cells[i])
            set_cell_margins(cells[i])
            cells[i].vertical_alignment = WD_CELL_VERTICAL_ALIGNMENT.CENTER
            if row_index % 2 == 1:
                set_cell_shading(cells[i], "F5F8FA")
            p = cells[i].paragraphs[0]
            style_paragraph(p, after=0, line=1.0)
            r = p.add_run(str(value))
            set_run_font(r, size=9.5)
        if widths:
            for cell, width in zip(cells, widths):
                cell.width = Inches(width)
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return table


def add_placeholder(doc, label):
    p = doc.add_paragraph()
    style_paragraph(p, after=8)
    r = p.add_run(label + ": ")
    set_run_font(r, bold=True)
    r = p.add_run(" ")
    set_run_font(r)
    return p


doc = Document()
section = doc.sections[0]
section.page_width = Inches(8.27)
section.page_height = Inches(11.69)
section.top_margin = Inches(1)
section.bottom_margin = Inches(1)
section.left_margin = Inches(1)
section.right_margin = Inches(1)

normal = doc.styles["Normal"]
normal.font.name = "Times New Roman"
normal._element.rPr.rFonts.set(qn("w:ascii"), "Times New Roman")
normal._element.rPr.rFonts.set(qn("w:hAnsi"), "Times New Roman")
normal.font.size = Pt(11)
normal.paragraph_format.space_after = Pt(7)
normal.paragraph_format.line_spacing = 1.08

for style_name, size in (("Heading 1", 15), ("Heading 2", 12)):
    style = doc.styles[style_name]
    style.font.name = "Times New Roman"
    style._element.rPr.rFonts.set(qn("w:ascii"), "Times New Roman")
    style._element.rPr.rFonts.set(qn("w:hAnsi"), "Times New Roman")
    style.font.size = Pt(size)
    style.font.bold = True
    style.font.color.rgb = RGBColor(0, 0, 0)

# Project overview
add_heading(doc, "Project Overview")
add_para(doc, "What if your financial documents could answer questions instead of simply occupying folders?")
add_para(doc, "What if a bank statement, payslip, offer letter, or HR policy could be searched using ordinary language, while live financial data could still be fetched when the answer depends on your latest transactions?")
add_para(doc, "FinanceAI is an AI-powered financial document assistant designed to bring a user's uploaded documents and personal finance data into one conversational experience.")
add_para(doc, "The application combines document storage, asynchronous ingestion, large-language-model classification, configurable chunking, embeddings, hybrid retrieval, reranking, and tool-enabled planning. Users can upload documents, ask questions about their contents, and request live information such as transactions, bills, or analytics through connected finance services.")
add_para(doc, "The key focus of FinanceAI is grounded assistance. Uploaded documents are indexed for retrieval, while live financial questions can be routed to application tools instead of relying on stale conversation memory. This gives the assistant a practical foundation for answering finance-related questions with relevant context and clearer source boundaries.")

# Problem statement
add_heading(doc, "Problem Statement")
add_para(doc, "Personal financial information is often distributed across bank statements, payslips, offer letters, tax files, HR policies, transaction histories, bills, and other documents. Finding one specific answer may require opening several files, remembering where a detail was recorded, or manually comparing information across sources.")
add_para(doc, "Traditional document search is usually based on exact keywords. It can miss relevant content when a user asks a question using different wording from the document. A basic chatbot has the opposite problem: it may produce fluent answers without reliably grounding them in the user's documents or latest financial data.")
add_para(doc, "A useful assistant therefore needs to solve several problems together: accept multiple document formats, identify what was uploaded, split content appropriately, make documents searchable, retrieve relevant passages, protect user-level data isolation, and combine retrieved evidence with live finance tools when necessary.")
add_para(doc, "Without these controls, users risk receiving incomplete, outdated, or unsupported financial answers. FinanceAI addresses this problem by combining retrieval-augmented generation with explicit tool usage and user-scoped document retrieval.")

# Proposed solution
add_heading(doc, "Proposed Solution")
add_para(doc, "FinanceAI provides a Spring Boot service that turns uploaded financial and professional documents into searchable knowledge and exposes that knowledge through a conversational AI interface.")
add_para(doc, "When a user uploads a PDF, DOCX, or TXT file, the file is stored in S3-compatible object storage and a metadata record is created in PostgreSQL. A Kafka event then starts the ingestion pipeline asynchronously. The pipeline downloads the file, extracts its text, asks an LLM to classify the document and recommend a chunking strategy, generates embeddings, and stores searchable representations in both Qdrant and Apache Lucene.")
add_para(doc, "When the user asks a question, the planner agent can use finance tools for live transactions, bills, and analytics or use the RAG tool for document-grounded questions. The RAG pipeline performs BM25 keyword retrieval and vector retrieval in parallel, fuses candidates using Reciprocal Rank Fusion, reranks the best candidates through Jina AI, and sends the final context to an LLM for an answer.")
add_para(doc, "Conversation history is persisted in PostgreSQL. When the context grows beyond the configured token threshold, the service generates a conversation summary so that the assistant can retain useful continuity without repeatedly sending the entire conversation to the model.")

# User stories
add_heading(doc, "User Stories")
stories = [
    "As a user, I want to upload financial and professional documents so that I can ask questions about them later.",
    "As a user, I want the system to identify the document type automatically so that I do not have to organise every file manually.",
    "As a user, I want to ask questions in natural language so that I can find information without remembering exact document keywords.",
    "As a user, I want answers to be grounded in my uploaded documents so that the assistant does not rely only on general model knowledge.",
    "As a user, I want live transaction, bill, and analytics questions to use current finance data so that answers do not become stale.",
    "As a user, I want to view my previous conversation and clear it when required so that I can manage my assistant history.",
    "As a user, I want to download or delete uploaded files so that I retain control over my stored financial documents.",
    "As a developer, I want retrieval quality to be evaluated with recall, precision, NDCG, and RAG test cases so that changes to the pipeline can be measured.",
]
for story in stories:
    add_bullet(doc, story)

# Tech stack
add_heading(doc, "Tech Stack")
add_table(doc, ["Category", "Technology", "Purpose"], [
    ("Language", "Java 17", "Primary backend language."),
    ("Framework", "Spring Boot 3.5.12", "REST APIs, dependency injection, configuration, and application runtime."),
    ("AI framework", "Spring AI 1.0.9", "Chat model, embedding model, tool calling, and OpenAI integration."),
    ("LLM", "OpenAI models", "Planner responses, document analysis, and RAG answer generation."),
    ("Persistence", "PostgreSQL + Spring Data JPA", "Conversation, message, and uploaded-document metadata storage."),
    ("Object storage", "AWS SDK S3 client", "Stores uploaded files using a configurable S3-compatible endpoint."),
    ("Vector database", "Qdrant", "Stores document embeddings and retrieves semantically similar chunks."),
    ("Keyword search", "Apache Lucene 9.12.2", "BM25 lexical retrieval over indexed document chunks."),
    ("Reranking", "Jina AI reranker", "Reranks hybrid retrieval candidates before final answer generation."),
    ("Messaging", "Apache Kafka + Spring Kafka", "Triggers asynchronous document ingestion after upload."),
    ("Document parsing", "Apache PDFBox and Apache POI", "Extracts text from PDF and DOCX files."),
    ("Security", "Spring Security + JWT", "Protects APIs and propagates the authenticated user context."),
    ("Observability", "Micrometer + Spring Boot Actuator", "Tracks pipeline timing, processed/failed documents, and health."),
    ("Build and tests", "Maven + JUnit 5", "Dependency management, packaging, unit tests, and retrieval evaluations."),
])

# Core features
add_heading(doc, "Core Features")
features = [
    ("Conversational Finance Assistant", "Accepts natural-language questions through the /chat API and stores user and assistant messages for continuity."),
    ("Tool-Enabled Planner Agent", "The planner can use transaction, bill, analytics, helper, and RAG tools. It supports a deliberate mode using gpt-5-mini and a faster response mode using gpt-4.1-mini."),
    ("Multi-Format Document Upload", "Supports PDF, DOCX, and TXT extraction. Uploaded files are stored in S3-compatible storage while metadata is tracked in PostgreSQL."),
    ("Asynchronous Document Ingestion", "A user-document-uploaded Kafka event starts ingestion outside the upload request, allowing file upload and document processing to be separated."),
    ("LLM-Guided Document Analysis", "The ingestion prompt identifies a document type, selects PAGE, DOCUMENT, or FIXED_SIZE chunking, recommends chunk settings where required, and creates a short summary."),
    ("Hybrid Retrieval", "Runs BM25 keyword retrieval through Lucene and semantic vector retrieval through Qdrant, filters both by user ID, and combines results with Reciprocal Rank Fusion."),
    ("Reranked RAG Answers", "The highest-ranked hybrid candidates are reranked through Jina AI before the final context is sent to the RAG response model."),
    ("Conversation Memory Management", "Persists conversations and messages, then creates a summary once the recent context reaches the configured token threshold."),
    ("File Lifecycle Management", "Lists user files, downloads objects, and deletes file metadata and vector representations when a document is removed."),
    ("Pipeline Metrics and Document States", "Tracks processing time and success/failure counters. Documents move through states such as PROCESSING, COMPLETED, REJECTED, and SERVER_ERROR."),
]
for title, description in features:
    add_para(doc, title, bold_prefix=title)
    add_para(doc, description, after=6)

# Architecture
add_heading(doc, "Architecture")
add_para(doc, "FinanceAI is organised as a layered Spring Boot service with separate boundaries for API handling, orchestration, storage, retrieval, and external AI integrations.")
add_code(doc, "Client -> Spring Security and JWT filter -> REST Controllers")
add_code(doc, "ChatController -> ChatService -> PlannerAgent -> Finance tools or RAGAgentTool")
add_code(doc, "FileStorageController -> FileService -> S3 object storage + PostgreSQL metadata")
add_code(doc, "FileService -> Kafka user-document-uploaded-topic -> UserDocumentConsumer -> DocumentIngestionPipeline")
add_code(doc, "DocumentIngestionPipeline -> Parser -> LLM analysis -> Chunking -> Embeddings -> Qdrant + Lucene")
add_code(doc, "RAGAgentTool -> DocumentRetrievalPipeline -> Lucene + Qdrant -> RRF -> Jina reranker -> LLM response")
add_para(doc, "Architecture diagram: ")
add_para(doc, "The editable architecture diagram can be inserted here once the final draw.io version is selected.", italic=True)

add_heading(doc, "Document Ingestion Workflow", level=2)
add_para(doc, "1. The authenticated user uploads a supported file through POST /file.")
add_para(doc, "2. FileService stores the object using an S3-compatible client and creates a UserDocument record with PROCESSING status.")
add_para(doc, "3. The service publishes UserDocumentUploadedEvent to user-document-uploaded-topic.")
add_para(doc, "4. UserDocumentConsumer receives the event and invokes DocumentIngestionPipeline asynchronously.")
add_para(doc, "5. The pipeline downloads the object, extracts text, rejects documents over the configured token limit, and asks the LLM to determine type, summary, and chunking strategy.")
add_para(doc, "6. Chunks are embedded and written to Qdrant with user and document metadata. The same chunks are indexed in Lucene for BM25 retrieval.")
add_para(doc, "7. The UserDocument record is updated with its type, summary, and final document state. Metrics are recorded for successful and failed processing.")

add_heading(doc, "Question Answering Workflow", level=2)
add_para(doc, "1. ChatService loads the authenticated user's conversation and recent messages after the latest summary timestamp.")
add_para(doc, "2. PlannerAgent receives the query and can call transaction, bill, analytics, helper, or RAG tools depending on the request.")
add_para(doc, "3. For document questions, the retrieval pipeline performs Lucene BM25 and Qdrant vector retrieval concurrently, with user ID filters applied to both paths.")
add_para(doc, "4. Reciprocal Rank Fusion combines the lexical and semantic candidates. Qdrant provides the chunk text and metadata for the fused candidates.")
add_para(doc, "5. Jina reranks the candidates, and the best results are passed to the RAG prompt and final language model.")
add_para(doc, "6. The assistant response and user query are stored as messages. A new conversation summary is stored when the token threshold requires it.")

# Data model
add_heading(doc, "Data Model")
add_table(doc, ["Entity", "Important fields", "Relationship / purpose"], [
    ("Conversation", "conversationId, userId, latestSummary, summaryUpdatedAt", "One persisted conversation is associated with a user ID and contains messages."),
    ("Message", "messageId, role, content, createdAt, conversation_id", "Many messages belong to one conversation; cascade and orphan removal are enabled."),
    ("UserDocument", "id, userId, objectKey, docType, docSummary, documentState, rejectedReason", "Stores file metadata and ingestion status. The user ID is stored as a scalar value."),
    ("Lucene index", "text, chunkId, documentId, userId", "Local BM25 index used for keyword retrieval and user filtering."),
    ("Qdrant point", "vector, chunkText, docId, userId, documentType", "Vector representation and payload used for semantic retrieval and filtering."),
])
add_para(doc, "Database diagram: ")
add_para(doc, "The current persistence model is represented by the Conversation-Message and UserDocument entities. Lucene and Qdrant are search stores rather than relational tables.", italic=True)

# Design decisions
add_heading(doc, "Design Decisions")
decisions = [
    ("Hybrid retrieval instead of a single search strategy", "Keyword search is strong when users use exact names, phrases, account identifiers, or policy terms. Vector search is stronger when the question uses different wording from the source document. FinanceAI runs both approaches, filters both by user ID, and combines their rankings using Reciprocal Rank Fusion. This reduces dependence on one retrieval failure mode, while Jina reranking further improves the ordering of the final candidates."),
    ("Asynchronous ingestion with Kafka", "Uploading a file and processing a file have different latency and failure characteristics. The upload path stores the object and metadata quickly, then publishes UserDocumentUploadedEvent. A consumer starts the heavier parsing, LLM, embedding, Qdrant, and Lucene work. This prevents the upload request from waiting for the complete AI pipeline and creates a clear place for retries and consumer scaling. The current system would benefit from adding retry and dead-letter handling as a future production hardening step."),
    ("LLM-guided chunking strategy", "Different documents have different retrieval structures. A short resume or offer letter may be meaningful as one document, a bank statement may be better represented page by page, and a long policy document may require fixed-size chunks with overlap. The ingestion prompt selects DOCUMENT, PAGE, or FIXED_SIZE and can recommend chunk size and overlap. This keeps chunking aligned with document structure instead of applying one arbitrary rule to every file."),
    ("Separate stores for source files, metadata, and search representations", "S3-compatible storage is used for the original file, PostgreSQL stores ownership and document lifecycle metadata, Qdrant stores semantic vectors, and Lucene stores the local lexical index. Each store is used for the capability it handles best. The trade-off is that deletion and failure recovery must keep several representations consistent."),
    ("Tool-enabled planner with live data as the source of truth", "The planner can call finance tools for transactions, bills, and analytics rather than trying to answer every question from chat memory. The planner prompt explicitly instructs the model to use tools for current data. This is important for financial questions because stored conversation summaries can become stale while transaction and bill data changes."),
    ("Conversation summaries for token control", "Full conversation history becomes increasingly expensive and can reduce the useful context available to the model. FinanceAI stores messages but sends the latest summary plus messages created after that summary when constructing the next request. A new summary is generated after the configured token threshold is reached, preserving continuity while controlling prompt size."),
    ("User-scoped retrieval", "Every Lucene and Qdrant retrieval path receives the authenticated user ID as a filter. The user ID is also stored in document metadata and search payloads. This prevents a semantically similar document belonging to another user from being returned as context, which is a core privacy requirement for a financial application."),
]
for title, description in decisions:
    add_para(doc, title, bold_prefix=title)
    add_para(doc, description, after=6)

# API overview
add_heading(doc, "API Overview")
add_table(doc, ["Method", "Endpoint", "Purpose"], [
    ("GET", "/chat?query={query}&thinkAndAnswer={boolean}", "Ask the planner agent a question and receive the conversation response list."),
    ("GET", "/chat/getMessages", "Retrieve the authenticated user's stored conversation messages."),
    ("DELETE", "/chat", "Delete the authenticated user's conversation."),
    ("POST", "/file", "Upload a document for asynchronous ingestion."),
    ("GET", "/file?objectKey={objectKey}", "Download a stored document."),
    ("GET", "/file/getFiles", "List the authenticated user's uploaded file metadata and ingestion states."),
    ("DELETE", "/file?objectKey={objectKey}", "Delete the stored file, metadata, and vector document representation."),
    ("GET", "/actuator/health", "Health endpoint exposed through Spring Boot Actuator."),
])

# Security
add_heading(doc, "Security")
add_para(doc, "The service uses Spring Security with a stateless security policy and a JWT authentication filter. The filter accepts a Bearer token or an HTTP-only jwt cookie, validates the signed claims, and places the user ID into the Spring Security context.")
add_para(doc, "All application endpoints require authentication. The actuator endpoint is explicitly permitted for health monitoring. The FinanceMVC backend URL is configured as an external dependency, and the RestClient forwards the incoming cookie when the AI service calls finance APIs on behalf of the user.")
add_para(doc, "Production deployment should keep JWT secrets, API keys, S3 credentials, Kafka credentials, and database credentials outside source control. HTTPS, secure cookie settings, restrictive CORS, token expiry, and careful logging are required when handling financial documents.")

# Testing
add_heading(doc, "Testing and Evaluation")
add_para(doc, "The repository includes JUnit-based evaluation tests for retrieval, ingestion, reranking, and RAG response quality. Test cases are stored as JSON resources and cover financial statements, HR policies, offer letters, resumes, and other document types.")
add_table(doc, ["Evaluation area", "Current approach"], [
    ("Vector retrieval", "Checks relevant chunk recall and precision against expected chunk IDs; the evaluation asserts a recall threshold of 0.8."),
    ("BM25 retrieval", "Measures relevant results returned by Lucene and asserts recall of at least 0.8."),
    ("Reranking", "Calculates NDCG@5 against relevance judgments and asserts a threshold of 0.7."),
    ("RAG answer quality", "Uses an evaluator model to classify whether the generated answer correctly addresses the expected answer."),
    ("Chunking strategy", "Tests expected document, page, and semantic chunking decisions for representative files."),
])
add_para(doc, "Test environment details, service credentials, execution time, and latest evaluation results: ")

# Setup
add_heading(doc, "Local Setup")
add_para(doc, "Prerequisites: Java 17, Maven, PostgreSQL, Kafka, an S3-compatible object store, Qdrant, and credentials for the configured OpenAI and Jina services. The local Kafka truststore is included under src/main/resources/certs, while service endpoints and credentials are supplied through environment variables.")
add_code(doc, "mvn spring-boot:run")
add_code(doc, "mvn clean package")
add_para(doc, "The application runs on port 8081 by default. Actuator metrics and health endpoints use management port 9091. The project currently expects the required database, Kafka, Qdrant, S3-compatible storage, OpenAI, and Jina configuration to be provided by the active Spring profile.")
add_para(doc, "Environment variables: ")
add_table(doc, ["Variable", "Purpose"], [
    ("DB_URL, DB_USERNAME, DB_PASSWORD", "PostgreSQL connection details."),
    ("KAFKA_BOOTSTRAP_SERVERS, KAFKA_USERNAME, KAFKA_PASSWORD", "Kafka connection and SASL credentials."),
    ("KAFKA_TRUSTSTORE_PASSWORD, KAFKA_TRUSTSTORE_LOCATION", "Kafka SSL truststore configuration."),
    ("QDRANT_URL, QDRANT_HOST, QDRANT_API_KEY", "Qdrant connection and authentication."),
    ("S3_BUCKET_NAME, S3_STORAGE_ENDPOINT, S3_STORAGE_REGION", "Object-storage location."),
    ("S3_API_KEY_ID, S3_API_KEY", "Object-storage credentials."),
    ("OPEN_AI_API_KEY, JINA_API_KEY", "AI model and reranker credentials."),
    ("JWT_SECRET, FRONTEND_URL, BACKEND_URL", "Authentication and service-integration configuration."),
    ("COOKIE_SECURE, COOKIE_SAME_SITE, PORT", "Cookie security and server configuration."),
])

# Limitations and roadmap
add_heading(doc, "Known Limitations and Future Improvements")
for item in [
    "Add explicit retry, dead-letter, and idempotency handling for Kafka-driven ingestion.",
    "Add circuit breakers, timeouts, and clearer fallback responses for OpenAI, Jina, Qdrant, and S3 failures.",
    "Add a durable indexing strategy or a rebuild command for the local Lucene index after restart or recovery.",
    "Ensure document deletion removes all indexed representations consistently, including the Lucene entry.",
    "Add integration tests for authentication, file ownership, Kafka ingestion, and external-service failure paths.",
    "Move hard-coded local filesystem paths and service-specific settings fully into configuration.",
    "Add API documentation through OpenAPI or Swagger and document request/response examples.",
    "Add deployment details, screenshots, live demo information, repository link, author, and license.",
]:
    add_bullet(doc, item)

# Closing metadata placeholders
add_heading(doc, "Project Information")
add_placeholder(doc, "Repository")
add_placeholder(doc, "Live Demo")
add_placeholder(doc, "Author")
add_placeholder(doc, "License")
add_placeholder(doc, "Screenshots")

doc.save(OUTPUT)
print(OUTPUT)
