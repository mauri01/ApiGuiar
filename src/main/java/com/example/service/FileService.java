package com.example.service;

import com.example.model.TargetManager;
import com.example.repository.TargetManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service("fileService")
public class FileService {

    @Autowired
    private TargetManagerRepository targetManagerRepository;

    @Value("${spring.folder.image}")
    String UPLOADED_FOLDER;

    @Value("${spring.folder.video}")
    String UPLOADED_FOLDER_VIDEO;

    public String saveFile(MultipartFile file,String type){
        Path path=null;
        try {
            byte[] bytes = file.getBytes();
            if(type.contains("image")){
                path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            }else{
                path = Paths.get(UPLOADED_FOLDER_VIDEO + file.getOriginalFilename());
            }
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }

        return String.valueOf(path);
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
