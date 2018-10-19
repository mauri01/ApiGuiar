package com.example.service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.model.TargetManager;
import com.example.repository.TargetManagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

@Service("fileService")
public class FileService {

    @Autowired
    private TargetManagerRepository targetManagerRepository;

    @Autowired
    private AmazonS3 s3client;

    @Value("${jsa.aws.access_key_id}")
    private String awsId;

    @Value("${jsa.aws.secret_access_key}")
    private String awsKey;

    @Value("${jsa.s3.region}")
    private String region;

    @Value("${jsa.s3.bucket}")
    private String nameBucket;

    @Value("${spring.folder.image}")
    String UPLOADED_FOLDER;

    @Value("${spring.folder.video}")
    String UPLOADED_FOLDER_VIDEO;

    private Logger logger = LoggerFactory.getLogger(FileService.class);

    public String saveFile(MultipartFile file,String type){

        try {
            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
            if(type.contains("image")){
                return convFile.getAbsolutePath();
            }else{
                s3client.putObject(new PutObjectRequest(nameBucket, file.getOriginalFilename(), convFile).withCannedAcl(CannedAccessControlList.PublicRead));
                URL url = s3client.getUrl(nameBucket, file.getOriginalFilename());
                convFile.delete();
                return url.toString();
            }

        } catch (AmazonServiceException ase) {
            logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
            logger.info("Error Message:    " + ase.getMessage());
            logger.info("HTTP Status Code: " + ase.getStatusCode());
            logger.info("AWS Error Code:   " + ase.getErrorCode());
            logger.info("Error Type:       " + ase.getErrorType());
            logger.info("Request ID:       " + ase.getRequestId());
            return "";
        } catch (AmazonClientException ace) {
            logger.info("Caught an AmazonClientException: ");
            logger.info("Error Message: " + ace.getMessage());
            return "";
        } catch (IOException e) {
            logger.info("Error Message: " + e.getMessage());
            return "";
        }
    }

    public void saveData(String idTarget, String name, String pathImage, String pathVideo) {
        TargetManager targetManager = new TargetManager();
        targetManager.setTargetId(idTarget);
        targetManager.setName(name);
        targetManager.setImage(pathImage);
        targetManager.setVideoUrl(pathVideo);
        targetManager.setActive(true);
        targetManagerRepository.save(targetManager);
    }

    public TargetManager getTarget(int id){
        return targetManagerRepository.findById(id);
    }
}
