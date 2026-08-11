package org.example.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.example.dto.LuceneDocumentData;
import org.example.exceptions.LuceneServiceException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class LuceneService {
    private IndexWriter indexWriter;
    private Path indexFolderPath;
    private Analyzer analyzer;

    @PostConstruct
    public void init() throws IOException {
        try {
            indexFolderPath= Paths.get("D:\\NCDC\\FinanceAI\\data\\lucene");
            Directory directory= FSDirectory.open(indexFolderPath);
            analyzer=new StandardAnalyzer();
            IndexWriterConfig config=new IndexWriterConfig(analyzer);
            indexWriter=new IndexWriter(directory,config);
        }catch (IOException e){
            throw new LuceneServiceException("Error while initializing index writer-Lucene service-Post construct");
        }
    }

    public void saveLuceneDocuments(
            List<LuceneDocumentData> documents
    ){
        try {
            for (LuceneDocumentData data : documents) {

                Document document = new Document();

                document.add(
                        new TextField(
                                "text",
                                data.getChunkText(),
                                Field.Store.NO
                        )
                );

                document.add(
                        new StringField(
                                "chunkId",
                                data.getChunkId(),
                                Field.Store.YES
                        )
                );

                document.add(
                        new StringField(
                                "documentId",
                                data.getDocId(),
                                Field.Store.YES
                        )
                );

                document.add(
                        new StringField(
                                "userId",
                                data.getUserId(),
                                Field.Store.YES
                        )
                );

                indexWriter.addDocument(document);
            }

            indexWriter.commit();
        }catch (IOException e){
            throw new LuceneServiceException("Error while processing lucene documents");
        }
    }

    public void searchLucene(String userQuery,String userId){


        try{
            Directory directory =
                    FSDirectory.open(indexFolderPath);
            DirectoryReader reader = DirectoryReader.open(directory);

            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            QueryParser parser=new QueryParser("text",analyzer);
            Query query=parser.parse(userQuery);

            Query userFilter =
                    new TermQuery(
                            new Term("userId", userId)
                    );

            Query finalQuery =
                    new BooleanQuery.Builder()
                            .add(query, BooleanClause.Occur.MUST)
                            .add(userFilter, BooleanClause.Occur.FILTER)
                            .build();

            TopDocs results=searcher.search(finalQuery,10);
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc scoreDoc : results.scoreDocs) {

                System.out.println("BM25 score: " + scoreDoc.score);

                Document document =
                        storedFields.document(scoreDoc.doc);

                String chunkId = document.get("chunkId");
                String documentId = document.get("documentId");

                System.out.println("chunkId: " + chunkId);
                System.out.println("documentId: " + documentId);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }catch (IOException e){
            throw new LuceneServiceException("Unable to fetch docs-lucene service error");
        }
    }


}
