package org.example.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.example.exceptions.UnsupportedFileTypeException;
import org.example.util.FileUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocParserService {

    public String extract(InputStream stream, String objectKey) {

        String extension= FileUtils.getFileExtension(objectKey);

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

    public List<String> extractPDFPages(InputStream inputStream){
        List<String> pages = new ArrayList<>();
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();

            for (int pageNumber = 1;
                 pageNumber <= document.getNumberOfPages();
                 pageNumber++) {

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String pageText = stripper.getText(document).trim();

                if (!pageText.isBlank()) {
                    pages.add(pageText);
                }
            }
        }catch (IOException e){
            throw new RuntimeException("Unable to extract PDF pages"+e);
        }

        return pages;
    }



}