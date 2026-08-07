package org.example.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.exceptions.UnsupportedFileTypeException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class DocParserService {

    public String extract(InputStream stream, String objectKey) {

        String extension=getExtension(objectKey);

        try {

            return switch (extension) {

                case "pdf" ->
                        extractPdf(stream);

                case "docx" ->
                        extractDocx(stream);

                case "txt" ->
                        extractTxt(stream);

                default ->
                        throw new UnsupportedOperationException(
                                "Unsupported content type : " + extension
                        );
            };

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract document text", e);
        }
    }

    private String extractPdf(InputStream stream) throws IOException {

        try (PDDocument document = Loader.loadPDF(stream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();

            return stripper.getText(document);
        }
    }

    private String extractDocx(InputStream stream) throws IOException {

        try (XWPFDocument document = new XWPFDocument(stream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {

            return extractor.getText();
        }
    }

    private String extractTxt(InputStream stream) throws IOException {

        return new String(
                stream.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String getExtension(String objectKey) {

        int dotIndex = objectKey.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == objectKey.length() - 1) {
            throw new UnsupportedFileTypeException("File has no extension");
        }

        return objectKey.substring(dotIndex + 1);
    }

}